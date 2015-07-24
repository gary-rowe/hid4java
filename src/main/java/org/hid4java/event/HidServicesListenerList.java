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

package org.hid4java.event;

import org.hid4java.HidDevice;
import org.hid4java.HidServicesListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * <p>HID services listener list</p>
 *
 * @since 0.0.1
 *  
 */
public class HidServicesListenerList {

  /**
   * The list with registered listeners
   */
  private final List<HidServicesListener> listeners = Collections.synchronizedList(new ArrayList<HidServicesListener>());

  private final ExecutorService executorService = Executors.newFixedThreadPool(
    3, new ThreadFactory() {
      @Override
      public Thread newThread(Runnable runnable) {
        Thread thread = Executors.defaultThreadFactory().newThread(runnable);
        thread.setName("hid4java event worker");
        thread.setDaemon(true);
        return thread;
      }
    });

  /**
   * @param listener The listener to add
   */
  public final void add(final HidServicesListener listener) {
    if (this.listeners.contains(listener)) {
      return;
    }
    this.listeners.add(listener);
  }

  /**
   * @param listener The listener to remove
   */
  public final void remove(final HidServicesListener listener) {
    this.listeners.remove(listener);
  }

  /**
   * Removes all listeners
   */
  public final void clear() {
    this.listeners.clear();
  }

  /**
   * @return The listeners list
   */
  protected final List<HidServicesListener> getListeners() {
    return this.listeners;
  }

  /**
   * Returns an array with the currently registered listeners.
   * The returned array is detached from the internal list of registered listeners.
   *
   * @return Array with registered listeners.
   */
  public HidServicesListener[] toArray() {
    return getListeners().toArray(new HidServicesListener[getListeners().size()]);
  }

  /**
   * <p>Fire the HID device attached event</p>
   *
   * @param hidDevice The device that was attached
   */
  public void fireHidDeviceAttached(final HidDevice hidDevice) {

    // Broadcast on a different thread
    executorService.submit(
      new Runnable() {
        @Override
        public void run() {

          HidServicesEvent event = new HidServicesEvent(hidDevice);

          for (final HidServicesListener listener : toArray()) {
            listener.hidDeviceAttached(event);
          }

        }
      });

  }

  /**
   * <p>Fire the HID device detached event</p>
   *
   * @param hidDevice The device that was detached
   */
  public void fireHidDeviceDetached(final HidDevice hidDevice) {

    // Broadcast on a different thread
    executorService.submit(
      new Runnable() {
        @Override
        public void run() {

          HidServicesEvent event = new HidServicesEvent(hidDevice);

          for (final HidServicesListener listener : toArray()) {
            listener.hidDeviceDetached(event);
          }

        }
      });

  }

  /**
   * <p>Fire the HID failure event</p>
   *
   * @param hidDevice The device that caused the error if known
   */
  public void fireHidFailure(final HidDevice hidDevice) {

    // Broadcast on a different thread
    executorService.submit(
      new Runnable() {
        @Override
        public void run() {

          HidServicesEvent event = new HidServicesEvent(hidDevice);

          for (final HidServicesListener listener : toArray()) {
            listener.hidFailure(event);
          }

        }
      });

  }
}

