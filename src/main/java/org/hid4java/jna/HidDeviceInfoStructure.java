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

import com.sun.jna.Structure;
import com.sun.jna.WString;

import java.util.Arrays;
import java.util.List;

/**
 * Value object to provide HID device information
 * @since 0.1.0
 */
public class HidDeviceInfoStructure extends Structure implements Structure.ByReference {

  /**
   * USB path
   */
  public String path;

  /**
   * Vendor ID
   */
  public short vendor_id;
  /**
   * Produce ID
   */
  public short product_id;
  /**
   * Serial number
   */
  public WString serial_number;

  /**
   * Release number
   */
  public short release_number;
  /**
   * Manufacturer string
   */
  public WString manufacturer_string;

  /**
   * Usage Page for this Device/Interface (Windows/Mac only)
   */
  public WString product_string;
  /**
   * Usage for this Device/Interface (Windows/Mac only)
   */
  public short usage_page;

  /**
   * Usage number
   */
  public short usage;
  /**
   * Interface number
   */
  public int interface_number;

  /**
   * Reference to next device
   */
  // Consider public HidDeviceInfo.ByReference next;
  public HidDeviceInfoStructure next;

  public HidDeviceInfoStructure next() {
    return next;
  }

  public boolean hasNext() {
    return next != null;
  }

  @Override
  protected List<String> getFieldOrder() {

    // If this precise order is not specified you get "SIGSEGV (0xb)"
    return Arrays.asList(
      "path",
      "vendor_id",
      "product_id",
      "serial_number",
      "release_number",
      "manufacturer_string",
      "product_string",
      "usage_page",
      "usage",
      "interface_number",
      "next"
    );

  }

  /**
   * @return A string representation of the attached device
   */
  public String show() {
    HidDeviceInfoStructure u = this;
    String str = "HidDevice\n";
    str += "\tpath:" + u.path + ">\n";
    str += "\tvendor_id: " + Integer.toHexString(u.vendor_id) + "\n";
    str += "\tproduct_id: " + Integer.toHexString(u.product_id) + "\n";
    str += "\tserial_number: " + u.serial_number + ">\n";
    str += "\trelease_number: " + u.release_number + "\n";
    str += "\tmanufacturer_string: " + u.manufacturer_string + ">\n";
    str += "\tproduct_string: " + u.product_string + ">\n";
    str += "\tusage_page: " + u.usage_page + "\n";
    str += "\tusage: " + u.usage + "\n";
    str += "\tinterface_number: " + u.interface_number + "\n";
    return str;
  }
}

