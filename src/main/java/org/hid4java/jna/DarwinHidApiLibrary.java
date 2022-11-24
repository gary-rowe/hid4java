package org.hid4java.jna;

import com.sun.jna.Native;
import com.sun.jna.WString;

public interface DarwinHidApiLibrary extends HidrawHidApiLibrary {

  DarwinHidApiLibrary INSTANCE = Native.load("hidapi", DarwinHidApiLibrary.class);

  /**
   * Changes the behavior of all further calls to {@link #hid_open(short, short, WString)} or {@link #hid_open_path(String)}.
   * <p>
   * All devices opened by HIDAPI with {@link #hid_open(short, short, WString)} or {@link #hid_open_path(String)}
   * are opened in exclusive mode per default.
   * <p>
   * Calling this function before {@link #hid_init()} or after {@link #hid_exit()} has no effect.
   *
   * @since hidapi 0.12.0
   * @param openExclusive When set to 0 - all further devices will be opened in non-exclusive mode.
   *                      Otherwise - all further devices will be opened in exclusive mode.
   */
  void hid_darwin_set_open_exclusive(int openExclusive);

}
