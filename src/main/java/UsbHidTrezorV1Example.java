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

import org.hid4java.*;
import org.hid4java.event.HidServicesEvent;

import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * <p>Demonstrate the USB HID interface using a production Bitcoin Trezor</p>
 *
 * @since 0.0.1
 *  
 */
public class UsbHidTrezorV1Example implements HidServicesListener {

  static final int PACKET_LENGTH = 64;
  private HidServices hidServices;

  public static void main(String[] args) throws HidException {

    UsbHidTrezorV1Example example = new UsbHidTrezorV1Example();
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

    // Open the Trezor device by Vendor ID and Product ID with wildcard serial number
    HidDevice trezor = hidServices.getHidDevice(0x534c, 0x01, null);
    if (trezor != null) {
      // Device is already attached so send message
      sendInitialise(trezor);
    } else {
      System.out.println("Waiting for Trezor attach...");
    }
    // Stop the main thread to demonstrate attach and detach events
    sleepUninterruptibly(5, TimeUnit.SECONDS);

    if (trezor != null && trezor.isOpen()) {
      trezor.close();
    }

    System.exit(0);
  }

  @Override
  public void hidDeviceAttached(HidServicesEvent event) {

    System.out.println("Device attached: " + event);

    if (event.getHidDevice().getVendorId() == 0x534c &&
      event.getHidDevice().getProductId() == 0x01) {

      // Open the Trezor device by Vendor ID and Product ID with wildcard serial number
      HidDevice trezor = hidServices.getHidDevice(0x534c, 0x01, null);
      if (trezor != null) {
        sendInitialise(trezor);
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

  private void sendInitialise(HidDevice trezor) {

    // Send the Initialise message
    byte[] message = new byte[64];
    message[0] = 0x3f;
    message[1] = 0x23;
    message[2] = 0x23;

    int val = trezor.write(message, PACKET_LENGTH, (byte) 0);
    if (val != -1) {
      System.out.println("> [" + val + "]");
    } else {
      System.err.println(trezor.getLastErrorMessage());
    }

    // Prepare to read a single data packet
    boolean moreData = true;
    while (moreData) {
      byte data[] = new byte[PACKET_LENGTH];
      // This method will now block for 500ms or until data is read
      val = trezor.read(data, 500);
      switch (val) {
        case -1:
          System.err.println(trezor.getLastErrorMessage());
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
