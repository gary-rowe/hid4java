/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2020 Gary Rowe
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

package org.hid4java.examples;

import org.hid4java.*;

/**
 * <p>Demonstrate the USB HID interface using a FIDO2 USB device</p>
 *
 * @since 0.8.0
 *
 */
public class Fido2AuthenticationExample extends BaseExample {

  public static void main(String[] args) throws HidException {

    Fido2AuthenticationExample example = new Fido2AuthenticationExample();
    example.executeExample();

  }

  private void executeExample() throws HidException {

    printPlatform();

    // Configure to use custom specification
    HidServicesSpecification hidServicesSpecification = new HidServicesSpecification();

    // Get HID services using custom specification
    HidServices hidServices = HidManager.getHidServices(hidServicesSpecification);
    hidServices.addHidServicesListener(this);

    // Enumerate devices looking for 0xf1d0 usage page
    for (HidDevice hidDevice : hidServices.getAttachedHidDevices()) {
      if (hidDevice.getUsagePage() == 0xfffff1d0) {
        System.out.println(ANSI_GREEN + hidDevice.getManufacturer() + ANSI_RESET);
      }
    }

    waitAndShutdown(hidServices);

  }

}
