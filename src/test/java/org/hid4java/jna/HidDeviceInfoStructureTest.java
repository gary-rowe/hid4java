package org.hid4java.jna;

import com.sun.jna.WString;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HidDeviceInfoStructureTest {

  @Test
  void getFieldOrder() {

    // Arrange
    HidDeviceInfoStructure testObject = new HidDeviceInfoStructure();
    List<String> expectedFieldOrder = Arrays.asList(
      "path",
      "vendor_id",
      "product_id",
      "serial_number",
      "release_number",
      "manufacturer_string",
      "product_string",
      "usage_page",
      "usage",
      "interface_number",
      "next"
    );

    // Act
    List<String> actualFieldOrder = testObject.getFieldOrder();

    // Assert
    assertEquals(11, actualFieldOrder.size());
    assertEquals(expectedFieldOrder, actualFieldOrder);

  }

  @Test
  void show() {

    // Arrange
    HidDeviceInfoStructure testObject = new HidDeviceInfoStructure();
    testObject.path = "path";
    testObject.vendor_id = 0x01;
    testObject.product_id = 0x02;
    testObject.serial_number = new WString("serial");
    testObject.release_number = 0x03;
    testObject.manufacturer_string = new WString("manufacturer");
    testObject.product_string = new WString("product");
    testObject.usage_page = 0x04;
    testObject.usage = 0x05;
    testObject.interface_number = 0x06;
    testObject.next = null;

    String expectedShow = "HidDevice\n" +
      "\tpath:path>\n" +
      "\tvendor_id: 1\n" +
      "\tproduct_id: 2\n" +
      "\tserial_number: serial>\n" +
      "\trelease_number: 3\n" +
      "\tmanufacturer_string: manufacturer>\n" +
      "\tproduct_string: product>\n" +
      "\tusage_page: 4\n" +
      "\tusage: 5\n" +
      "\tinterface_number: 6\n";

    // Act
    String actualShow = testObject.show();

    // Assert
    assertEquals(expectedShow, actualShow);

  }
}