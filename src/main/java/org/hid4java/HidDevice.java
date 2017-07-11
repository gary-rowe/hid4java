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

  private String path;
  private short vendorId;
  private short productId;
  private String serialNumber;
  private int releaseNumber;
  private String manufacturer;
  private String product;
  private int usagePage;
  private int usage;
  private int interfaceNumber;

  /**
   * @param infoStructure The HID device info structure providing details
   * @param hidDeviceManager The HID device manager providing access to device enumeration for post IO scanning
   */
  public HidDevice(HidDeviceInfoStructure infoStructure, HidDeviceManager hidDeviceManager) {

    this.hidDeviceManager = hidDeviceManager;

    this.hidDeviceStructure = null;

    this.path = infoStructure.path;
    this.vendorId = infoStructure.vendor_id;
    this.productId = infoStructure.product_id;
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
   *
   * See #8 for details
   *
   * @return A unique device ID made up from vendor ID, product ID and serial number
   */
  public String getId() {
    return path;
  }

  public String getPath() {
    return path;
  }

  public short getVendorId() {
    return vendorId;
  }

  public short getProductId() {
    return productId;
  }

  public String getSerialNumber() {
    return serialNumber;
  }

  public int getReleaseNumber() {
    return releaseNumber;
  }

  public String getManufacturer() {
    return manufacturer;
  }

  public String getProduct() {
    return product;
  }

  public int getUsagePage() {
    return usagePage;
  }

  public int getUsage() {
    return usage;
  }

  public int getInterfaceNumber() {
    return interfaceNumber;
  }

  /**
   * <p>Open this device and obtain a device structure</p>
   * @return True if the device was successfully opened
   */
  public boolean open() {
    hidDeviceStructure = HidApi.open(path);
    return hidDeviceStructure != null;
  }

  /**
   * @return True if the device structure is present
   */
  public boolean isOpen() {
    return hidDeviceStructure != null;
  }

  /**
   * <p>
   * Close this device freeing the HidApi resources
   * </p>
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
   *
   * @return The actual number of bytes read and -1 on error. If no packet was
   * available to be read and the handle is in non-blocking mode, this
   * function returns 0.
   */
  public int read(byte[] data) {
    if (!isOpen()) {
      throw new IllegalStateException("Device has not been opened");
    }
    return HidApi.read(hidDeviceStructure, data);
  }

  /**
   * <p>
   * Read an Input report from a HID device with timeout
   * </p>
   *
   * @param bytes         The buffer to read into
   * @param timeoutMillis The number of milliseconds to wait before giving up
   *
   * @return The actual number of bytes read and -1 on error. If no packet was
   * available to be read within the timeout period returns 0.
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
   *
   * @return The number of bytes read plus one for the report ID (which has
   * been removed from the first byte), or -1 on error.
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
   *
   * @return This function returns the actual number of bytes written and -1
   * on error.
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
   *
   * @return The string
   */
  public String getIndexedString(int index) {
    return HidApi.getIndexedString(hidDeviceStructure, index);
  }

  public int write(byte[] message, int packetLength, byte reportId) {
    if (!isOpen()) {
      throw new IllegalStateException("Device has not been opened");
    }
    int result = HidApi.write(hidDeviceStructure, message, packetLength, reportId);
    // Update HID manager
    hidDeviceManager.afterDeviceWrite();
    return result;
  }

  public String getLastErrorMessage() {
    return HidApi.getLastErrorMessage(hidDeviceStructure);
  }

  /**
   * @param vendorId     The vendor ID
   * @param productId    The product ID
   * @param serialNumber The serial number
   *
   * @return True if the device matches the given the combination
   */
  public boolean isVidPidSerial(int vendorId, int productId, String serialNumber) {
    if(serialNumber == null)
      return (vendorId == 0 || this.vendorId == vendorId)
        && (productId == 0 || this.productId == productId);
    else
       return (vendorId == 0 || this.vendorId == vendorId)
      && (productId == 0 || this.productId == productId)
      && ( this.serialNumber.equals(serialNumber));
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
