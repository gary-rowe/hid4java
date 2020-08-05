package org.hid4java.jna;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WideStringBufferTest {

  @Test
  void getFieldOrder() {

    // Arrange
    WideStringBuffer testObject = new WideStringBuffer(new byte[] {0x61, 0x00, 0x62, 0x00, 0x63, 0x00});

    // Act
    List<String> fieldOrder = testObject.getFieldOrder();

    // Assert
    assertEquals(1, fieldOrder.size());
    assertEquals("buffer", fieldOrder.get(0));

  }

  @Test
  void testToString() {

    // Arrange
    WideStringBuffer testObject = new WideStringBuffer(new byte[] {0x61, 0x00, 0x62, 0x00, 0x63, 0x00});

    // Act
    String wchar_t = testObject.toString();

    // Assert
    assertEquals("abc", wchar_t);

  }
}