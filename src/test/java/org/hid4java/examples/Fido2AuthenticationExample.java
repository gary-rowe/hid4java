/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2020 Gary Rowe
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

package org.hid4java.examples;

import org.hid4java.*;
import org.hid4java.jna.HidApi;

import java.security.SecureRandom;

/*
 * Demonstrate the USB HID interface using a FIDO2 USB device
 *
 * @since 0.8.0
 */
public class Fido2AuthenticationExample extends BaseExample {

  // Authentication data flags
  private static byte CTAP_AUTHDATA_USER_PRESENT = 0x01;
  private static byte CTAP_AUTHDATA_USER_VERIFIED = 0x04;
  private static byte CTAP_AUTHDATA_ATT_CRED = 0x40;
  private static byte CTAP_AUTHDATA_EXT_DATA = (byte) 0x80;

  // CTAPHID command opcodes
  private static byte CTAP_CMD_PING = 0x01;
  private static byte CTAP_CMD_MSG = 0x03;
  private static byte CTAP_CMD_LOCK = 0x04;
  private static byte CTAP_CMD_INIT = 0x06;
  private static byte CTAP_CMD_WINK = 0x08;
  private static byte CTAP_CMD_CBOR = 0x10;
  private static byte CTAP_CMD_CANCEL = 0x11;
  private static byte CTAP_KEEPALIVE = 0x3b;
  private static byte CTAP_FRAME_INIT = (byte) 0x80;

  // CTAPHID CBOR command opcodes
  private static byte CTAP_CBOR_MAKECRED = 0x01;
  private static byte CTAP_CBOR_ASSERT = 0x02;
  private static byte CTAP_CBOR_GETINFO = 0x04;
  private static byte CTAP_CBOR_CLIENT_PIN = 0x06;
  private static byte CTAP_CBOR_RESET = 0x07;
  private static byte CTAP_CBOR_NEXT_ASSERT = 0x08;
  private static byte CTAP_CBOR_BIO_ENROLL_PRE = 0x40;
  private static byte CTAP_CBOR_CRED_MGMT_PRE = 0x41;

  // U2F command opcodes
  private static byte U2F_CMD_REGISTER = 0x01;
  private static byte U2F_CMD_AUTH = 0x02;

  // U2F command flags
  private static byte U2F_AUTH_SIGN = 0x03;
  private static byte U2F_AUTH_CHECK = 0x07;

  // ISO7816-4 status words
  private static int SW_CONDITIONS_NOT_SATISFIED = 0x6985;
  private static int SW_WRONG_DATA = 0x6a80;
  private static int SW_NO_ERROR = 0x9000;

  // HID Broadcast channel ID
  private static int CTAP_CID_BROADCAST = 0xffffffff;

  private static int CTAP_INIT_HEADER_LEN = 7;
  private static int CTAP_CONT_HEADER_LEN = 5;

  // Maximum length of a CTAP HID report in bytes
  private static int CTAP_MAX_REPORT_LEN = 64;

  // Minimum length of a CTAP HID report in bytes
  private static int CTAP_MIN_REPORT_LEN = CTAP_INIT_HEADER_LEN + 1;

  // Maximum message size in bytes
  private static int FIDO_MAXMSG = 2048;

  // CTAP capability bits (set means supported)
  private static byte FIDO_CAP_WINK = 0x01;
  private static byte FIDO_CAP_CBOR = 0x04;
  private static byte FIDO_CAP_NMSG = 0x08;

  // Supported COSE algorithms
  private static int COSE_ES256 = -7;
  private static int COSE_EDDSA = -8;
  private static int COSE_ECDH_ES256 = -25;
  private static int COSE_RS256 = -257;

  // Supported COSE types
  private static int COSE_KTY_OKP = 1;
  private static int COSE_KTY_EC2 = 2;
  private static int COSE_KTY_RSA = 3;

  // Supported curves
  private static int COSE_P256 = 1;
  private static int COSE_ED25519 = 6;

  // Supported extensions
  private static byte FIDO_EXT_HMAC_SECRET = 0x01;
  private static byte FIDO_EXT_CRED_PROTECT = 0x02;

  // Supported credential protection policies
  private static int FIDO_CRED_PROT_UV_OPTIONAL = 0x01;
  private static int FIDO_CRED_PROT_UV_OPTIONAL_WITH_ID = 0x02;
  private static int FIDO_CRED_PROT_UV_REQUIRED = 0x03;

  // Secure random source for nonce
  private static SecureRandom secureRandom = new SecureRandom();

  public static void main(String[] args) throws HidException {

    Fido2AuthenticationExample example = new Fido2AuthenticationExample();
    example.executeExample();

  }

  private void executeExample() throws HidException {

    printPlatform();

    HidApi.logTraffic = true;

    // Configure to use custom specification
    HidServicesSpecification hidServicesSpecification = new HidServicesSpecification();

    // Get HID services using custom specification
    HidServices hidServices = HidManager.getHidServices(hidServicesSpecification);
    hidServices.addHidServicesListener(this);

    // Enumerate devices looking for = 0xf1d0 usage page
    HidDevice fidoDevice = null;
    for (HidDevice hidDevice : hidServices.getAttachedHidDevices()) {
      if (hidDevice.getUsage() == 0x01 && hidDevice.getUsagePage() == 0xfffff1d0) {
        System.out.println(ANSI_GREEN + "Using FIDO device: " + hidDevice.getPath() + ANSI_RESET);
        fidoDevice = hidDevice;
        break;
      }
    }

    if (fidoDevice == null) {
      // Shut down and rely on auto-shutdown hook to clear HidApi resources
      System.out.println(ANSI_YELLOW + "No FIDO2 devices attached." + ANSI_RESET);
    } else {
      getFidoFeatures(fidoDevice);
    }

    waitAndShutdown(hidServices);

  }

  private byte[] generateNonce() {

    byte[] nonce = new byte[8];
    secureRandom.nextBytes(nonce);
    return nonce;

  }

  private void getFidoFeatures(HidDevice hidDevice) {

    // Ensure device is open after an attach/detach event
    if (!hidDevice.isOpen()) {
      hidDevice.open();
    }

    byte[] nonce = generateNonce();

    // Ping
    byte[] pingRequest = new byte[]{
      (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, // Broadcast channel
      (byte) ((byte) 0x80 + CTAP_CMD_PING), // Command
      0x00, 0x08, // Payload byte count (BCNT)
      nonce[0], nonce[1], nonce[2], nonce[3], nonce[4], nonce[5], nonce[6], nonce[7]
    };

    // Initialise
    byte[] initialiseRequest = new byte[]{
      (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, // Broadcast channel
      (byte) ((byte) 0x80 + CTAP_CMD_INIT), // Command
      0x00, 0x08, // Payload byte count (BCNT)
      nonce[0], nonce[1], nonce[2], nonce[3], nonce[4], nonce[5], nonce[6], nonce[7]
    };

    // GetInfo
    byte[] getInfoRequest = new byte[]{
      0x01, 0x02, 0x03, 0x04, // Response channel (supplied by device)
      (byte) ((byte) 0x80 + CTAP_CMD_CBOR), // Command
      0x00, 0x01, // // Payload byte count (BCNT)
      CTAP_CBOR_GETINFO
    };

    // Write message to device with zero byte padding
    System.out.println(ANSI_GREEN + "Sending CTAPHID_INIT..." + ANSI_RESET);
    int bytesWritten = hidDevice.write(initialiseRequest, CTAP_MAX_REPORT_LEN, (byte) 0x00, true);
    if (bytesWritten < 0) {
      System.out.println(ANSI_RED + hidDevice.getLastErrorMessage() + ANSI_RESET);
      return;
    }

    // Read everything from the device
    byte[] initialiseResponse = hidDevice.readAll();
    if (initialiseResponse == null || initialiseResponse.length == 0) {
      System.out.println(ANSI_RED + hidDevice.getLastErrorMessage() + ANSI_RESET);
      return;
    }

    System.out.println(ANSI_GREEN + "Response is:" + ANSI_RESET);
    System.out.printf("Channel id: %02x %02x %02x %02x%n",
      initialiseResponse[0], initialiseResponse[1], initialiseResponse[2], initialiseResponse[3]
    );
    System.out.printf("Command (0x86): %02x%n", initialiseResponse[4]);
    System.out.printf("Byte count (0x00 0x11): %02x %02x%n", initialiseResponse[5], initialiseResponse[6]);
    System.out.printf("Nonce (%02x %02x %02x %02x %02x %02x %02x %02x): %02x %02x %02x %02x %02x %02x %02x %02x%n",
      nonce[0], nonce[1], nonce[2], nonce[3], nonce[4], nonce[5], nonce[6], nonce[7],
      initialiseResponse[7], initialiseResponse[8],initialiseResponse[9], initialiseResponse[10],
      initialiseResponse[11], initialiseResponse[12],initialiseResponse[13], initialiseResponse[14]
    );
    System.out.printf("New channel id: %02x %02x %02x %02x%n",
      initialiseResponse[15], initialiseResponse[16],initialiseResponse[17], initialiseResponse[18]
    );
    System.out.printf("Protocol (0x02): %02x%n", initialiseResponse[19]);
    System.out.printf("Major: %02x%n", initialiseResponse[20]);
    System.out.printf("Minor: %02x%n", initialiseResponse[21]);
    System.out.printf("Build: %02x%n", initialiseResponse[22]);
    System.out.printf("Capabilities: %02x%n", initialiseResponse[23]);

  }

}
