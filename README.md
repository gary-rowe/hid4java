Status: [![Build Status](https://travis-ci.org/gary-rowe/hid4java.png?branch=master)](https://travis-ci.org/gary-rowe/hid4java)

### Project status

Release: Available for production work

Latest release: [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.hid4java/hid4java/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.hid4java/hid4java)
[![Javadocs](http://www.javadoc.io/badge/org.hid4java/hid4java.svg)](http://www.javadoc.io/doc/org.hid4java/hid4java)

#### A note on time commitment

When `hid4java` was first created it was because I was working on a project that required a multi-platform USB HID library. That project
was mothballed in 2017 leaving `hid4java` on the back burner of my activities. This is a shame because it was originally intended to be
a general purpose library to help other developers access HID devices through an easy to use API in Java. 

Seeing that the project remains in use by developers all over the world I have decided to continue maintaining `hid4java` as a hobby and
will be dedicating roughly an evening a week (probably a Wednesday in the GMT timezone) to keeping on top of issues and releases.

I'd really appreciate it if other developers with more experience of USB than myself can help out with pull requests or perhaps testing
agains a range of devices on operating systems that I don't have easy access to. 

#### Telegram group

If you want to discuss `hid4java` in general please use [the Telegram chat](https://t.me/joinchat/CtU4ZBltWCAFBAjwM5KLLw). I can't guarantee
an instant response but I'm usually active on Telegram during office hours in the GMT timezone.

### Summary 

The `hid4java` project supports USB HID devices through a common API which is provided here under the MIT license.
The API is very simple but provides great flexibility such as support for feature reports and blocking reads with
timeouts. Attach/detach events are provided to allow applications to respond instantly to device availability.

The wiki provides a [guide to building the project](https://github.com/gary-rowe/hid4java/wiki/How-to-build-the-project).

### Technologies

* [hidapi](https://github.com/libusb/hidapi) - Native USB HID library for multiple platforms
* [dockcross](https://github.com/dockcross/dockcross) - Cross-compilation environments for multiple platforms to create hidapi libraries
* [JNA](https://github.com/twall/jna) - to remove the need for Java Native Interface (JNI) and greatly simplify the project
* Java 6+ - to remove dependencies on JVMs that have reached end of life

### Maven dependency

```xml

<dependencies>

  <!-- hid4java for cross-platform HID USB -->
  <dependency>
    <groupId>org.hid4java</groupId>
    <artifactId>hid4java</artifactId>
    <version>0.6.0</version>
  </dependency>

</dependencies>

```


### Code example

Taken from [UsbHidTrezorV1Example](https://github.com/gary-rowe/hid4java/blob/develop/src/test/java/org/hid4java/UsbHidDeviceExample.java) which
provides more details. See later for how to run it from the command line.

```java
// Get HID services
hidServices = HidManager.getHidServices();
hidServices.addHidServicesListener(this);

// Provide a list of attached devices
for (HidDevice hidDevice : hidServices.getAttachedHidDevices()) {
  System.out.println(hidDevice);
}

// Open a Bitcoin Trezor device by Vendor ID and Product ID with wildcard serial number
HidDevice trezor = hidServices.getHidDevice(0x534c, 0x01, null);

// Send the Initialise message
byte[] message = new byte[64];
message[0] = 0x3f;
message[1] = 0x23;
message[2] = 0x23;

int val = trezor.write(message, 64, (byte) 0);
if (val != -1) {
  System.out.println("> [" + val + "]");
} else {
  System.err.println(trezor.getLastErrorMessage());
}

// Clean shutdown
hidServices.shutdown();
    
```
 
### Getting started

If you're unfamiliar with Maven and git the wiki provides [an easy guide to creating a development environment](https://github.com/gary-rowe/hid4java/wiki/How-to-build-the-project).

The project uses the standard Maven build process and can be used without having external hardware attached. Just do the usual

```
cd <project directory>
mvn clean install
```

and you're good to go. If you're in an IDE then you can use `src/test/java/org/hid4java/UsbHidTrezorV1Example`) to verify correct
operation. From the command line:

```
mvn clean test exec:java -Dexec.classpathScope="test" -Dexec.mainClass="org.hid4java.UsbHidDeviceExample"
```
If you have a Trezor device attached you'll see a "Features" message appear as a big block of hex otherwise it will be
just a simple enumeration of attached USB devices. You can plug various devices in and out to see messages.

Use CTRL+C to quit the example.

### Frequently asked questions (FAQ)

#### What platforms do you support?

If you have a native version of `hidapi` for your platform then you'll be able to support it. 

Pre-compiled versions for Windows (32/64), OS X (10.5+) and Linux (32/64) are provided and you
must follow the JNA naming convention when adding new libraries.

#### Why not just use usb4java?

The [usb4java](http://usb4java.org/) project, while superb, does not support HID devices on OS X 
and apparently there are no plans to introduce HID support anytime soon.
 
You will find that trying to claim the USB device on OS X will fail with permissions problems. If
you apply a workaround (such as adding a kernel extension) then it will still fall over just a
little later in the process. The bottom line is that you *must* use hidapi to communicate with HID
devices on OS X.

#### Is the latest code in Maven Central?

Yes but not the older versions at present. If you need to use the older code for some
reason, you'll need to add this to your project's `pom.xml`.

```xml
<repositories>

  <repository>
    <id>mbhd-maven-release</id>
    <url>https://raw.github.com/bitcoin-solutions/mbhd-maven/master/releases</url>
    <releases/>
  </repository>

</repositories>

<dependencies>

  <!-- hid4java for cross-platform HID USB -->
  <dependency>
    <groupId>org.hid4java</groupId>
    <artifactId>hid4java</artifactId>
    <version>0.6.0</version>
  </dependency>

</dependencies>

```
#### I want to try the latest snapshot release - I know the risks

OK, add the following to your project's `pom.xml`:

```xml
<repositories>

  <!-- Only include the snapshot repo if you're working with the latest hid4java on develop -->
  <repository>
    <id>mbhd-maven-snapshot</id>
    <url>https://raw.github.com/bitcoin-solutions/mbhd-maven/master/snapshots</url>
    <!-- These artifacts change frequently during development iterations -->
    <snapshots>
      <updatePolicy>always</updatePolicy>
    </snapshots>
  </repository>

</repositories>

<dependencies>

  <!-- hid4java for cross-platform HID USB -->
  <dependency>
    <groupId>org.hid4java</groupId>
    <artifactId>hid4java</artifactId>
    <version>develop-SNAPSHOT</version>
  </dependency>

</dependencies>

```
 
#### Can I just copy this code into my project?

Yes. Perhaps you'd prefer to use 

```
git submodule add https://github.com/gary-rowe/hid4java hid4java 
```
so that you can keep up to date with changes whilst still fixing the version. The master branch always represents the latest released version.

#### My platform isn't supported what can I do?

The `hidapi` libraries can be compiled using the popular [dockcross](https://github.com/dockcross/dockcross) project. Essentially you use Docker containers that represent
 the build environment you are targeting (e.g. Windows x86) and can then perform the build there. The resulting library can then be added under a suitably named sub-directory.
 
For example, here is a typical process for cross-compiling a shared Windows x86 target (DLL) on OS X. It assumes that you have Docker running but have not installed `hidapi` or
 `dockcross`.

```bash
# Clone the upstream repos
git clone https://github.com/libusb/hidapi.git
git clone https://github.com/dockcross/dockcross.git 

# Configure dockcross Docker script for Windows x86 so it is executable and on path
cd dockcross
docker run --rm dockcross/windows-shared-x86 > ./dockcross-windows-shared-x86
chmod +x ./dockcross-windows-shared-x86
mv ./dockcross-windows-shared-x86 /usr/local/bin

# Cross-copmile hidapi
cd ../hidapi
dockcross-windows-shared-x86 bash -c 'sudo ./bootstrap && sudo ./configure --host=i686-w64-mingw32 && sudo make'

# Examine the output (hidtest is not required)
cd windows/.libs
file libhidapi-0.dll  

# Move and rename the DLL into hid4java structure
mv libhidapi-0.dll ~/src/hid4java/src/main/resources/win32-x86/hidapi.dll
```

Some of the older versions of the `hidapi` native libraries have been removed in version 0.6.0, so if you have a particular requirement and can demonstrate a good case to
 include it in the standard build, please raise an issue to get it looked at.

#### How can I know the version of this library programmatically?

The `HidServices` entry class provides a method `getVersion` which reads the version from the JAR manifest file. You can test it as follows:

```bash
mvn clean package
java -cp target/hid4java-develop-SNAPSHOT.jar org.hid4java.HidServices
```
The version will reflect what is in the `pom.zml` so will only be a numeric triplet on the `master` branch.

### Troubleshooting

The following are known issues and their solutions or workarounds.

#### I get a `SIGSEGV (0xb)` when starting up

This shouldn't occur unless you've been changing the code.

You have probably got the `getFieldOrder` list wrong. Use the field list from Class.getFields() to get a suitable order.
Another cause is if a `Structure` has not been initialised and is being deferenced, perhaps in a `toString()` method.

There is also the possibility that using the built-in `HidDeviceManager` code can cause problems in some applications.

#### I get a "The parameter is incorrect" when writing

There is a special case on Windows for report ID `0x00` which can cause a misalignment during a hidapi `write()`.
To compensate for this, hid4java will detect when it is running on Windows with a report ID of `0x00` and simply copy
the `data` unmodified to the write buffer. In all other cases it will prepend the report ID to the data before submitting
it to hidapi.

If you're seeing this then it may be that your code is attempting to second guess `hid4java`.

#### The `hidapi` library doesn't load

On startup `hid4java` will search the classpath looking for a library that matches the machine OS and architecture (e.g. Windows running on AMD64). It uses the JNA naming conventions to do this and will report the expected path if it fails. You can add your own entry under `src/main/resources` and it should get picked up. Ideally you should [raise an issue](https://github.com/gary-rowe/hid4java/issues) on the `hid4java` repo so that the proper library can be put into the project so that others can avoid this problem.

#### The `hidapi` library loads but takes a long time on Windows

You have probably terminated the JVM using a `kill -9` rather than a clean shutdown. This will have left the `HidApi` process lock on the DLL still in force and Windows will continuously check to see if it can share it with a new instance.
Just detach and re-attach the device to clear it.

#### I'm seeing spurious attach/detach events occurring on Windows

This was a device enumeration bug in early versions of `hid4java`. Use version 0.3.1 or higher.

#### My device doesn't work on Linux

Different flavours of Linux require different settings:

##### Ubuntu

Out of the box Ubuntu classifies HID devices as belonging to root. You can override this rule by creating your own under `/etc/udev/rules.d`:
```
sudo gedit /etc/udev/rules.d/99-myhid.rules
```
Make the content of this file as below (using your own discovered hex values for `idProduct` and `idVendor`):
```
# My HID device
ATTRS{idProduct}=="0001", ATTRS{idVendor}=="abcd", MODE="0660", GROUP="plugdev"
```
Save and exit from root, then unplug and replug your device. The rules should take effect immediately. If they're still not running it may that you're not a member of the `plugdev` group. You can fix this as follows (assuming that `plugdev` is not present on your system):
```
sudo addgroup plugdev
sudo addgroup yourusername plugdev
```

##### Slackware

Edit the USB udev rules `/etc/udev/rules.d` as follows:
```
MODE="0666", GROUP="dialout"
```

##### ARM
Running on ARM machines you may encounter problems due to a missing library. This is just a naming issue for the `udev` library and can be resolved using the following command (or equivalent for your system):
```
sudo ln -sf /lib/arm-linux-gnueabihf/libudev.so.1 /lib/arm-linux-gnueabihf/libudev.so.0
```
Thanks to @MaxRoma for that one!

#### My device doesn't work on Windows

Check that the usage page is not `0x06` which is reserved for keyboards and mice. [Windows opens these devices for its exclusive use](https://msdn.microsoft.com/en-us/library/windows/hardware/jj128406%28v=vs.85%29.aspx) and thus hid4java
cannot establish its own connection to them. You will need to use the lower level usb4java library for this.

### Release procedure

The release procedure is as follows:

1. Finalise all development on the `develop` branch.
2. Run the `./release.sh` script to verify release conditions and perform the merge to `master` with appropriate tagging. Code will be pushed upstream.
3. Run `mvn clean deploy` to push the release artifacts to Maven Central.

### Closing notes

All trademarks and copyrights are acknowledged.

Many thanks to victorix who provided the basis for this library. Please see the inspiration <a href="http://developer.mbed.org/cookbook/USBHID-bindings-">on the mbed.org site.</a></p>

Thanks also go to everyone who has contributed their knowledge and advice during the creation and subsequent improvement of this library.
