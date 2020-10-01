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
 * Specification to provide the following to API consumers:
 * <ul>
 * <li>Flexible configuration of HID services parameters</li>
 * </ul>
 *
 * @since 0.5.0
 */
public class HidServicesSpecification {

  private ScanMode scanMode = ScanMode.SCAN_AT_FIXED_INTERVAL;
  private boolean autoShutdown = true;
  private int scanInterval = 500;
  private int pauseInterval = 5000;
  private boolean autoStart = true;
  private boolean autoDataRead = false;
  private int dataReadInterval = 500;

  public ScanMode getScanMode() {
    return scanMode;
  }

  /**
   * @param scanMode The scan mode to use to facilitate attach/detach events
   */
  public void setScanMode(ScanMode scanMode) {
    this.scanMode = scanMode;
  }

  public int getScanInterval() {
    return scanInterval;
  }

  /**
   * @param scanInterval The interval in milliseconds between device enumeration scans
   */
  public void setScanInterval(int scanInterval) {
    // Verify parameters
    if (scanInterval < 0) {
      throw new IllegalArgumentException("'scanInterval' must be greater than or equal to zero.");
    }
    this.scanInterval = scanInterval;
  }

  public int getPauseInterval() {
    return pauseInterval;
  }

  /**
   * @param pauseInterval The interval in milliseconds where device enumeration is paused (if scan mode supports pausing)
   *
   */
  public void setPauseInterval(int pauseInterval) {
    if (pauseInterval < 0) {
      throw new IllegalArgumentException("'pauseInterval' must be greater than or equal to zero.");
    }
    this.pauseInterval = pauseInterval;
  }

  public boolean isAutoShutdown() {

    return autoShutdown;
  }

  /**
   * @param autoShutdown True if a shutdown hook should be set to close the API automatically (recommended)
   *
   */
  public void setAutoShutdown(boolean autoShutdown) {
    this.autoShutdown = autoShutdown;
  }


  public boolean isAutoStart() {
    return autoStart;
  }

  /**
   * @param autoStart True if the HidServices should start before any listeners are registered (default is backwards compatible to 0.6.0 and below)
   */
  public void setAutoStart(boolean autoStart) {
    this.autoStart = autoStart;
  }

  public boolean isAutoDataRead() {
    return autoDataRead;
  }

  /**
   * @param autoDataRead True if device input buffer should be automatically checked and read
   * @since 0.8.0
   */
  public void setAutoDataRead(boolean autoDataRead) {
    this.autoDataRead = autoDataRead;
  }

  public int getDataReadInterval() {
    return dataReadInterval;
  }

  /**
   *
   * @param dataReadInterval The interval in milliseconds between attempts to read device input buffer
   */
  public void setDataReadInterval(int dataReadInterval) {
    this.dataReadInterval = dataReadInterval;
  }
}
