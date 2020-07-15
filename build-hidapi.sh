#!/usr/bin/env bash

eval "$(docker-machine env default)"

# Convenience script to build hidapi locally

cd ~/Workspaces/Docker/dockcross

# 64-bit
echo -e "\033[32mConfiguring Windows 64-bit\033[0m"
docker run --rm dockcross/windows-shared-x64 > ./dockcross-windows-shared-x64
chmod +x ./dockcross-windows-shared-x64
mv ./dockcross-windows-shared-x64 /usr/local/bin

# 32-bit
echo -e "\033[32mConfiguring Windows 32-bit\033[0m"
docker run --rm dockcross/windows-shared-x86 > ./dockcross-windows-shared-x86
chmod +x ./dockcross-windows-shared-x86
mv ./dockcross-windows-shared-x86 /usr/local/bin

echo -e "\033[32mConfiguring Linux environments\033[0m"

# 64 bit
echo -e "\033[32mConfiguring Linux 64-bit\033[0m"
docker run --rm dockcross/linux-x64 > ./dockcross-linux-x64
chmod +x ./dockcross-linux-x64
mv ./dockcross-linux-x64 /usr/local/bin

# 32 bit
echo -e "\033[32mConfiguring Linux 32-bit\033[0m"
docker run --rm dockcross/linux-x86 > ./dockcross-linux-x86
chmod +x ./dockcross-linux-x86
mv ./dockcross-linux-x86 /usr/local/bin

# Raspberry Pi (ARMv6)
echo -e "\033[32mConfiguring Raspberry Pi (ARMv6)\033[0m"
docker run --rm dockcross/linux-armv6 > ./dockcross-linux-armv6
chmod +x ./dockcross-linux-armv6
mv ./dockcross-linux-armv6 /usr/local/bin

# Android
# 64 bit
echo -e "\033[32mConfiguring Android 64-bit\033[0m"
docker run --rm dockcross/android-arm64 > ./dockcross-android-arm64
chmod +x ./dockcross-android-arm64
mv ./dockcross-android-arm64 /usr/local/bin

# 32 bit
echo -e "\033[32mConfiguring Android 32-bit\033[0m"
docker run --rm dockcross/android-arm > ./dockcross-android-arm
chmod +x ./dockcross-android-arm
mv ./dockcross-android-arm /usr/local/bin

# Cross compilation
echo -e "\033[32m------------------------------------------------------------------------\033[0m"

cd ~/Workspaces/Cpp/hidapi/

# Windows environments

# 64-bit
echo -e "\033[32mBuilding Windows 64-bit\033[0m"
dockcross-windows-shared-x64 bash -c 'sudo apt-get update && sudo apt-get --yes install libudev-dev libusb-1.0-0-dev && sudo make clean && sudo ./bootstrap && sudo ./configure --host=x86_64-w64-mingw32 && sudo make'
if [[ "$?" -ne 0 ]]
  then
    echo -e "\033[31mFailed\033[0m - Removing damaged targets"
    rm ../../Java/Personal/hid4java/src/main/resources/win32-x86-64/hidapi.dll
    rm ../../Java/Personal/hid4java/src/main/resources/win32-amd64/hidapi.dll
    exit
  else
    echo -e "\033[32mOK\033[0m"
    cp windows/.libs/libhidapi-0.dll ../../Java/Personal/hid4java/src/main/resources/win32-x86-64/hidapi.dll
    cp windows/.libs/libhidapi-0.dll ../../Java/Personal/hid4java/src/main/resources/win32-amd64/hidapi.dll
fi
echo -e "\033[32m------------------------------------------------------------------------\033[0m"

# 32-bit
echo -e "\033[32mBuilding Windows 32-bit\033[0m"
dockcross-windows-shared-x86 bash -c 'sudo make clean && sudo ./bootstrap && sudo ./configure --host=i686-w64-mingw32 && sudo make'
if [[ "$?" -ne 0 ]]
  then
    echo -e "\033[31mFailed\033[0m - Removing damaged targets"
    rm ../../Java/Personal/hid4java/src/main/resources/win32-x86/hidapi.dll
  else
    echo -e "\033[32mOK\033[0m"
    cp windows/.libs/libhidapi-0.dll ../../Java/Personal/hid4java/src/main/resources/win32-x86/hidapi.dll
fi
echo -e "\033[32m------------------------------------------------------------------------\033[0m"

# Linux environments

# 64-bit
echo -e "\033[32mBuilding Linux 64-bit\033[0m"
dockcross-linux-x64 bash -c 'sudo apt-get update && sudo apt-get --yes install libudev-dev libusb-1.0-0-dev && sudo make clean && sudo ./bootstrap && sudo ./configure && sudo make'
if [[ "$?" -ne 0 ]]
  then
    echo -e "\033[31mFailed\033[0m - Removing damaged targets"
    rm ../../Java/Personal/hid4java/src/main/resources/win32-x86/hidapi.dll
    rm ../../Java/Personal/hid4java/src/main/resources/linux-x86-64/libhidapi.so
    rm ../../Java/Personal/hid4java/src/main/resources/linux-amd64/libhidapi.so
  else
    echo -e "\033[32mOK\033[0m"
    cp linux/.libs/libhidapi-hidraw.so ../../Java/Personal/hid4java/src/main/resources/linux-x86-64/libhidapi.so
    cp linux/.libs/libhidapi-hidraw.so ../../Java/Personal/hid4java/src/main/resources/linux-amd64/libhidapi.so
fi
echo -e "\033[32m------------------------------------------------------------------------\033[0m"

# 32-bit
echo -e "\033[32mBuilding Linux 32-bit\033[0m"
dockcross-linux-x86 bash -c 'sudo apt-get update && sudo apt-get --yes install libudev-dev libusb-1.0-0-dev && sudo make clean && sudo ./bootstrap && sudo ./configure && sudo make'
if [[ "$?" -ne 0 ]]
  then
    echo -e "\033[31mFailed\033[0m - Removing damaged targets"
    rm ../../Java/Personal/hid4java/src/main/resources/linux-x86/libhidapi.so
  else
    echo -e "\033[32mOK\033[0m"
    cp linux/.libs/libhidapi-hidraw.so ../../Java/Personal/hid4java/src/main/resources/linux-x86/libhidapi.so
fi
echo -e "\033[32m------------------------------------------------------------------------\033[0m"

# ARM environments

# ARMv6 - Hard float (Raspberry Pi Model 1)
echo -e "\033[32mBuilding Raspberry Pi (ARMv6)\033[0m"
dockcross-linux-armv6 bash -c 'sudo dpkg --add-architecture armhf && sudo apt-get update && sudo apt-get --yes install gcc-arm-linux-gnueabihf libudev-dev:armhf libusb-1.0-0-dev:armhf && sudo make clean && sudo ./bootstrap && sudo ./configure --host=arm-linux-gnueabihf && sudo make'
if [[ "$?" -ne 0 ]]
  then
    echo -e "\033[31mFailed\033[0m - Removing damaged targets"
    rm ../../Java/Personal/hid4java/src/main/resources/linux-arm/libhidapi.so
  else
    echo -e "\033[32mOK\033[0m"
    cp linux/.libs/libhidapi-hidraw.so ../../Java/Personal/hid4java/src/main/resources/linux-arm/libhidapi.so
fi
echo -e "\033[32m------------------------------------------------------------------------\033[0m"

# OS X environments

# Darwin
echo -e "\033[32mBuilding OS X Darwin\033[0m"
make clean
./bootstrap
./configure
make
if [[ "$?" -ne 0 ]]
  then
    echo -e "\033[31mFailed\033[0m - Removing damaged targets"
    rm ../../Java/Personal/hid4java/src/main/resources/darwin/libhidapi.dylib
  else
    echo -e "\033[32mOK\033[0m"
    cp mac/.libs/libhidapi.0.dylib ../../Java/Personal/hid4java/src/main/resources/darwin/libhidapi.dylib
fi
echo -e "\033[32m------------------------------------------------------------------------\033[0m"

# List all file info
echo -e "\033[32mResulting build files placed in hid4java:\033[0m"

# Windows
echo -e "\033[32mWindows\033[0m"
echo -e "\033[32mwin32-x86-64\033[0m"
file -b ../../Java/Personal/hid4java/src/main/resources/win32-x86-64/hidapi.dll

echo -e "\033[32mwin32-amd64\033[0m"
file -b  ../../Java/Personal/hid4java/src/main/resources/win32-amd64/hidapi.dll

echo -e "\033[32mwin32-x86\033[0m"
file -b ../../Java/Personal/hid4java/src/main/resources/win32-x86/hidapi.dll

echo -e "\033[32m------------------------------------------------------------------------\033[0m"

# Linux
echo -e "\033[32mLinux\033[0m"
echo -e "\033[32mlinux-x86-64\033[0m"
file -b ../../Java/Personal/hid4java/src/main/resources/linux-x86-64/libhidapi.so

echo -e "\033[32mlinux-amd64\033[0m"
file -b ../../Java/Personal/hid4java/src/main/resources/linux-amd64/libhidapi.so

echo -e "\033[32mlinux-arm\033[0m"
file -b ../../Java/Personal/hid4java/src/main/resources/linux-arm/libhidapi.so

echo -e "\033[32m------------------------------------------------------------------------\033[0m"

# ARM

echo -e "\033[32mARM (hard float)\033[0m"
echo -e "\033[32mlinux-arm\033[0m"
file -b ../../Java/Personal/hid4java/src/main/resources/linux-arm/libhidapi.so

# OS X
echo -e "\033[32mOS X\033[0m"
echo -e "\033[32mdarwin\033[0m"
file -b ../../Java/Personal/hid4java/src/main/resources/darwin/libhidapi.dylib

echo -e "\033[32m------------------------------------------------------------------------\033[0m"

echo -e "\033[32mDone - Check all OK in summary above.\033[0m"
