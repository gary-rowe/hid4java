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

package org.hid4java;

/**
 * Provide a collection of different device enumeration scanning modes so that
 * device attach/detach events can be generated.
 */
public enum ScanMode {

  /**
   * Equivalent to scan interval of zero.
   */
  NO_SCAN,
  /**
   * Trigger continuous scan at given interval.
   */
  SCAN_AT_FIXED_INTERVAL,
  /**
   * Trigger continuous scan at given interval but introduce a pause after a write
   * operation to allow the device time to process data without having to respond
   * to further enumeration requests.
   *
   * This can be a useful strategy for handling devices with constrained processing
   * power and/or limited USB stacks.
   *
   * Note this will affect the time to generate a device attach/detach event since
   * scanning will be paused.
   */
  SCAN_AT_FIXED_INTERVAL_WITH_PAUSE_AFTER_WRITE,

}
