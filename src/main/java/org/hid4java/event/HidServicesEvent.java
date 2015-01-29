package org.hid4java.event;

import org.hid4java.HidDevice;


/**
 * <p>Event to provide the following to API consumers:</p>
 * <ul>
 * <li>Provision of HID device information</li>
 * </ul>
 *
 * @since 0.0.1
 *  
 */
public class HidServicesEvent {

  private final HidDevice hidDevice;

  /**
   * @param device The HidDevice involved in the event
   */
  public HidServicesEvent(HidDevice device) {
    hidDevice = device;
  }

  /**
   * @return The associated HidDevice
   */
  public HidDevice getHidDevice() {
    return hidDevice;
  }

  @Override
  public String toString() {
    return "HidServicesEvent{" +
      "hidDevice=" + hidDevice +
      '}';
  }
}
