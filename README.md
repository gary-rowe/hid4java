# Project status

🚨 OBSOLETE. DO NOT USE! 🚨

I first created `hid4java` to support a specific hardware device over 10 years ago and it kind of grew from there. I no longer have the time or inclination to support the project and I would rather that someone else continue the work via a fork or complete rewrite.

I'd like to express my thanks to all those developers who have contributed by generously providing code, testing, review and general feedback. I learned a great deal along the way with this project.

I'll continue to leave this repository open for people to use as a reference point, but there will be no further development.

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.hid4java/hid4java/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.hid4java/hid4java) [![Javadoc](https://javadoc.io/badge2/org.hid4java/hid4java/javadoc.svg)](https://javadoc.io/doc/org.hid4java/hid4java) 

# 🌟 Summary 

The `hid4java` project supports USB HID devices through a common API which is provided here under the MIT license. The API is very simple but provides great flexibility such as support for feature reports and blocking reads with timeouts. Attach/detach events are provided to allow applications to respond instantly to device availability.

## Telegram group

If you want to discuss `hid4java` in general please use [the Telegram chat](https://t.me/joinchat/CtU4ZBltWCAFBAjwM5KLLw). I can't guarantee
an instant response but I'm usually active on Telegram during office hours in the GMT timezone.

👀 Remember to [check the Wiki first](https://github.com/gary-rowe/hid4java/wiki/Home) before asking questions to avoid causing frustration!

## Technologies

* [hidapi](https://github.com/libusb/hidapi) - Native USB HID library for multiple platforms
* [JNA](https://github.com/twall/jna) - Removes the need for Java Native Interface (JNI) and greatly simplify the project
* [dockcross](https://github.com/dockcross/dockcross) - Cross-compilation environments for multiple platforms to create hidapi libraries
* [Maven](https://maven.apache.org/) - Build environment for Java projects if you need to customise the library
* Java 8+ - to remove dependencies on JVMs that have reached end of life

## Maven dependency

```xml

<dependencies>

  <!-- hid4java for cross-platform HID USB -->
  <dependency>
    <groupId>org.hid4java</groupId>
    <artifactId>hid4java</artifactId>
    <version>0.8.0</version>
  </dependency>

</dependencies>

```
If you are developing and want the latest code, you'll need to reference release candidate artifacts deployed to the Maven Central snapshot repository. These are updated more frequently as part of wider user acceptance testing, but they are not considered stable enough for production use. 

Add the following `repositories` section to your project's `pom.xml` if you're using Maven for your build process.

```xml
  <!-- Enable Maven Central snapshots repository -->
<repositories>
  <repository>
    <id>oss.sonatype.org-snapshot</id>
    <url>https://oss.sonatype.org/content/repositories/snapshots</url>
    <releases>
      <enabled>false</enabled>
    </releases>
    <snapshots>
      <enabled>true</enabled>
    </snapshots>
  </repository>
</repositories>
```

## Gradle dependency

```gradle

repositories {
    mavenCentral()
}

dependencies {
    implementation('org.hid4java:hid4java')
}

```

## 🚀 Code example

Taken from [UsbHidEnumerationExample](https://github.com/gary-rowe/hid4java/blob/develop/src/test/java/org/hid4java/examples/UsbHidEnumerationExample.java) which
provides more details. 

```java
// Configure to use custom specification
HidServicesSpecification hidServicesSpecification = new HidServicesSpecification();

// Use the v0.7.0 manual start feature to get immediate attach events
hidServicesSpecification.setAutoStart(false);

// Get HID services using custom specification
HidServices hidServices = HidManager.getHidServices(hidServicesSpecification);
hidServices.addHidServicesListener(this);

// Manually start the services to get attachment event
hidServices.start();

// Provide a list of attached devices
for (HidDevice hidDevice : hidServices.getAttachedHidDevices()) {
  System.out.println(hidDevice);
}
    
```

# ⚙ Local build

If you're unfamiliar with Maven and git the wiki provides [an easy guide to creating a development environment](https://github.com/gary-rowe/hid4java/wiki/How-to-set-up-a-build-environment-from-scratch).

The project uses the standard Maven build process and can be used without having external hardware attached. Just do the usual

```shell
cd <workspace>
git clone https://github.com/gary-rowe/hid4java.git
cd hid4java

# Maven build
mvn clean install
```

and you're good to go. 

Maven will place the built JAR into the `target` directory. 
The Maven `install` process will also place copies of the built JARs into `~/.m2/repository/org/hid4java/hid4java/<version>/` so that other local projects can find and share them. 

# 🤔 More information

Much of the information previously in this README has been migrated to the [project Wiki](https://github.com/gary-rowe/hid4java/wiki/Home) as it was getting rather long. Here are some useful jumping off points that should help:

* [Home](https://github.com/gary-rowe/hid4java/wiki/Home) - The wiki Home page with lots of useful launch points
* [FAQ](https://github.com/gary-rowe/hid4java/wiki/FAQ) - Frequently asked questions
* [Examples](https://github.com/gary-rowe/hid4java/wiki/Examples) - Using the examples to kickstart your own project
* [Troubleshooting](https://github.com/gary-rowe/hid4java/wiki/Troubleshooting) - A comprehensive troubleshooting guide

# 📕 Closing notes

All trademarks and copyrights are acknowledged.

Many thanks to `victorix` who provided the basis for this library. Please [see the inspiration on the mbed site](https://os.mbed.com/cookbook/USBHID-bindings-).

Thanks also go to everyone who has contributed their knowledge and advice during the creation and subsequent improvement of this library.
