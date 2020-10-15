/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2015 Gary Rowe
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package org.hid4java;

import org.hid4java.event.HidServicesListenerList;
import org.hid4java.jna.HidApi;
import org.hid4java.jna.HidDeviceInfoStructure;

import java.util.*;

/**
 * Manager to provide the following to HID services:
 * <ul>
 * <li>Access to the underlying JNA and hidapi library</li>
 * <li>Device attach/detach detection (if configured)</li>
 * <li>Device data read (if configured)</li>
 * </ul>
 *
 * @since 0.0.1
 */
class HidDeviceManager {

  /**
   * The HID services specification providing configuration parameters
   */
  private final HidServicesSpecification hidServicesSpecification;

  /**
   * The currently attached devices keyed on ID
   */
  private final Map<String, HidDevice> attachedDevices = Collections.synchronizedMap(new HashMap<String, HidDevice>());

  /**
   * HID services listener list
   */
  private final HidServicesListenerList listenerList;

  /**
   * The device enumeration thread
   *
   * We use a Thread instead of Executor since it may be stopped/paused/restarted frequently
   * and executors are more heavyweight in this regard
   */
  private Thread scanThread = null;

  /**
   * Constructs a new device manager
   *
   * @param listenerList             The HID services providing access to the event model
   * @param hidServicesSpecification Provides various parameters for configuring HID services
   *
   * @throws HidException If USB HID initialization fails
   */
  HidDeviceManager(HidServicesListenerList listenerList, HidServicesSpecification hidServicesSpecification) throws HidException {

    this.listenerList = listenerList;
    this.hidServicesSpecification = hidServicesSpecification;

    // Attempt to initialise and fail fast
    try {
      HidApi.init();
    } catch (Throwable t) {
      // Typically this is a linking issue with the native library
      throw new HidException("Hidapi did not initialise: " + t.getMessage(), t);
    }

  }

  /**
   * Starts the manager
   *
   * If already started (scanning) it will immediately return without doing anything
   *
   * Otherwise this will perform a one-off scan of all devices then if the scan interval
   * is zero will stop there or will start the scanning daemon thread at the required interval.
   *
   * @throws HidException If something goes wrong (such as Hidapi not initialising correctly)
   */
  public void start() {

    // Check for previous start
    if (this.isScanning()) {
      return;
    }

    // Perform a one-off scan to populate attached devices
    scan();

    // Ensure we have a scan thread available
    configureScanThread(getScanRunnable());

  }

  /**
   * Stop the scan thread and close all attached devices
   *
   * This is normally part of a general application shutdown
   */
  public synchronized void stop() {

    stopScanThread();

    // Close all attached devices
    for (HidDevice hidDevice: attachedDevices.values()) {
        hidDevice.close();
    }

  }

  /**
   * Updates the device list by adding newly connected devices to it and by
   * removing no longer connected devices.
   *
   * Will fire attach/detach events as appropriate.
   */
  public synchronized void scan() {

    List<String> removeList = new ArrayList<>();

    List<HidDevice> attachedHidDeviceList = getAttachedHidDevices();

    for (HidDevice attachedDevice : attachedHidDeviceList) {

      if (!this.attachedDevices.containsKey(attachedDevice.getId())) {

        // Device has become attached so add it but do not open
        attachedDevices.put(attachedDevice.getId(), attachedDevice);

        // Fire the event on a separate thread
        listenerList.fireHidDeviceAttached(attachedDevice);

      }
    }

    for (Map.Entry<String, HidDevice> entry : attachedDevices.entrySet()) {

      String deviceId = entry.getKey();
      HidDevice hidDevice = entry.getValue();

      if (!attachedHidDeviceList.contains(hidDevice)) {

        // Keep track of removals
        removeList.add(deviceId);

        // Fire the event on a separate thread
        listenerList.fireHidDeviceDetached(this.attachedDevices.get(deviceId));

      }
    }

    if (!removeList.isEmpty()) {
      // Update the attached devices map
      this.attachedDevices.keySet().removeAll(removeList);
    }

  }

  /**
   * @return True if the scan thread is running, false otherwise.
   */
  public boolean isScanning() {
    return scanThread != null && scanThread.isAlive();
  }

  /**
   * @return A list of all attached HID devices
   */
  public List<HidDevice> getAttachedHidDevices() {

    List<HidDevice> hidDeviceList = new ArrayList<>();

    final HidDeviceInfoStructure root;
    try {
      // Use 0,0 to list all attached devices
      // This comes back as a linked list from hidapi
      root = HidApi.enumerateDevices(0, 0);
    } catch (Throwable e) {
      // Could not initialise hidapi (possibly an unknown platform)
      // Trigger a general stop as something serious has happened
      stop();
      // Inform the caller that something serious has gone wrong
      throw new HidException("Unable to start HidApi: " + e.getMessage());
    }

    if (root != null) {

      HidDeviceInfoStructure hidDeviceInfoStructure = root;
      do {
        // Wrap in HidDevice
        hidDeviceList.add(new HidDevice(
          hidDeviceInfoStructure,
          this,
          hidServicesSpecification));
        // Move to the next in the linked list
        hidDeviceInfoStructure = hidDeviceInfoStructure.next();
      } while (hidDeviceInfoStructure != null);

      // Dispose of the device list to free memory
      HidApi.freeEnumeration(root);
    }

    return hidDeviceList;
  }

  /**
   * Indicate that a device write has occurred which may require a change in scanning frequency
   */
  public void afterDeviceWrite() {

    if (ScanMode.SCAN_AT_FIXED_INTERVAL_WITH_PAUSE_AFTER_WRITE == hidServicesSpecification.getScanMode() && isScanning()) {
      stopScanThread();
      // Ensure we have a new scan executor service available
      configureScanThread(getScanRunnable());

    }

  }

  /**
   * Indicate that an automatic data read has occurred which may require an event to be fired
   *
   * @param hidDevice The device that has received data
   * @param dataReceived The data received
   * @since 0.8.0
   */
  public void afterDeviceDataRead(HidDevice hidDevice, byte[] dataReceived) {

    if (dataReceived != null && dataReceived.length > 0) {
      this.listenerList.fireHidDataReceived(hidDevice, dataReceived);
    }

  }

  /**
   * Stop the scan thread
   */
  private synchronized void stopScanThread() {

    if (isScanning()) {
      scanThread.interrupt();
    }

  }

  /**
   * Configures the scan thread to allow recovery from stop or pause
   */
  private synchronized void configureScanThread(Runnable scanRunnable) {

    if (isScanning()) {
      stopScanThread();
    }

    // Require a new one
    scanThread = new Thread(scanRunnable);
    scanThread.setDaemon(true);
    scanThread.setName("hid4java device scanner");
    scanThread.start();

  }

  private synchronized Runnable getScanRunnable() {

    final int scanInterval = hidServicesSpecification.getScanInterval();
    final int pauseInterval = hidServicesSpecification.getPauseInterval();

    switch (hidServicesSpecification.getScanMode()) {
      case NO_SCAN:
        return new Runnable() {
          @Override
          public void run() {
            // Do nothing
          }
        };
      case SCAN_AT_FIXED_INTERVAL:
        return new Runnable() {
          @Override
          public void run() {

            while (true) {
              try {
                //noinspection BusyWait
                Thread.sleep(scanInterval);
              } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
              }
              scan();
            }
          }
        };
      case SCAN_AT_FIXED_INTERVAL_WITH_PAUSE_AFTER_WRITE:
        return new Runnable() {
          @Override
          public void run() {
            // Provide an initial pause
            try {
              Thread.sleep(pauseInterval);
            } catch (final InterruptedException e) {
              Thread.currentThread().interrupt();
            }

            // Switch to continuous running
            while (true) {
              try {
                //noinspection BusyWait
                Thread.sleep(scanInterval);
              } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
              }
              scan();
            }
          }
        };
      default:
        return null;
    }


  }

}