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

package org.hid4java.jna;

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

/**
 * <p>Wrapper for a wide character (WCHAR) structure</p>
 */
public class WideStringBuffer extends Structure implements Structure.ByReference {

  public byte[] buffer = null;

  public WideStringBuffer(int len) {
    buffer = new byte[len];
  }

  public WideStringBuffer(byte[] bytes) {
    buffer = bytes;
  }

  @Override
  protected List getFieldOrder() {
    return Arrays.asList("buffer");
  }

  /**
   * <p>hidapi uses wchar_t which is written l i k e   t h i s (with '\0' in between)</p>
   */
  public String toString() {
    String str = "";
    for (int i = 0; i < buffer.length && buffer[i] != 0; i += 2)
      str += (char) (buffer[i] | buffer[i + 1] << 8);
    return str;
  }

}
