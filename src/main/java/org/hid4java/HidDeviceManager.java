package org.hid4java;

import org.hid4java.event.HidServicesListenerList;
import org.hid4java.jna.HidApi;
import org.hid4java.jna.HidDeviceInfoStructure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>Manager to provide the following to HID services:</p>
 * <ul>
 * <li>Access to the underlying JNA and hidapi library</li>
 * <li>Device attach/detach detection</li>
 * </ul>
 *
 * @since 0.0.1
 *  
 */
class HidDeviceManager {

  /**
   * The scan interval in milliseconds
   */
  private int scanInterval = 500;

  /**
   * The currently attached devices keyed on ID
   */
  private final Map<String, HidDevice> attachedDevices = Collections.synchronizedMap(new HashMap<String, HidDevice>());

  /**
   * HID services listener list
   */
  private final HidServicesListenerList listenerList;
  private Thread scanThread = null;

  /**
   * Indicates whether or not the {@link #scanThread} is running
   */
  private boolean scanning = false;

  /**
   * Constructs a new device manager
   *
   * @param listenerList The HID services providing access to the event model
   * @param scanInterval The scan interval in milliseconds (default is 500ms)
   *
   * @throws HidException If USB HID initialization fails
   */
  HidDeviceManager(HidServicesListenerList listenerList, final int scanInterval) throws HidException {

    this.listenerList = listenerList;
    this.scanInterval = scanInterval;

  }

  /**
   * Starts the manager.
   *
   * If already started (scanning) it will immediately return without doing anything
   *
   * Otherwise this will perform a one-off scan of all devices then if the scan interval
   * is zero will stop there or will start the scanning daemon thread at the required interval.
   */
  public void start() {

    // Check for previous start
    if (this.isScanning()) {
      return;
    }

    // Perform a one-off scan to populate attached devices
    scan();

    // Do not start the scan thread when interval is set to 0
    final int scanInterval = this.scanInterval;
    if (scanInterval == 0) {
      return;
    }

    // Create a daemon thread to ensure lifecycle
    scanThread = new Thread(
      new Runnable() {
        @Override
        public void run() {
          scanning = true;
          while (true) {
            try {
              Thread.sleep(scanInterval);
            } catch (final InterruptedException e) {
              Thread.currentThread().interrupt();
              break;
            }
            scan();
          }
          // Allow restart
          scanning = false;
        }
      });
    scanThread.setDaemon(true);
    scanThread.setName("hid4java Device Scanner");
    scanThread.start();
  }

  /**
   * Stop the scanning thread if it is running
   */
  public synchronized void stop() {

    if (scanThread != null) {
      scanThread.interrupt();
    }

  }

  /**
   * Updates the device list by adding newly connected devices to it and by
   * removing no longer connected devices
   */
  public synchronized void scan() {

    List<String> removeList = new ArrayList<String>();

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
   * @param scanInterval The scan thread's interval in millis (requires restart of thread)
   */
  public void setScanInterval(int scanInterval) {
    this.scanInterval = scanInterval;
  }

  /**
   * @return True if the scan thread is running, false otherwise.
   */
  public boolean isScanning() {
    return scanning;
  }


  /**
   * @return A list of all attached HID devices
   */
  public List<HidDevice> getAttachedHidDevices() {

    List<HidDevice> hidDeviceList = new ArrayList<HidDevice>();

    // Use 0,0 to list all attached devices
    // This comes back as a linked list from hidapi
    HidDeviceInfoStructure root = HidApi.enumerateDevices(0, 0);
    if (root != null) {

      HidDeviceInfoStructure hidDeviceInfoStructure = root;
      do {
        // Wrap in HidDevice
        hidDeviceList.add(new HidDevice(hidDeviceInfoStructure));
        // Move to the next in the linked list
        hidDeviceInfoStructure = hidDeviceInfoStructure.next();
      } while (hidDeviceInfoStructure != null);

      // Dispose of the device list to free memory
      HidApi.freeEnumeration(root);
    }

    return hidDeviceList;
  }
}