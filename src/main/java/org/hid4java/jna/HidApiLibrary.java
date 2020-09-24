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

import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.WString;

/**
 * JNA library interface to act as the proxy for the underlying native library
 * This approach removes the need for any JNI or native code
 * @since 0.1.0
 */
public interface HidApiLibrary extends Library {

  /**
   * Initialize the HIDAPI library.
   * This function initializes the HIDAPI library. Calling it is not strictly necessary,
   * as it will be called automatically by hid_enumerate() and any of the hid_open_*() functions
   * if it is needed. This function should be called at the beginning of execution however,
   * if there is a chance of HIDAPI handles being opened by different threads simultaneously.
   */
  void hid_init();

  /**
   * Finalize the HIDAPI library.
   * <p>
   * This function frees all of the static data associated with HIDAPI. It should be called
   * at the end of execution to avoid memory leaks.
   */
  void hid_exit();

  /**
   * Open a HID device using a Vendor ID (VID), Product ID (PID) and optionally a serial number.
   *
   * If serial_number is NULL, the first device with the specified VID and PID is opened.
   *
   * @param vendor_id The vendor ID
   * @param product_id The product ID
   * @param serial_number The serial number (or null for wildcard)
   *
   * @return A pointer to a HidDevice on success or null on failure
   */
  Pointer hid_open(short vendor_id, short product_id, WString serial_number);

  /**
   * Close a HID device
   *
   * @param device A device handle
   */
  void hid_close(Pointer device);

  /**
   * Get a string describing the last error which occurred.
   *
   * @param device A device handle
   *
   * @return A string containing the last error which occurred or null if none has occurred.
   */
  Pointer hid_error(Pointer device);

  /**
   * Read an Input report from a HID device.
   *
   * Input reports are returned to the host through the INTERRUPT IN endpoint. The first byte will contain the Report number
   * if the device uses numbered reports.
   *
   * @param device A device handle returned from hid_open().
   * @param bytes  A buffer to put the read data into.
   * @param length The number of bytes to read. For devices with multiple reports, make sure to read an extra byte for the report number.
   *
   * @return This function returns the actual number of bytes read and -1 on error. If no packet was available to be read
   * and the handle is in non-blocking mode this function returns 0.
   */
  int hid_read(Pointer device, WideStringBuffer.ByReference bytes, int length);

  /**
   * Read an Input report from a HID device with timeout.
   *
   * Input reports are returned to the host through the INTERRUPT IN endpoint. The first byte will contain the Report number
   * if the device uses numbered reports.
   *
   * @param device  A device handle
   * @param bytes   A buffer to put the read data into.
   * @param length  The number of bytes to read. For devices with multiple reports, make sure to read an extra byte for the report number.
   * @param timeout The timeout in milliseconds or -1 for blocking wait.
   *
   * @return This function returns the actual number of bytes read and -1 on error. If no packet was available to be read within
   * the timeout period, this function returns 0.
   */
  int hid_read_timeout(Pointer device, WideStringBuffer.ByReference bytes, int length, int timeout);

  /**
   * Write an Output report to a HID device.
   *
   * The first byte of data[] must contain the Report ID. For devices which only support a single report, this must be set to 0x0.
   * The remaining bytes contain the report data.
   *
   * Since the Report ID is mandatory, calls to hid_write() will always contain one more byte than the report contains.
   *
   * For example, if a hid report is 16 bytes long, 17 bytes must be passed to hid_write(), the Report ID (or 0x0, for devices with
   * a single report), followed by the report data (16 bytes). In this example, the length passed in would be 17.
   *
   * hid_write() will send the data on the first OUT endpoint, if one exists. If it does not, it will send the data through the
   * Control Endpoint (Endpoint 0).
   *
   * @param device A device handle
   * @param data   the data to send, including the report number as the first byte
   * @param len    The length in bytes of the data to send
   *
   * @return The actual number of bytes written, -1 on error
   */
  int hid_write(Pointer device, WideStringBuffer.ByReference data, int len);

  /**
   * Get a feature report from a HID device.
   *
   * Set the first byte of data[] to the Report ID of the report to be read. Make sure to allow space for this extra byte in data[].
   * Upon return, the first byte will still contain the Report ID, and the report data will start in data[1].
   *
   * @param device A device handle
   * @param data   A buffer to put the read data into, including the Report ID. Set the first byte of data[] to the Report ID of the report to be read, or set it to zero if your device does not use numbered reports.
   * @param length The number of bytes to read, including an extra byte for the report ID. The buffer can be longer than the actual report.
   *
   * @return The number of bytes read plus one for the report ID (which is still in the first byte), or -1 on error
   */
  int hid_get_feature_report(Pointer device, WideStringBuffer.ByReference data, int length);

  /**
   * Send a Feature report to the device.
   *
   * Feature reports are sent over the Control endpoint as a Set_Report transfer.
   *
   * The first byte of data[] must contain the Report ID. For devices which only support a single report, this must be set to 0x0.
   *
   * The remaining bytes contain the report data.
   *
   * Since the Report ID is mandatory, calls to hid_send_feature_report() will always contain one more byte than the report contains.
   *
   * For example, if a hid report is 16 bytes long, 17 bytes must be passed to hid_send_feature_report():
   * the Report ID (or 0x0, for devices which do not use numbered reports), followed by the report data (16 bytes).
   * In this example, the length passed in would be 17.
   *
   * @param device The device handle
   * @param data   The data to send, including the report number as the first byte
   * @param length The length in bytes of the data to send, including the report number
   *
   * @return The actual number of bytes written, -1 on error
   */
  int hid_send_feature_report(Pointer device, WideStringBuffer.ByReference data, int length);

  /**
   * Get a string from a HID device, based on its string index.
   *
   * @param device the device handle
   * @param idx    The index of the string to get
   * @param string A wide string buffer to put the data into
   * @param len    The length of the buffer in multiples of wchar_t
   *
   * @return 0 on success, -1 on failure
   */
  int hid_get_indexed_string(Pointer device, int idx, WideStringBuffer.ByReference string, int len);

  /**
   * Get the manufacturer string from a HID device
   *
   * @param device the device handle
   * @param str    A wide string buffer to put the data into
   * @param len    The length of the buffer in multiple of wchar_t
   *
   * @return 0 on success, -1 on failure
   */
  @SuppressWarnings("UnusedReturnValue")
  int hid_get_manufacturer_string(Pointer device, WideStringBuffer.ByReference str, int len);

  /**
   * Get the product number string from a HID device
   *
   * @param device the device handle
   * @param str    A wide string buffer to put the data into
   * @param len    The length of the buffer in multiple of wchar_t
   *
   * @return 0 on success, -1 on failure
   */
  @SuppressWarnings("UnusedReturnValue")
  int hid_get_product_string(Pointer device, WideStringBuffer.ByReference str, int len);

  /**
   * Get the serial number string from a HID device
   *
   * @param device the device handle
   * @param str    A wide string buffer to put the data into
   * @param len    The length of the buffer in multiple of wchar_t
   *
   * @return 0 on success, -1 on failure
   */
  @SuppressWarnings("UnusedReturnValue")
  int hid_get_serial_number_string(Pointer device, WideStringBuffer.ByReference str, int len);

  /**
   * Set the device handle to be non-blocking.
   *
   * In non-blocking mode calls to hid_read() will return immediately with a value of 0 if there is no data to be read.
   *
   * In blocking mode, hid_read() will wait (block) until there is data to read before returning.
   *
   * Nonblocking can be turned on and off at any time.
   *
   * @param device   The device handle
   * @param nonblock 0 disables non-blocking, 1 enables non-blocking
   *
   * @return 0 on success, -1 on error
   */
  int hid_set_nonblocking(Pointer device, int nonblock);

  /**
   * Enumerate the HID Devices.
   *
   * This function returns a linked list of all the HID devices attached to the system which match vendor_id and product_id.
   *
   * If vendor_id is set to 0 then any vendor matches. If product_id is set to 0 then any product matches.
   *
   * If vendor_id and product_id are both set to 0, then all HID devices will be returned.
   *
   * @param vendor_id  The vendor ID
   * @param product_id The product ID
   *
   * @return A linked list of all discovered matching devices
   */
  HidDeviceInfoStructure hid_enumerate(short vendor_id, short product_id);

  /**
   * Free an enumeration linked list
   *
   * @param devs The device information pointer
   */
  void hid_free_enumeration(Pointer devs);

  /**
   * Open a HID device by its path name.
   *
   * The path name be determined by calling hid_enumerate(), or a platform-specific path name can be used (eg: "/dev/hidraw0" on Linux).
   *
   * @param path The path name
   *
   * @return The pointer if successful or null
   */
  Pointer hid_open_path(String path);
}