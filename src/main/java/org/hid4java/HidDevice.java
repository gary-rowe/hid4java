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

import org.hid4java.jna.HidApi;
import org.hid4java.jna.HidDeviceInfoStructure;
import org.hid4java.jna.HidDeviceStructure;

import java.util.Arrays;

/**
 * <p>
 * High level wrapper to provide the following to API consumers:
 * </p>
 * <ul>
 * <li>Simplified access to the underlying JNA HidDeviceStructure</li>
 * </ul>
 *
 * @since 0.0.1  
 */
public class HidDevice {

  private final HidDeviceManager hidDeviceManager;
  private HidDeviceStructure hidDeviceStructure;

  private final String path;
  private final int vendorId;
  private final int productId;
  private String serialNumber;
  private final int releaseNumber;
  private String manufacturer;
  private String product;
  private final int usagePage;
  private final int usage;
  private final int interfaceNumber;

  /**
   * @param infoStructure    The HID device info structure providing details
   * @param hidDeviceManager The HID device manager providing access to device enumeration for post IO scanning
   * @since 0.1.0
   */
  public HidDevice(HidDeviceInfoStructure infoStructure, HidDeviceManager hidDeviceManager) {

    this.hidDeviceManager = hidDeviceManager;

    this.hidDeviceStructure = null;

    this.path = infoStructure.path;

    // Note that the low-level HidDeviceInfoStructure is directly written to by
    // the JNA library and implies an unsigned short which is not available in Java.
    // The bitmask converts from [-32768, 32767] to [0,65535]
    // In Java 8 Short.toUnsignedInt() is available.
    this.vendorId = infoStructure.vendor_id & 0xffff;
    this.productId = infoStructure.product_id & 0xffff;

    this.releaseNumber = infoStructure.release_number;
    if (infoStructure.serial_number != null) {
      this.serialNumber = infoStructure.serial_number.toString();
    }
    if (infoStructure.manufacturer_string != null) {
      this.manufacturer = infoStructure.manufacturer_string.toString();
    }
    if (infoStructure.product_string != null) {
      this.product = infoStructure.product_string.toString();
    }
    this.usagePage = infoStructure.usage_page;
    this.usage = infoStructure.usage;
    this.interfaceNumber = infoStructure.interface_number;
  }

  /**
   * The "path" is well-supported across Windows, Mac and Linux so makes a
   * better choice for a unique ID
   * <p>
   * See #8 for details
   *
   * @return A unique device ID made up from vendor ID, product ID and serial number
   * @since 0.1.0
   */
  public String getId() {
    return path;
  }

  /**
   * @return The device path
   * @since 0.1.0
   */
  public String getPath() {
    return path;
  }

  /**
   * @return Int version of vendor ID
   * @since 0.1.0
   */
  public int getVendorId() {
    return vendorId;
  }

  /**
   * @return Int version of product ID
   * @since 0.1.0
   */
  public int getProductId() {
    return productId;
  }

  /**
   * @return The device serial number
   * @since 0.1.0
   */
  public String getSerialNumber() {
    return serialNumber;
  }

  /**
   * @return The release number
   * @since 0.1.0
   */
  public int getReleaseNumber() {
    return releaseNumber;
  }

  /**
   * @return The manufacturer
   * @since 0.1.0
   */
  public String getManufacturer() {
    return manufacturer;
  }

  /**
   * @return The product
   * @since 0.1.0
   */
  public String getProduct() {
    return product;
  }

  /**
   * @return The usage page
   * @since 0.1.0
   */
  public int getUsagePage() {
    return usagePage;
  }

  /**
   * @return The usage information
   * @since 0.1.0
   */
  public int getUsage() {
    return usage;
  }

  public int getInterfaceNumber() {
    return interfaceNumber;
  }

  /**
   * <p>Open this device and obtain a device structure</p>
   *
   * @return True if the device was successfully opened
   * @since 0.1.0
   */
  public boolean open() {
    hidDeviceStructure = HidApi.open(path);
    return hidDeviceStructure != null;
  }

  /**
   * @return True if the device structure is present
   * @since 0.1.0
   */
  public boolean isOpen() {
    return hidDeviceStructure != null;
  }

  /**
   * <p>Close this device freeing the HidApi resources</p>
   *
   * @since 0.1.0
   */
  public void close() {
    if (!isOpen()) {
      return;
    }
    HidApi.close(hidDeviceStructure);
    hidDeviceStructure = null;
  }

  /**
   * <p>
   * Set the device handle to be non-blocking
   * </p>
   *
   * <p>
   * In non-blocking mode calls to hid_read() will return immediately with a
   * value of 0 if there is no data to be read. In blocking mode, hid_read()
   * will wait (block) until there is data to read before returning
   * </p>
   *
   * <p>
   * Non-blocking can be turned on and off at any time
   * </p>
   *
   * @param nonBlocking True if non-blocking mode is required
   * @since 0.1.0
   */
  public void setNonBlocking(boolean nonBlocking) {
    if (!isOpen()) {
      throw new IllegalStateException("Device has not been opened");
    }
    HidApi.setNonBlocking(hidDeviceStructure, nonBlocking);
  }

  /**
   * <p>
   * Read an Input report from a HID device
   * </p>
   * <p>
   * Input reports are returned to the host through the INTERRUPT IN endpoint.
   * The first byte will contain the Report number if the device uses numbered
   * reports
   * </p>
   *
   * @param data The buffer to read into
   * @return The actual number of bytes read and -1 on error. If no packet was
   * available to be read and the handle is in non-blocking mode, this
   * function returns 0.
   * @since 0.1.0
   */
  public int read(byte[] data) {
    if (!isOpen()) {
      throw new IllegalStateException("Device has not been opened");
    }
    return HidApi.read(hidDeviceStructure, data);
  }

  /**
   * <p>
   * Read an Input report from a HID device
   * </p>
   * <p>
   * Input reports are returned to the host through the INTERRUPT IN endpoint.
   * The first byte will contain the Report number if the device uses numbered
   * reports
   * </p>
   *
   * @param amountToRead  the number of bytes to read
   * @param timeoutMillis The number of milliseconds to wait before giving up
   * @return a Byte array of the read data
   * @since 0.1.0
   */
  public Byte[] read(int amountToRead, int timeoutMillis) {
    if (!isOpen()) {
      throw new IllegalStateException("Device has not been opened");
    }

    byte[] bytes = new byte[amountToRead];
    int read = HidApi.read(hidDeviceStructure, bytes, timeoutMillis);
    Byte[] retData = new Byte[read];
    for (int i = 0; i < read; i++) {
      retData[i] = bytes[i];
    }
    return retData;
  }

  /**
   * <p>Read an Input report from a HID device</p>
   * Input reports are returned to the host through the INTERRUPT IN endpoint.
   * The first byte will contain the Report number if the device uses numbered
   * reports
   *
   * @param amountToRead the number of bytes to read. If -1 then read until no more bytes are available.
   * @return a Byte array of the read data
   * @since 0.1.0
   */
  public Byte[] read(int amountToRead) {
    if (!isOpen()) {
      throw new IllegalStateException("Device has not been opened");
    }

    byte[] bytes = new byte[amountToRead];
    int read = HidApi.read(hidDeviceStructure, bytes);
    Byte[] retData = new Byte[read];
    for (int i = 0; i < read; i++) {
      retData[i] = bytes[i];
    }
    return retData;
  }

  /**
   * <p>
   * Read an Input report from a HID device
   * </p>
   * <p>
   * Input reports are returned to the host through the INTERRUPT IN endpoint.
   * The first byte will contain the Report number if the device uses numbered
   * reports
   * </p>
   *
   * @return a Byte array of the read data
   * @since 0.1.0
   */
  public Byte[] read() {
    return read(64, 1000);
  }

  /**
   * <p>
   * Read an Input report from a HID device with timeout
   * </p>
   *
   * @param bytes         The buffer to read into
   * @param timeoutMillis The number of milliseconds to wait before giving up
   * @return The actual number of bytes read and -1 on error. If no packet was
   * available to be read within the timeout period returns 0.
   * @since 0.1.0
   */
  public int read(byte[] bytes, int timeoutMillis) {
    if (!isOpen()) {
      throw new IllegalStateException("Device has not been opened");
    }
    return HidApi.read(hidDeviceStructure, bytes, timeoutMillis);

  }

  /**
   * <p>
   * Get a feature report from a HID device
   * </p>
   * <p>
   * Under the covers the HID library will set the first byte of data[] to the
   * Report ID of the report to be read. Upon return, the first byte will
   * still contain the Report ID, and the report data will start in data[1]
   * </p>
   * <p>
   * This method handles all the wide string and array manipulation for you
   * </p>
   *
   * @param data     The buffer to contain the report
   * @param reportId The report ID (or (byte) 0x00)
   * @return The number of bytes read plus one for the report ID (which has
   * been removed from the first byte), or -1 on error.
   * @since 0.1.0
   */
  public int getFeatureReport(byte[] data, byte reportId) {
    if (!isOpen()) {
      throw new IllegalStateException("Device has not been opened");
    }
    return HidApi.getFeatureReport(hidDeviceStructure, data, reportId);
  }

  /**
   * <p>
   * Send a Feature report to the device
   * </p>
   *
   * <p>
   * Under the covers, feature reports are sent over the Control endpoint as a
   * Set_Report transfer. The first byte of data[] must contain the Report ID.
   * For devices which only support a single report, this must be set to 0x0.
   * The remaining bytes contain the report data
   * </p>
   * <p>
   * Since the Report ID is mandatory, calls to hid_send_feature_report() will
   * always contain one more byte than the report contains. For example, if a
   * hid report is 16 bytes long, 17 bytes must be passed to
   * hid_send_feature_report(): the Report ID (or 0x0, for devices which do
   * not use numbered reports), followed by the report data (16 bytes). In
   * this example, the length passed in would be 17
   * </p>
   *
   * <p>
   * This method handles all the array manipulation for you
   * </p>
   *
   * @param data     The feature report data (will be widened and have the report
   *                 ID pre-pended)
   * @param reportId The report ID (or (byte) 0x00)
   * @return This function returns the actual number of bytes written and -1
   * on error.
   * @since 0.1.0
   */
  public int sendFeatureReport(byte[] data, byte reportId) {
    if (!isOpen()) {
      throw new IllegalStateException("Device has not been opened");
    }
    return HidApi.sendFeatureReport(hidDeviceStructure, data, reportId);
  }

  /**
   * <p>
   * Get a string from a HID device, based on its string index
   * </p>
   *
   * @param index The index
   * @return The string
   * @since 0.1.0
   */
  public String getIndexedString(int index) {
    return HidApi.getIndexedString(hidDeviceStructure, index);
  }

  /**
   * <p>Write the message to the HID API without zero byte padding.</p>
   *
   * <p>Note that the report ID will be prefixed to the HID packet as per HID rules.</p>
   *
   * @param message      The message
   * @param packetLength The packet length
   * @param reportId     The report ID (will be prefixed to the HID packet)
   * @return The number of bytes written (including report ID), or -1 if an error occurs
   * @since 0.1.0
   */
  public int write(byte[] message, int packetLength, byte reportId) {
    return write(message, packetLength, reportId, false);
  }

  /**
   * <p>Write the message to the HID API with optional zero byte padding to packet length.</p>
   *
   * <p>Note that the report ID will be prefixed to the HID packet as per HID rules.</p>
   *
   * @param message      The message
   * @param packetLength The packet length
   * @param reportId     The report ID
   * @param applyPadding True if the message should be filled with zero bytes to the packet length
   * @return The number of bytes written (including report ID), or -1 if an error occurs
   * @since 0.8.0
   */
  public int write(byte[] message, int packetLength, byte reportId, boolean applyPadding) {
    if (!isOpen()) {
      throw new IllegalStateException("Device has not been opened");
    }

    if (applyPadding) {
      message = Arrays.copyOf(message, packetLength + 1);
    }

    int result = HidApi.write(hidDeviceStructure, message, packetLength, reportId);
    // Update HID manager
    hidDeviceManager.afterDeviceWrite();
    return result;

  }

  /**
   * @return The last error message from HID API
   * @since 0.1.0
   */
  public String getLastErrorMessage() {
    return HidApi.getLastErrorMessage(hidDeviceStructure);
  }

  /**
   * @param vendorId     The vendor ID
   * @param productId    The product ID
   * @param serialNumber The serial number
   * @return True if the device matches the given the combination with vendorId, productId being zero acting as a wildcard
   * @since 0.1.0
   */
  public boolean isVidPidSerial(int vendorId, int productId, String serialNumber) {
    if (serialNumber == null)
      return (vendorId == 0 || this.vendorId == vendorId)
        && (productId == 0 || this.productId == productId);
    else
      return (vendorId == 0 || this.vendorId == vendorId)
        && (productId == 0 || this.productId == productId)
        && (this.serialNumber.equals(serialNumber));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    HidDevice hidDevice = (HidDevice) o;

    return path.equals(hidDevice.path);

  }

  @Override
  public int hashCode() {
    return path.hashCode();
  }

  @Override
  public String toString() {
    return "HidDevice [path=" + path
      + ", vendorId=0x" + Integer.toHexString(vendorId)
      + ", productId=0x" + Integer.toHexString(productId)
      + ", serialNumber=" + serialNumber
      + ", releaseNumber=0x" + Integer.toHexString(releaseNumber)
      + ", manufacturer=" + manufacturer
      + ", product=" + product
      + ", usagePage=0x" + Integer.toHexString(usagePage)
      + ", usage=0x" + Integer.toHexString(usage)
      + ", interfaceNumber=" + interfaceNumber
      + "]";
  }

}
