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
import org.hid4java.event.HidServicesEvent;

/**
 * <p>Demonstrate the USB HID interface using a Satoshi Labs Trezor</p>
 *
 * @since 0.0.1
 *
 */
public class UsbHidDeviceExample extends BaseExample {

  private static final Integer VENDOR_ID = 0x534c;
  private static final Integer PRODUCT_ID = 0x01;
  private static final int PACKET_LENGTH = 64;
  private static final String SERIAL_NUMBER = null;

  public static void main(String[] args) throws HidException {

    UsbHidDeviceExample example = new UsbHidDeviceExample();
    example.executeExample();

  }

  private void executeExample() throws HidException {

    printPlatform();

    // Configure to use custom specification
    HidServicesSpecification hidServicesSpecification = new HidServicesSpecification();
    // Use the fixed interval with pause after write to allow device to process data
    // without being interrupted by enumeration requests
    hidServicesSpecification.setScanMode(ScanMode.SCAN_AT_FIXED_INTERVAL_WITH_PAUSE_AFTER_WRITE);

    // Get HID services using custom specification
    HidServices hidServices = HidManager.getHidServices(hidServicesSpecification);
    hidServices.addHidServicesListener(this);

    System.out.println("Enumerating attached devices...");

    // Provide a list of attached devices
    for (HidDevice hidDevice : hidServices.getAttachedHidDevices()) {
      System.out.println(hidDevice);
    }

    // Open the device device by Vendor ID and Product ID with wildcard serial number
    HidDevice hidDevice = hidServices.getHidDevice(VENDOR_ID, PRODUCT_ID, SERIAL_NUMBER);
    if (hidDevice != null && hidDevice.getUsagePage() == 0xffffff00) {
      // Device is already attached and successfully opened so send message
      System.out.println(ANSI_GREEN+ "Found required interface on device..." + ANSI_RESET);
      sendMessage(hidDevice);
    } else {
      System.out.println(ANSI_YELLOW + "Required device not found." + ANSI_RESET);
    }

    waitAndShutdown(hidServices);

  }

  @Override
  public void hidDeviceAttached(HidServicesEvent event) {

    super.hidDeviceAttached(event);

    // Add serial number when more than one device with the same
    // vendor ID and product ID will be present at the same time
    // Add usage page when more than one interface is available (e.g. FIDO)
    if (event.getHidDevice().isVidPidSerial(VENDOR_ID, PRODUCT_ID, null)
      && event.getHidDevice().getUsagePage() == 0xffffff00) {
      sendMessage(event.getHidDevice());
    }

  }

  private void sendMessage(HidDevice hidDevice) {

    // Ensure device is open after an attach/detach event
    if (!hidDevice.isOpen()) {
      hidDevice.open();
    }

    System.out.println("Device is open, sending INITIALISE...");

    // Send the Initialise message
    byte[] message = new byte[PACKET_LENGTH];
    message[0] = 0x3f; // USB: Payload 63 bytes
    message[1] = 0x23; // Device: '#'
    message[2] = 0x23; // Device: '#'
    message[3] = 0x00; // INITIALISE

    int val = hidDevice.write(message, PACKET_LENGTH, (byte) 0x00);
    if (val >= 0) {
      System.out.println(ANSI_CYAN + "> [" + val + "]" + ANSI_RESET);
    } else {
      System.out.println(ANSI_RED + hidDevice.getLastErrorMessage() + ANSI_RESET);
    }

    // Prepare to read a single data packet
    boolean moreData = true;
    while (moreData) {
      byte[] data = new byte[PACKET_LENGTH];
      // This method will now block for 500ms or until data is read
      val = hidDevice.read(data, 500);
      switch (val) {
        case -1:
          System.out.println(ANSI_RED + hidDevice.getLastErrorMessage() + ANSI_RESET);
          break;
        case 0:
          moreData = false;
          break;
        default:
          System.out.print(ANSI_PURPLE + "< [");
          for (byte b : data) {
            System.out.printf(" %02x", b);
          }
          System.out.println("]" + ANSI_RESET);
          break;
      }
    }
  }

}
