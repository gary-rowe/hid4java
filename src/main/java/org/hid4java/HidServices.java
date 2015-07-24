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

import java.util.List;

/**
 * <p>JNA bridge class to provide the following to USB HID:</p>
 * <ul>
 * <li>Access to the <code>signal11/hidapi</code> via JNA</li>
 * </ul>
 * <p>Requires the hidapi to be present on the classpath or the system library search path.</p>
 *
 * @since 0.0.1
 *  
 */
public class HidServices {

  /**
   * The HID services listeners
   */
  private final HidServicesListenerList listeners = new HidServicesListenerList();

  /**
   * The HID device scanner
   */
  private final HidDeviceManager deviceManager;

  /**
   * Initialise and start scanning for USB devices
   *
   * @throws HidException If something goes wrong
   */
  public HidServices() throws HidException {

    deviceManager = new HidDeviceManager(listeners, 500);
    deviceManager.start();

    // Ensure we release resources
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        System.err.println("Triggered shutdown hook");
        deviceManager.stop();
        HidApi.exit();
      }
    });

  }

  /**
   * Stop scanning for devices and close connection to HidApi
   */
  public void stop() {

    deviceManager.stop();

  }

  /**
   * @param listener The listener to add
   */
  public void addHidServicesListener(final HidServicesListener listener) {
    this.listeners.add(listener);
  }

  /**
   * @param listener The listener to remove
   */
  public void removeUsbServicesListener(final HidServicesListener listener) {
    this.listeners.remove(listener);
  }

  /**
   * Manually scans for HID device connection changes and triggers listener events as required
   */
  public void scan() {
    this.deviceManager.scan();
  }

  /**
   * @return A list of all attached HID devices
   */
  public List<HidDevice> getAttachedHidDevices() {
    return deviceManager.getAttachedHidDevices();
  }

  /**
   * @param vendorId     The vendor ID
   * @param productId    The product ID
   * @param serialNumber The serial number (use null for wildcard)
   *
   * @return The device if attached, null if detached
   */
  public HidDevice getHidDevice(int vendorId, int productId, String serialNumber) {

    List<HidDevice> devices = deviceManager.getAttachedHidDevices();
    for (HidDevice device : devices) {
      if (device.isVidPidSerial(vendorId, productId, serialNumber)) {
        if (device.open()) {
          return device;
        } else {
          return null;
        }
      }
    }

    return null;
  }
}