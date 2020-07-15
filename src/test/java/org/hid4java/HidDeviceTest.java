package org.hid4java;

import com.sun.jna.WString;
import org.hid4java.jna.HidDeviceInfoStructure;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class HidDeviceTest {

  HidDeviceInfoStructure mockStructure = new HidDeviceInfoStructure();

  @Test
  void isVidPidSerial_UnsignedShort_Simple() {

    // Arrange
    mockStructure.vendor_id = 0x01;
    mockStructure.product_id = 0x02;
    mockStructure.serial_number = new WString("1234");

    // Act
    HidDevice testObject = new HidDevice(mockStructure, null);

    // Assert
    assertTrue(testObject.isVidPidSerial(0x01, 0x02, "1234"));

  }

  @Test
  void isVidPidSerial_UnsignedShort_Overflow() {

    // Arrange
    mockStructure.vendor_id = 0xffff8001;
    mockStructure.product_id = 0xffff8002;
    mockStructure.serial_number = new WString("1234");

    // Act
    HidDevice testObject = new HidDevice(mockStructure, null);

    // Assert
    assertTrue(testObject.isVidPidSerial(0x8001, 0x8002, "1234"));

  }


}