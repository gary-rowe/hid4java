package org.hid4java.jna;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.WString;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class provides a management layer for thread-sensitive operations within the HIDAPI library on macOS. Due to
 * threading constraints in the underlying native library, certain functions must be called from the same thread. This
 * includes {@code hid_init()} and {@code hid_exit()}, which must be invoked on a consistent thread to avoid runtime
 * failures. See <a href="https://github.com/libusb/hidapi/issues/666">hidapi #666</a>
 * <br>
 * Additionally, certain functions such as {@code hid_open()}, {@code hid_enumerate()}, and {@code hid_open_path()} may
 * implicitly call {@code hid_init()} if it has not been previously called. To prevent potential crashes due to these
 * implicit calls, these functions are also managed in the same way.
 * <br>
 * This class utilizes a single-thread executor to wrap the thread-sensitive calls onto a single thread. Despite using
 * an executor, the operations are executed in a blocking manner, ensuring they complete before returning.
 */
public class DarwinHidApiLibrary implements HidApiLibrary {
  // WARNING:  This is NOT a JNA interface, but rather a wrapped instance
  public static HidApiLibrary INSTANCE = new DarwinHidApiLibrary();
  private static final RealDarwinHidApiLibrary REAL_INSTANCE = Native.load("hidapi", RealDarwinHidApiLibrary.class);
  private static final ExecutorService executor = Executors.newSingleThreadExecutor();

  private interface RealDarwinHidApiLibrary extends HidrawHidApiLibrary {
    /**
     * Changes the behavior of all further calls to {@link #hid_open(short, short, WString)} or {@link #hid_open_path(String)}.
     * <br>
     * All devices opened by HIDAPI with {@link #hid_open(short, short, WString)} or {@link #hid_open_path(String)}
     * are opened in exclusive mode per default.
     * <br>
     * Calling this function before {@link #hid_init()} or after {@link #hid_exit()} has no effect.
     *
     * @since hidapi 0.12.0
     * @param openExclusive When set to 0 - all further devices will be opened in non-exclusive mode.
     *                      Otherwise - all further devices will be opened in exclusive mode.
     */
    void hid_darwin_set_open_exclusive(int openExclusive);
  }
  @Override
  public void hid_init() {
    try {
      executor.submit(REAL_INSTANCE::hid_init).get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void hid_exit() {
    try {
      executor.submit(REAL_INSTANCE::hid_exit).get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Pointer hid_open(short vendor_id, short product_id, WString serial_number) {
    try {
      return executor.submit(() -> REAL_INSTANCE.hid_open(vendor_id, product_id, serial_number)).get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void hid_close(Pointer device) {
    REAL_INSTANCE.hid_close(device);
  }

  @Override
  public Pointer hid_error(Pointer device) {
    return REAL_INSTANCE.hid_error(device);
  }

  @Override
  public int hid_read(Pointer device, WideStringBuffer.ByReference bytes, int length) {
    return REAL_INSTANCE.hid_read(device, bytes, length);
  }

  @Override
  public int hid_read_timeout(Pointer device, WideStringBuffer.ByReference bytes, int length, int timeout) {
    return REAL_INSTANCE.hid_read_timeout(device, bytes, length, timeout);
  }

  @Override
  public int hid_write(Pointer device, WideStringBuffer.ByReference data, int len) {
    return REAL_INSTANCE.hid_write(device, data, len);
  }

  @Override
  public int hid_get_feature_report(Pointer device, WideStringBuffer.ByReference data, int length) {
    return REAL_INSTANCE.hid_get_feature_report(device, data, length);
  }

  @Override
  public int hid_send_feature_report(Pointer device, WideStringBuffer.ByReference data, int length) {
    return REAL_INSTANCE.hid_send_feature_report(device, data, length);
  }

  @Override
  public int hid_get_indexed_string(Pointer device, int idx, WideStringBuffer.ByReference string, int len) {
    return REAL_INSTANCE.hid_get_indexed_string(device, idx, string, len);
  }

  @Override
  public int hid_get_report_descriptor(Pointer device, byte[] buffer, int size) {
    return REAL_INSTANCE.hid_get_report_descriptor(device, buffer, size);
  }

  @Override
  public int hid_get_manufacturer_string(Pointer device, WideStringBuffer.ByReference str, int len) {
    return REAL_INSTANCE.hid_get_manufacturer_string(device, str, len);
  }

  @Override
  public int hid_get_product_string(Pointer device, WideStringBuffer.ByReference str, int len) {
    return REAL_INSTANCE.hid_get_product_string(device, str, len);
  }

  @Override
  public int hid_get_serial_number_string(Pointer device, WideStringBuffer.ByReference str, int len) {
    return REAL_INSTANCE.hid_get_serial_number_string(device, str, len);
  }

  @Override
  public int hid_set_nonblocking(Pointer device, int nonblock) {
    return REAL_INSTANCE.hid_set_nonblocking(device, nonblock);
  }

  @Override
  public HidDeviceInfoStructure hid_enumerate(short vendor_id, short product_id) {
    try {
      return executor.submit(() -> REAL_INSTANCE.hid_enumerate(vendor_id, product_id)).get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void hid_free_enumeration(Pointer devs) {
    REAL_INSTANCE.hid_free_enumeration(devs);
  }

  @Override
  public Pointer hid_open_path(String path) {
    try {
      return executor.submit(() -> REAL_INSTANCE.hid_open_path(path)).get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String hid_version_str() {
    return REAL_INSTANCE.hid_version_str();
  }

  public void hid_darwin_set_open_exclusive(int openExclusive) {
    REAL_INSTANCE.hid_darwin_set_open_exclusive(openExclusive);
  }
}
