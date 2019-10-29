#!/usr/bin/env bash

eval "$(docker-machine env default)"

# Convenience script to build hidapi locally

cd ~/Workspaces/Docker/dockcross

# Windows environments

# 64-bit
docker run --rm dockcross/windows-shared-x64 > ./dockcross-windows-shared-x64
chmod +x ./dockcross-windows-shared-x64
mv ./dockcross-windows-shared-x64 /usr/local/bin

# 32-bit
docker run --rm dockcross/windows-shared-x86 > ./dockcross-windows-shared-x86
chmod +x ./dockcross-windows-shared-x86
mv ./dockcross-windows-shared-x86 /usr/local/bin

# Linux environments

# 64 bit
docker run --rm dockcross/linux-x64 > ./dockcross-linux-x64
chmod +x ./dockcross-linux-x64
mv ./dockcross-linux-x64 /usr/local/bin

# 32 bit
docker run --rm dockcross/linux-x86 > ./dockcross-linux-x86
chmod +x ./dockcross-linux-x86
mv ./dockcross-linux-x86 /usr/local/bin

# Raspberry Pi (ARMv6)
docker run --rm dockcross/linux-armv6 > ./dockcross-linux-armv6
chmod +x ./dockcross-linux-armv6
mv ./dockcross-linux-armv6 /usr/local/bin

# Cross compilation
cd ~/Workspaces/Cpp/hidapi/

# Windows environments

# 64-bit
dockcross-windows-shared-x64 bash -c 'sudo make clean && sudo ./bootstrap && sudo ./configure --host=x86_64-w64-mingw32 && sudo make'
cp windows/.libs/libhidapi-0.dll ../../Java/Personal/hid4java/src/main/resources/win32-x86-64/hidapi.dll
cp windows/.libs/libhidapi-0.dll ../../Java/Personal/hid4java/src/main/resources/win32-amd64/hidapi.dll

# 32-bit
dockcross-windows-shared-x86 bash -c 'sudo make clean && sudo ./bootstrap && sudo ./configure --host=i686-w64-mingw32 && sudo make'
cp windows/.libs/libhidapi-0.dll ../../Java/Personal/hid4java/src/main/resources/win32-x86/hidapi.dll

# Linux environments (require libudev and libusb to be installed)

# 64-bit
dockcross-linux-x64 bash -c 'sudo make clean && sudo apt-get --force-yes --yes install libudev-dev libusb-1.0-0-dev && sudo ./bootstrap && sudo ./configure && sudo make'
cp linux/.libs/libhidapi-hidraw.so ../../Java/Personal/hid4java/src/main/resources/linux-x86-64/libhidapi.so
cp linux/.libs/libhidapi-hidraw.so ../../Java/Personal/hid4java/src/main/resources/linux-amd64/libhidapi.so

# 32-bit
dockcross-linux-x86 bash -c 'sudo make clean && sudo apt-get --force-yes --yes install libudev-dev libusb-1.0-0-dev && sudo ./bootstrap && sudo ./configure && sudo make'
cp linux/.libs/libhidapi-hidraw.so ../../Java/Personal/hid4java/src/main/resources/linux-x86/libhidapi.so

# Raspberry Pi (ARMv6) - broken
#dockcross-linux-armv6 bash -c 'sudo make clean && sudo apt-get --force-yes --yes install libudev-dev libusb-1.0-dev && sudo ./bootstrap && sudo ./configure --host=arm-linux &&
# sudo make'
#cp linux/.libs/libhidapi-0.so ../../Java/Personal/hid4java/src/main/resources/linux-arm/libhidapi.so

# OS X environments

# Darwin
./bootstrap
./configure
make
cp mac/.libs/libhidapi.0.dylib ../../Java/Personal/hid4java/src/main/resources/darwin/libhidapi.dylib

# List all file info

# Windows
file ../../Java/Personal/hid4java/src/main/resources/win32-x86-64/hidapi.dll
file ../../Java/Personal/hid4java/src/main/resources/win32-amd64/hidapi.dll
file ../../Java/Personal/hid4java/src/main/resources/win32-x86/hidapi.dll

# Linux
file ../../Java/Personal/hid4java/src/main/resources/linux-x86-64/libhidapi.so
file ../../Java/Personal/hid4java/src/main/resources/linux-amd64/libhidapi.so
#file ../../Java/Personal/hid4java/src/main/resources/linux-arm/libhidapi.so

# OS X
file ../../Java/Personal/hid4java/src/main/resources/darwin/libhidapi.dylib

echo Done