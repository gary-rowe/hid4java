package org.hid4java.jna;

/**
 * <p>[Pattern] to provide the following to {@link Object}:</p>
 * <ul>
 * <li></li>
 * </ul>
 * <p>Example:</p>
 * <pre>
 * </pre>
 *
 * @since 0.0.1
 *  
 */

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.WString;

/**
 * <p>JNA library interface to act as the proxy for the underlying native library</p>
 * <p>This approach removes the need for any JNI or native code</p>
 */
public interface HidApiLibrary extends Library {

  HidApiLibrary INSTANCE = (HidApiLibrary) Native.loadLibrary("hidapi", HidApiLibrary.class);

  /**
   * <p>Initialize the HIDAPI library.</p>
   * <p>This function initializes the HIDAPI library. Calling it is not strictly necessary,
   * as it will be called automatically by hid_enumerate() and any of the hid_open_*() functions
   * if it is needed. This function should be called at the beginning of execution however,
   * if there is a chance of HIDAPI handles being opened by different threads simultaneously.</p>
   */
  void hid_init();

  /**
   * <p>Finalize the HIDAPI library.</p>
   *
   * <p>This function frees all of the static data associated with HIDAPI. It should be called
   * at the end of execution to avoid memory leaks.</p>
   */
  void hid_exit();

  Pointer hid_open(short vendor_id, short product_id, WString serial_number);

  void hid_close(Pointer device);

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

  int hid_write(Pointer device, WideStringBuffer.ByReference data, int len);

  int hid_get_feature_report(Pointer device, WideStringBuffer.ByReference data, int length);

  int hid_send_feature_report(Pointer device, WideStringBuffer.ByReference data, int length);

  int hid_get_indexed_string(Pointer device, int idx, WideStringBuffer.ByReference string, int len);

  int hid_get_manufacturer_string(Pointer device, WideStringBuffer.ByReference str, int len);

  int hid_get_product_string(Pointer device, WideStringBuffer.ByReference str, int len);

  int hid_get_serial_number_string(Pointer device, WideStringBuffer.ByReference str, int len);

  int hid_set_nonblocking(Pointer device, int nonblock);

  HidDeviceInfoStructure hid_enumerate(short vendor_id, short product_id);

  void hid_free_enumeration(Pointer devs);

  Pointer hid_open_path(String path);
}