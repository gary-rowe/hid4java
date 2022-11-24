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

package org.hid4java.jna;

import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.WString;

/**
 * JNA utility class to provide the following to low level operations:
 * <ul>
 * <li>Direct access to the HID API library through JNA</li>
 * </ul>
 *
 * @since 0.0.1
 */
public class HidApi {

  /**
   * Default length for wide string buffer
   */
  private static final int WSTR_LEN = 512;

  /**
   * Error message if device is not initialised
   */
  private static final String DEVICE_NULL = "Device not initialised";

  /**
   * Device error code
   */
  private static final int DEVICE_ERROR = -2;

  /**
   * Enables use of the libusb variant of the hidapi native library when running on a Linux platform.
   * <p>
   * The default is hidraw which enables Bluetooth devices but requires udev rules.
   */
  public static boolean useLibUsbVariant = false;

  /**
   * When false - all devices will be opened in exclusive mode. (Default)
   * When true - all devices will be opened in non-exclusive mode.
   * <p>
   * See {@link DarwinHidApiLibrary#hid_darwin_set_open_exclusive(int)} for more information.
   */
  public static boolean darwinOpenDevicesNonExclusive = false;

  /**
   * Enables HID traffic logging to stdout to assist debugging. This will show all bytes (including the extra report ID)
   * that were sent or received via HIDAPI buffers. It does not log direct string calls (e.g. getEnumeratedString()).
   *
   * Format is '&gt;' for host to device then '[count]' then hex bytes.
   *
   * This may present a security issue if left enabled in production, although a packet sniffer would see the same data.
   */
  public static boolean logTraffic = false;

  /**
   * The HID API library
   */
  private static HidApiLibrary hidApiLibrary;

  /**
   * Open a HID device using a Vendor ID (VID), Product ID (PID) and optionally a serial number
   *
   * @param vendor       The vendor ID
   * @param product      The product ID
   * @param serialNumber The serial number
   *
   * @return The device or null if not found
   */
  public static HidDeviceStructure open(int vendor, int product, String serialNumber) {

    // Attempt to open the device
    Pointer p = hidApiLibrary.hid_open(
      (short) vendor,
      (short) product,
      serialNumber == null ? null : new WString(serialNumber)
    );

    if (p != null) {
      // Wrap the structure
      return new HidDeviceStructure(p);
    }

    return null;

  }

  /**
   * Initialise the HID API library. Should always be called before using any other API calls.
   */
  public static void init() {

    if (useLibUsbVariant && Platform.isLinux()) {
      hidApiLibrary = LibusbHidApiLibrary.INSTANCE;
    } else if (Platform.isMac()) {
      hidApiLibrary = DarwinHidApiLibrary.INSTANCE;
    } else {
      hidApiLibrary = HidrawHidApiLibrary.INSTANCE;
    }

    hidApiLibrary.hid_init();

    if (hidApiLibrary instanceof DarwinHidApiLibrary) {
      ((DarwinHidApiLibrary) hidApiLibrary).hid_darwin_set_open_exclusive(darwinOpenDevicesNonExclusive ? 0 : 1);
    }
  }

  /**
   * Finalise the HID API library
   */
  public static void exit() {
    hidApiLibrary.hid_exit();
  }

  /**
   * Open a HID device by its path name
   *
   * @param path The device path (e.g. "0003:0002:00")
   *
   * @return The device or null if not found
   */
  public static HidDeviceStructure open(String path) {
    Pointer p = hidApiLibrary.hid_open_path(path);
    return (p == null ? null : new HidDeviceStructure(p));
  }

  /**
   * Close a HID device
   *
   * @param device The HID device structure
   */
  public static void close(HidDeviceStructure device) {

    if (device != null) {
      hidApiLibrary.hid_close(device.ptr());
    }

  }

  /**
   * Enumerate the attached HID devices
   *
   * @param vendor  The vendor ID
   * @param product The product ID
   *
   * @return The device info of the matching device
   */
  public static HidDeviceInfoStructure enumerateDevices(int vendor, int product) {

    return hidApiLibrary.hid_enumerate((short) vendor, (short) product);

  }

  /**
   * Free an enumeration linked list
   *
   * @param list The list to free
   */
  public static void freeEnumeration(HidDeviceInfoStructure list) {

    hidApiLibrary.hid_free_enumeration(list.getPointer());

  }

  /**
   * @param device The HID device structure
   *
   * @return A string describing the last error which occurred
   */
  public static String getLastErrorMessage(HidDeviceStructure device) {

    if (device == null) {
      return DEVICE_NULL;
    }

    Pointer p = hidApiLibrary.hid_error(device.ptr());

    return p == null ? null : new WideStringBuffer(p.getByteArray(0, WSTR_LEN)).toString();
  }

  /**
   * @param device The HID device
   *
   * @return The device manufacturer string
   */
  public static String getManufacturer(HidDeviceStructure device) {

    if (device == null) {
      return DEVICE_NULL;
    }

    WideStringBuffer wStr = new WideStringBuffer(WSTR_LEN);
    hidApiLibrary.hid_get_manufacturer_string(device.ptr(), wStr, WSTR_LEN);

    return wStr.toString();
  }

  /**
   * @param device The HID device
   *
   * @return The device product ID
   */
  public static String getProductId(HidDeviceStructure device) {

    if (device == null) {
      return DEVICE_NULL;
    }

    WideStringBuffer wBuffer = new WideStringBuffer(WSTR_LEN);
    hidApiLibrary.hid_get_product_string(device.ptr(), wBuffer, WSTR_LEN);

    return wBuffer.toString();
  }

  /**
   * @param device The HID device
   *
   * @return The device serial number
   */
  public static String getSerialNumber(HidDeviceStructure device) {

    if (device == null) {
      return DEVICE_NULL;
    }

    WideStringBuffer wBuffer = new WideStringBuffer(WSTR_LEN);

    hidApiLibrary.hid_get_serial_number_string(device.ptr(), wBuffer, WSTR_LEN);

    return wBuffer.toString();
  }

  /**
   * Set the device handle to be non-blocking
   *
   * In non-blocking mode calls to hid_read() will return immediately with a value of 0 if there is no data to be read.
   * In blocking mode, hid_read() will wait (block) until there is data to read before returning
   *
   * Non-blocking can be turned on and off at any time
   *
   * @param device      The HID device
   * @param nonBlocking True if non-blocking mode is required
   *
   * @return True if successful
   */
  public static boolean setNonBlocking(HidDeviceStructure device, boolean nonBlocking) {

    return device != null && 0 == hidApiLibrary.hid_set_nonblocking(device.ptr(), nonBlocking ? 1 : 0);

  }

  /**
   * Read an Input report from a HID device
   * Input reports are returned to the host through the INTERRUPT IN endpoint. The first byte
   * will contain the Report ID if the device uses numbered reports.
   *
   * @param device The HID device
   * @param buffer The buffer to read into (allow an extra byte if device supports multiple report IDs)
   *
   * @return The actual number of bytes read and -1 on error. If no packet was available to be read
   * and the handle is in non-blocking mode, this function returns 0.
   */
  public static int read(HidDeviceStructure device, byte[] buffer) {

    if (device == null || buffer == null) {
      return DEVICE_ERROR;
    }

    WideStringBuffer wBuffer = new WideStringBuffer(buffer);

    int result = hidApiLibrary.hid_read(device.ptr(), wBuffer, wBuffer.buffer.length);

    if (result > 0) {
      logTraffic(wBuffer, false);
    }

    return result;

  }

  /**
   * Read an Input report from a HID device with timeout
   *
   * @param device        The HID device
   * @param buffer        The buffer to read into
   * @param timeoutMillis The number of milliseconds to wait before giving up
   *
   * @return The actual number of bytes read and -1 on error. If no packet was available to be read within
   * the timeout period returns 0.
   */
  public static int read(HidDeviceStructure device, byte[] buffer, int timeoutMillis) {

    if (device == null || buffer == null) {
      return DEVICE_ERROR;
    }

    WideStringBuffer wBuffer = new WideStringBuffer(buffer);

    int result = hidApiLibrary.hid_read_timeout(device.ptr(), wBuffer, buffer.length, timeoutMillis);

    if (result > 0) {
      logTraffic(wBuffer, false);
    }

    return result;

  }

  /**
   * Get a feature report from a HID device
   *
   * <b>HID API notes</b>
   *
   * Under the covers the HID library will set the first byte of data[] to the Report ID of the report to be read.
   * Upon return, the first byte will still contain the Report ID, and the report data will start in data[1]
   * This method handles all the wide string and array manipulation for you
   *
   * @param device   The HID device
   * @param data     The buffer to contain the report
   * @param reportId The report ID (or (byte) 0x00)
   *
   * @return The number of bytes read plus one for the report ID (which has been removed from the first byte), or -1 on error.
   */
  public static int getFeatureReport(HidDeviceStructure device, byte[] data, byte reportId) {

    if (device == null || data == null) {
      return DEVICE_ERROR;
    }

    // Create a large buffer
    WideStringBuffer report = new WideStringBuffer(WSTR_LEN);
    report.buffer[0] = reportId;
    int res = hidApiLibrary.hid_get_feature_report(device.ptr(), report, data.length + 1);

    if (res == -1) {
      return res;
    }

    // Avoid index out of bounds exception
    System.arraycopy(report.buffer, 1, data, 0, Math.min(res, data.length));

    logTraffic(report, false);

    return res;

  }

  /**
   * Send a Feature report to the device using a simplified interface
   *
   * <b>HID API notes</b>
   *
   * Under the covers, feature reports are sent over the Control endpoint as a Set_Report transfer.
   * The first byte of data[] must contain the Report ID. For devices which only support a single report,
   * this must be set to 0x0. The remaining bytes contain the report data
   * Since the Report ID is mandatory, calls to hid_send_feature_report() will always contain one more byte than
   * the report contains.
   *
   * For example, if a hid report is 16 bytes long, 17 bytes must be passed to
   * hid_send_feature_report(): the Report ID (or 0x00, for devices which do not use numbered reports), followed by
   * the report data (16 bytes). In this example, the bytes written would be 17.
   *
   * This method handles all the array manipulation for you
   *
   * @param device   The HID device
   * @param data     The feature report data (will be widened and have the report ID pre-pended)
   * @param reportId The report ID (or (byte) 0x00)
   *
   * @return This function returns the actual number of bytes written and -1 on error.
   */
  public static int sendFeatureReport(HidDeviceStructure device, byte[] data, byte reportId) {

    if (device == null || data == null) {
      return DEVICE_ERROR;
    }

    WideStringBuffer report = new WideStringBuffer(data.length + 1);
    report.buffer[0] = reportId;

    System.arraycopy(data, 0, report.buffer, 1, data.length);

    logTraffic(report, true);

    return hidApiLibrary.hid_send_feature_report(device.ptr(), report, report.buffer.length);

  }

  /**
   * Write an Output report to a HID device using a simplified interface
   *
   * <b>HID API notes</b>
   *
   * In USB HID the first byte of the data packet must contain the Report ID.
   * For devices which only support a single report, this must be set to 0x00.
   * The remaining bytes contain the report data. Since the Report ID is mandatory,
   * calls to <code>hid_write()</code> will always contain one more byte than the report
   * contains.
   *
   * For example, if a HID report is 16 bytes long, 17 bytes must be passed to <code>hid_write()</code>,
   * the Report ID (or 0x00, for devices with a single report), followed by the report data (16 bytes).
   * In this example, the length passed in would be 17.
   *
   * <code>hid_write()</code> will send the data on the first OUT endpoint, if one exists.
   * If it does not, it will send the data through the Control Endpoint (Endpoint 0)
   *
   * @param device   The device
   * @param data     The report data to write (should not include the Report ID)
   * @param len      The length of the report data (should not include the Report ID)
   * @param reportId The report ID (or (byte) 0x00)
   *
   * @return The number of bytes written, or -1 if an error occurs
   */
  public static int write(HidDeviceStructure device, byte[] data, int len, byte reportId) {

    // Fail fast
    if (device == null || data == null) {
      return DEVICE_ERROR;
    }

    // Precondition checks
    if (data.length < len) {
      len = data.length;
    }

    final WideStringBuffer report;

    // Put report ID into position 0 and fill out buffer
    report = new WideStringBuffer(len + 1);
    report.buffer[0] = reportId;
    if (len >= 1) {
      System.arraycopy(data, 0, report.buffer, 1, len);
    }

    logTraffic(report, true);

    return hidApiLibrary.hid_write(device.ptr(), report, report.buffer.length);

  }

  /**
   * Get a string from a HID device, based on its string index
   *
   * @param device The HID device
   * @param idx    The index
   *
   * @return The string
   */
  public static String getIndexedString(HidDeviceStructure device, int idx) {

    if (device == null) {
      return DEVICE_NULL;
    }
    WideStringBuffer wStr = new WideStringBuffer(WSTR_LEN);
    int res = hidApiLibrary.hid_get_indexed_string(device.ptr(), idx, wStr, WSTR_LEN);

    return res == -1 ? null : wStr.toString();
  }

  /**
   *
   * @param buffer The buffer to serialise for traffic
   * @param isWrite True if writing (from host to device)
   */
  private static void logTraffic(WideStringBuffer buffer, boolean isWrite) {
    if (HidApi.logTraffic && buffer != null && buffer.size() > 0) {
      if (isWrite) {
        System.out.print("> ");
      } else {
        System.out.print("< ");
      }
      System.out.printf("[%02x]:", buffer.buffer.length);
      for (byte b : buffer.buffer) {
        System.out.printf(" %02x", b);
      }
      System.out.println();
    }
  }

}
