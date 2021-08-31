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

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * JNA bridge class to provide the following to USB HID:
 * <ul>
 * <li>Access to the <code>signal11/hidapi</code> via JNA</li>
 * </ul>
 * Requires the hidapi to be present on the classpath or the system library search path.
 *
 * @since 0.0.1
 */
public class HidServices {

  /**
   * The HID services listeners for receiving attach/detach events etc
   */
  private final HidServicesListenerList listeners = new HidServicesListenerList();

  /**
   * The HID device manager handles scanning operations
   */
  private final HidDeviceManager hidDeviceManager;

  /**
   * Jar entry point to allow for version interrogation
   *
   * @param args Nothing required
   */
  public static void main(String[] args) {
    System.out.println("Version: " + getVersion());
  }

  /**
   * Initialise with a default HID specification
   *
   * @throws HidException If something goes wrong (see {@link HidDeviceManager#HidDeviceManager(HidServicesListenerList, HidServicesSpecification)}
   */
  public HidServices() throws HidException {
    this(new HidServicesSpecification());
  }

  /**
   * @param hidServicesSpecification Provides various parameters for configuring HID services
   * @throws HidException If something goes wrong (see {@link HidDeviceManager#HidDeviceManager(HidServicesListenerList, HidServicesSpecification)}
   */
  public HidServices(HidServicesSpecification hidServicesSpecification) {
    hidDeviceManager = new HidDeviceManager(listeners, hidServicesSpecification);

    // Check for automatic start (default behaviour for 0.6.0 and below)
    // which will prevent an attachment event firing if the device is already
    // attached since listeners will not have been registered at this point
    if (hidServicesSpecification.isAutoStart()) {
      hidDeviceManager.start();
    }

    if (hidServicesSpecification.isAutoShutdown()) {
      // Ensure we release resources during shutdown
      Runtime.getRuntime().addShutdownHook(
        new Thread() {
          @Override
          public void run() {
            shutdown();
          }
        });
    }

  }

  /**
   * Stop all device threads and shut down the {@link HidApi}
   */
  public void shutdown() {
    stop();
    try {
      HidApi.exit();
    } catch (Throwable e) {
      // Silently fail (user will already have been given an exception)
    }
  }

  /**
   * Stop all threads (enumeration, data read etc), close all devices
   * and clear all listeners
   *
   * Normally part of an application shutdown
   */
  public void stop() {
    hidDeviceManager.stop();
    this.listeners.clear();
  }

  /**
   * Start all threads (enumeration, data read etc) as configured
   */
  public void start() {
    hidDeviceManager.start();
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
  public void removeHidServicesListener(final HidServicesListener listener) {
    this.listeners.remove(listener);
  }

  /**
   * Manually scans for HID device connection changes and triggers listener events as required
   */
  public void scan() {
    this.hidDeviceManager.scan();
  }

  /**
   * @return A list of all attached HID devices
   */
  public List<HidDevice> getAttachedHidDevices() {
    return hidDeviceManager.getAttachedHidDevices();
  }

  /**
   * @param vendorId     The vendor ID
   * @param productId    The product ID
   * @param serialNumber The serial number (use null for wildcard)
   * @return The device if attached, null if detached
   */
  public HidDevice getHidDevice(int vendorId, int productId, String serialNumber) {

    List<HidDevice> devices = hidDeviceManager.getAttachedHidDevices();
    for (HidDevice device : devices) {
      if (device.isVidPidSerial(vendorId, productId, serialNumber)) {
        device.open();
        return device;
      }
    }

    return null;
  }

  /**
   * @return The current library version from the manifest or 0.0.x if an error occurs
   */
  public static String getVersion() {

    Class<HidServices> clazz = HidServices.class;
    String className = clazz.getSimpleName() + ".class";
    String classPath = clazz.getResource(className).toString();
    if (!classPath.startsWith("jar")) {
      // Class not from JAR
      return "0.0.1";
    }
    String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) +
      "/META-INF/MANIFEST.MF";
    Manifest manifest;
    try {
      manifest = new Manifest(new URL(manifestPath).openStream());
    } catch (IOException e) {
      return "0.0.2";
    }
    Attributes attr = manifest.getMainAttributes();
    String value = attr.getValue("Implementation-Version");
    if (null == value) {
      return "0.0.3";
    } else {
      return value;
    }
  }

  /**
   * Returns the full version of the underlying hidapi library
   *
   * @return The version in major.minor.patch format
   */
  public static String getNativeVersion() {
    return HidApi.getVersion();
  }
}
