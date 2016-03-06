/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2016 Gary Rowe
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

package org.hid4java;import org.hid4java.event.HidServicesEvent;

import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * <p>Demonstrate the USB HID interface using a production Bitcoin Trezor</p>
 *
 * @since 0.0.1
 *  
 */
public class UsbHidDeviceExample implements HidServicesListener {

  private static final Integer VENDOR_ID = 0x534c;
  private static final Integer PRODUCT_ID = 0x01;
  private static final int PACKET_LENGTH = 64;
  private HidServices hidServices;

  public static void main(String[] args) throws HidException {

    UsbHidDeviceExample example = new UsbHidDeviceExample();
    example.executeExample();

  }

  public void executeExample() throws HidException {

    System.out.println("Loading hidapi...");

    // Get HID services
    hidServices = HidManager.getHidServices();
    hidServices.addHidServicesListener(this);

    System.out.println("Enumerating attached devices...");

    // Provide a list of attached devices
    for (HidDevice hidDevice : hidServices.getAttachedHidDevices()) {
      System.out.println(hidDevice);
    }

    // Open the device device by Vendor ID and Product ID with wildcard serial number
    HidDevice hidDevice = hidServices.getHidDevice(VENDOR_ID, PRODUCT_ID, null);
    if (hidDevice != null) {
      // Consider overriding dropReportIdZero on Windows
      // if you see "The parameter is incorrect"
      // HidApi.dropReportIdZero = true;

      // Device is already attached so send message
      sendMessage(hidDevice);
    } else {
      System.out.println("Waiting for device attach...");
    }
    // Stop the main thread to demonstrate attach and detach events
    sleepUninterruptibly(5, TimeUnit.SECONDS);

    if (hidDevice != null && hidDevice.isOpen()) {
      hidDevice.close();
    }

    System.exit(0);
  }

  @Override
  public void hidDeviceAttached(HidServicesEvent event) {

    System.out.println("Device attached: " + event);

    if (event.getHidDevice().getVendorId() == VENDOR_ID &&
      event.getHidDevice().getProductId() == PRODUCT_ID) {

      // Open the Trezor device by Vendor ID and Product ID with wildcard serial number
      HidDevice trezor = hidServices.getHidDevice(VENDOR_ID, PRODUCT_ID, null);
      if (trezor != null) {
        sendMessage(trezor);
      }

    }

  }

  @Override
  public void hidDeviceDetached(HidServicesEvent event) {

    System.err.println("Device detached: " + event);

  }

  @Override
  public void hidFailure(HidServicesEvent event) {

    System.err.println("HID failure: " + event);

  }

  private void sendMessage(HidDevice hidDevice) {

    // Send the Initialise message
    byte[] message = new byte[PACKET_LENGTH];
    message[0] = 0x3f; // USB: Payload 63 bytes
    message[1] = 0x23; // Device: '#'
    message[2] = 0x23; // Device: '#'

    int val = hidDevice.write(message, PACKET_LENGTH, (byte) 0x00);
    if (val != -1) {
      System.out.println("> [" + val + "]");
    } else {
      System.err.println(hidDevice.getLastErrorMessage());
    }

    // Prepare to read a single data packet
    boolean moreData = true;
    while (moreData) {
      byte data[] = new byte[PACKET_LENGTH];
      // This method will now block for 500ms or until data is read
      val = hidDevice.read(data, 500);
      switch (val) {
        case -1:
          System.err.println(hidDevice.getLastErrorMessage());
          break;
        case 0:
          moreData = false;
          break;
        default:
          System.out.print("< [");
          for (byte b : data) {
            System.out.printf(" %02x", b);
          }
          System.out.println("]");
          break;
      }
    }
  }

  /**
   * Invokes {@code unit.}{@link java.util.concurrent.TimeUnit#sleep(long) sleep(sleepFor)}
   * uninterruptibly.
   */
  public static void sleepUninterruptibly(long sleepFor, TimeUnit unit) {
    boolean interrupted = false;
    try {
      long remainingNanos = unit.toNanos(sleepFor);
      long end = System.nanoTime() + remainingNanos;
      while (true) {
        try {
          // TimeUnit.sleep() treats negative timeouts just like zero.
          NANOSECONDS.sleep(remainingNanos);
          return;
        } catch (InterruptedException e) {
          interrupted = true;
          remainingNanos = end - System.nanoTime();
        }
      }
    } finally {
      if (interrupted) {
        Thread.currentThread().interrupt();
      }
    }
  }

}
