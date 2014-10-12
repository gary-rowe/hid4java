Status: [![Build Status](https://travis-ci.org/gary-rowe/hid4java.png?branch=master)](https://travis-ci.org/gary-rowe/hid4java)

### Project status

Early-Beta: Expect bugs and minimal API changes. Not suitable for production, but developers should start integrating.

### hid4java 

The hid4java project supports USB HID devices through a common API which is provided here under the MIT license.

### Technologies

* [hidapi](https://github.com/signal11/hidapi) - Native USB HID library for multiple platforms
* [JNA](https://github.com/twall/jna) - to remove the need for Java Native Interface (JNI) and greatly simplify the project
* Java 7+ - to remove dependencies on JVMs that have reached end of life

### Code example

```java
// Get HID services
hidServices = HidManager.getHidServices();
hidServices.addUsbServicesListener(this);

// Provide a list of attached devices
for (HidDeviceInfo hidDeviceInfo : hidServices.getAttachedHidDevices()) {
  System.out.println(hidDeviceInfo);
}

// Open a Bitcoin Trezor device by Vendor ID and Product ID with wildcard serial number
HidDevice trezor = hidServices.getHidDevice(0x534c, 0x01, null);

// Send the Initialise message
byte[] message = new byte[64];
for (int i = 0; i < 64; i++) {
  message[i] = 0x00;
}
message[0] = 0x3f;
message[1] = 0x23;
message[2] = 0x23;

int val = trezor.write(message, 64, (byte) 0);
if (val != -1) {
  System.out.println("> [" + val + "]");
} else {
  System.err.println(trezor.getLastErrorMessage());
}
    
```
 
### Getting started

The project uses the standard Maven build process and can be used without having external hardware attached. Just do the usual

```
$ cd <project directory>
$ mvn clean install
```

and you're good to go. Your next step is to explore the examples.

### Frequently asked questions (FAQ)

#### What platforms do you support ?

If you have a native version of `hidapi` for your platform then you'll be able to support it. 

Pre-compiled versions for Windows (32/64), OS X (10.5+) and Linux (32/64) are provided.

#### Why not just use usb4java ?

The usb4java project, while superb, does not support HID devices on OS X and there are no plans to introduce HID support anytime soon.
 
#### Can I just copy this code into my project ?

Yes. Perhaps you'd prefer to use 

```
git submodule add https://github.com/gary-rowe/hid4java hid4java 
```
so that you can keep up to date with changes whilst still fixing the version. 


### Troubleshooting

The following are known issues and their solutions or workarounds.

#### I get a `SIGSEGV (0xb)` when starting up

You have probably got the `getFieldOrder` list wrong. Use the field list from Class.getFields() to get a suitable order.

#### My device doesn't work on Ubuntu

Out of the box Ubuntu classifies HID devices as belonging to root. You can override this rule by creating your own under `/etc/udev/rules.d`:

```
$ sudo gedit /etc/udev/rules.d/99-myhid.rules
```

Make the content of this file as below (using your own discovered hex values for `idProduct` and `idVendor`):

```
# My HID device
ATTRS{idProduct}=="0001", ATTRS{idVendor}=="abcd", MODE="0660", GROUP="plugdev"
```

Save and exit from root, then unplug and replug your device. The rules should take effect immediately. If they're still not 
running it may that you're not a member of the `plugdev` group. You can fix this as follows (assuming that `plugdev` is not present on 
your system):

```
$ sudo addgroup plugdev
$ sudo addgroup yourusername plugdev 
```

### Closing notes

All trademarks and copyrights are acknowledged.

Many thanks to victorix who provided the basis for this library. Please see the inspiration <a href="http://developer.mbed.org/cookbook/USBHID-bindings-">on the mbed.org site.</a></p>
