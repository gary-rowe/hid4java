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

import com.sun.jna.Platform;
import org.hid4java.HidServices;
import org.hid4java.HidServicesListener;
import org.hid4java.event.HidServicesEvent;

import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

public abstract class BaseExample implements HidServicesListener {

  public static final String ANSI_RESET = "\u001B[0m";
  public static final String ANSI_BLACK = "\u001B[30m";
  public static final String ANSI_RED = "\u001B[31m";
  public static final String ANSI_GREEN = "\u001B[32m";
  public static final String ANSI_YELLOW = "\u001B[33m";
  public static final String ANSI_BLUE = "\u001B[34m";
  public static final String ANSI_PURPLE = "\u001B[35m";
  public static final String ANSI_CYAN = "\u001B[36m";
  public static final String ANSI_WHITE = "\u001B[37m";

  public void printPlatform() {

    // System info to assist with library detection
    System.out.println("Platform architecture: " + Platform.ARCH);
    System.out.println("Resource prefix: " + Platform.RESOURCE_PREFIX);
    System.out.println("Libusb activation: " + Platform.isLinux());

  }

  public void waitAndShutdown(HidServices hidServices) {

    System.out.printf(ANSI_YELLOW + "Waiting 30s to demonstrate attach/detach handling. Watch for slow response after write if configured.%n" + ANSI_RESET);

    // Stop the main thread to demonstrate attach and detach events
    sleepNoInterruption();

    // Shut down and rely on auto-shutdown hook to clear HidApi resources
    System.out.printf(ANSI_YELLOW + "Triggering shutdown...%n" + ANSI_RESET);
    hidServices.shutdown();
  }

  /**
   * Invokes {@code unit.}{@link TimeUnit#sleep(long) sleep(sleepFor)}
   * uninterruptibly.
   */
  public static void sleepNoInterruption() {
    boolean interrupted = false;
    try {
      long remainingNanos = TimeUnit.SECONDS.toNanos(5);
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

  @Override
  public void hidDeviceAttached(HidServicesEvent event) {

    System.out.println(ANSI_BLUE + "Device attached: " + event + ANSI_RESET);

  }

  @Override
  public void hidDeviceDetached(HidServicesEvent event) {

    System.out.println(ANSI_YELLOW + "Device detached: " + event + ANSI_RESET);

  }

  @Override
  public void hidFailure(HidServicesEvent event) {

    System.out.println(ANSI_RED + "HID failure: " + event + ANSI_RESET);

  }

  @Override
  public void hidDataReceived(HidServicesEvent event) {

    System.out.printf(ANSI_PURPLE + "Data received:%n");
    byte[] dataReceived = event.getDataReceived();
    System.out.printf("< [%02x]:", dataReceived.length);
    for (byte b : dataReceived) {
      System.out.printf(" %02x", b);
    }
    System.out.println(ANSI_RESET);

  }

}
