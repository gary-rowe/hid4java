package org.hid4java;

import com.sun.jna.WString;
import org.hid4java.jna.HidDeviceInfoStructure;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    HidDevice testObject = new HidDevice(mockStructure, null, new HidServicesSpecification());

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
    HidDevice testObject = new HidDevice(mockStructure, null, new HidServicesSpecification());

    // Assert
    assertTrue(testObject.isVidPidSerial(0x8001, 0x8002, "1234"));

  }

  @Test
  void verifyFields() {

    // Arrange
    mockStructure.path="path";
    mockStructure.vendor_id=1;
    mockStructure.product_id=2;
    mockStructure.serial_number=new WString("serial");
    mockStructure.release_number=3;
    mockStructure.manufacturer_string=new WString("manufacturer");
    mockStructure.product_string = new WString("product");
    mockStructure.usage_page=4;
    mockStructure.usage=5;
    mockStructure.interface_number=6;
    mockStructure.next=null;

    // Act
    HidDevice testObject = new HidDevice(mockStructure, null, new HidServicesSpecification());

    // Assert
    assertEquals("path", testObject.getPath());
    assertEquals(1, testObject.getVendorId());
    assertEquals(2, testObject.getProductId());
    assertEquals("serial", testObject.getSerialNumber());
    assertEquals(3, testObject.getReleaseNumber());
    assertEquals("manufacturer", testObject.getManufacturer());
    assertEquals("product", testObject.getProduct());
    assertEquals(4,testObject.getUsagePage());
    assertEquals(5, testObject.getUsage());
    assertEquals(6, testObject.getInterfaceNumber());

  }

}