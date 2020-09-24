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

/**
 * Factory to provide the following to API consumers:
 * <ul>
 * <li>Access to configured HID services</li>
 * </ul>
 *
 * @since 0.0.1
 */
public class HidManager {

  private static final Object servicesLock = new Object();

  private static HidServices hidServices = null;

  /**
   * Simple service provider providing generally safe defaults. If you find you are experiencing problems, particularly
   * with constrained devices, consider exploring the {@link HidServicesSpecification} options.
   *
   * @return A single instance of the HID services using the default specification
   */
  public static HidServices getHidServices() throws HidException {

    synchronized (servicesLock) {
      if (null == hidServices) {
        // Use defaults
        hidServices = getHidServices(new HidServicesSpecification());
      }
    }

    return hidServices;

  }

  /**
   * Fully configurable service provider
   *
   * @param hidServicesSpecification Provides various parameters for configuring HID services
   * @return A single instance of the HID services using specified parameters
   * @since 0.5.0
   */
  public static HidServices getHidServices(HidServicesSpecification hidServicesSpecification) throws HidException {

    synchronized (servicesLock) {
      if (null == hidServices) {
        hidServices = new HidServices(hidServicesSpecification);
      }
    }

    return hidServices;

  }

}
