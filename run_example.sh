#!/bin/sh
# run_example.sh
# Wrapper script to run the example programs in subdirectory 
# ./test/java/org/examples of a git checkout of hid4java.

# Note that this script seems to be required for Linux (specifically 
# Ubuntu 24.04.2 LTS) because the documented recipe of running 
# examples with a command line like: 
#
# mvn clean test exec:java -Dexec.classpathScope="test" -Dexec.mainClass="org.hid4java.examples.UsbHidEnumerationExample"
#
# does not exit cleanly.

# The examples run to completion as expected, but threads started by the 
# application do not exit promptly when sent an interrupt signal, the shutdown
# process is delayed, and ultimately an exception is thrown reporting
# that a background thread was closed by force.  

# Messages seen when this occurs include:
#
# [WARNING] thread Thread[...] was interrupted but is still alive after waiting at least 15000msecs
# [WARNING] thread Thread[...] will linger despite being asked to die via interruption
# [WARNING] NOTE: ... thread(s) did not finish despite being asked to via interruption. This is not a problem with exec:java, it is a problem with the running code. Although not serious, it should be remedied.

# The following StackOverflow answer suggests that this might be a 
# problem arising because of the fact that when the app is run from 
# Maven it is not allowed to own the main thread, and the operation of 
# the Thread.interrupt() requires the main thread to exit in order
# to unblock and shut down background threads.
# https://stackoverflow.com/a/77783869

# The following issue has been raised on hid4java's GitHub repository
# relating to this problem:
# https://github.com/gary-rowe/hid4java/issues/161

examplejavapkg=org.hid4java.examples
examplesubpath=$(echo $examplejavapkg | sed -e 's^\.^/^g')
srcpathprefix=./src/test/java
clspathprefix=./target/test-classes

# This problem has been reported against a specific variant of
# AMD/Intel desktop Linux.
# If the problem turns out to be Maven-related and affect other 
# platforms, it may be necessary to require the platform to 
# be specified or detected.  For now, YAGNI.
h4j_platform=linux-amd64

usage() {
    echo ""
    echo "Usage:"
    echo "   $0 [ExampleClassName]"
    echo ""
    echo "where [ExampleClassName] is the basename of one of the following "
    echo "Java source files in ./src/test/java/$examplesubpath/: "
    echo ""
    ls -1 ./src/test/java/$examplesubpath/ | grep -v BaseExample
    echo ""
    exit 1
}

examplename=$1

if [ -z $examplename ]
then
    usage
elif [ ! -f $srcpathprefix/$examplesubpath/$examplename.java ]
then
    echo Example source file not found at $srcpathprefix/$examplesubpath/$examplename.java
    usage
elif [ ! -f $clspathprefix/$examplesubpath/$examplename.class ]
then
    echo Example compiled file not found at $clspathprefix/$examplesubpath/$examplename.class
    echo Perhaps run 'sudo mvn clean test' and fix build errors?
else
    echo Pre-run checks OK
fi

# To run examples under AMD/Intel Linux, we need a classpath 
# which includes the following components:
#
# + the .class files compiled from hid4java source for the 
# hid4java library itself and its tests (the examples are
# a subdirectory of the test set)
CLASSPATH=target/classes
CLASSPATH=$CLASSPATH:target/test-classes:
#
# + the .so files necessary to run under the current OS platform
CLASSPATH=$CLASSPATH:target/classes/$h4j_platform
#
# + the .jar file providing JNA library, in the user's Maven repository
CLASSPATH=$CLASSPATH:$HOME/.m2/repository/net/java/dev/jna/jna/5.16.0/jna-5.16.0.jar

export CLASSPATH

sudo java -cp $CLASSPATH org.hid4java.examples.$examplename


