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

### Closing notes

All trademarks and copyrights are acknowledged.

Many thanks to victorix who provided the basis for this library. Please see the inspiration <a href="http://developer.mbed.org/cookbook/USBHID-bindings-">on the mbed.org site.</a></p>
