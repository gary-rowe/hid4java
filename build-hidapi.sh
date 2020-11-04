#!/usr/bin/env bash

# Convenience script to build hidapi locally
# Directory structure is
# ~/Workspaces
#  + Cpp
#    + hidapi
#  + Docker
#    + dockcross
#  + Java
#    + Personal
#      + hid4java
#
# Supported command line arguments are:
#
# all - build all variants
# windows - build all Windows variants
# linux - build all Linux variants
# osx - build all OS X variants
# darwin - OS X 64-bit
# linux-aarch64 - Linux ARMv8 64-bit
# linux-amd64 - Linux AMD 64-bit
# linux-arm - Linux ARMv7 hard float 32-bit
# linux-armel - Linux ARMv6 EABI 32-bit
# linux-x86-64 - Linux x86 64-bit
# linux-x86 - Linux x86 32-bit
# win32-x86 - Windows 32-bit
# win32-x86-64 - Windows 64-bit
#

echo -e "\033[32m------------------------------------------------------------------------\033[0m"
echo -e "\033[33mTarget build for HIDAPI is $1\033[0m"

# Dockcross latest release

echo -e "\033[32mConfiguring Dockcross\033[0m"
cd ~/Workspaces/Docker/dockcross/ || exit
git checkout master
git pull

echo -e "\033[32m------------------------------------------------------------------------\033[0m"

# Ensure Docker is running

echo -e "\033[32mConfiguring Docker\033[0m"
if docker_result=$(docker-machine env default); then
    eval "$docker_result"
    echo -e "\033[32mOK\033[0m"
else
    echo -e "\033[31mFailed\033[0m - Docker not running. Use 'docker-machine start default'"
    exit
fi
echo -e "\033[32m------------------------------------------------------------------------\033[0m"

# Windows cross compilers

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

# Linux cross compilers

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

# ARM cross compilers

# 32-bit ARMv6 EABI
echo -e "\033[32mConfiguring ARMv6 EABI 32-bit\033[0m"
docker run --rm dockcross/linux-armv6 > ./dockcross-linux-armv6
chmod +x ./dockcross-linux-armv6
mv ./dockcross-linux-armv6 /usr/local/bin

# 32-bit ARMv7 hard float
echo -e "\033[32mConfiguring ARMv7 32-bit\033[0m"
docker run --rm dockcross/linux-armv7 > ./dockcross-linux-armv7
chmod +x ./dockcross-linux-armv7
mv ./dockcross-linux-armv7 /usr/local/bin

# 64-bit (arm64, aarch64)
echo -e "\033[32mConfiguring ARM 64-bit\033[0m"
docker run --rm dockcross/linux-arm64 > ./dockcross-linux-arm64
chmod +x ./dockcross-linux-arm64
mv ./dockcross-linux-arm64 /usr/local/bin

echo -e "\033[32m------------------------------------------------------------------------\033[0m"

# HIDAPI latest release
echo -e "\033[32mConfiguring HIDAPI\033[0m"
cd ~/Workspaces/Cpp/hidapi/ || exit
git checkout master
git pull

echo -e "\033[32m------------------------------------------------------------------------\033[0m"

# Windows environments

# 64-bit
if [[ "$1" == "all" ]] || [[ "$1" == "windows" ]] || [[ "$1" == "win32-x86-64" ]]
  then
    echo -e "\033[32mBuilding Windows 64-bit\033[0m"
    dockcross-windows-shared-x64 bash -c 'sudo apt-get update && sudo apt-get --yes install libudev-dev libusb-1.0-0-dev && sudo make clean && sudo ./bootstrap && sudo ./configure --host=x86_64-w64-mingw32 && sudo make'
    if [[ "$?" -ne 0 ]]
      then
        echo -e "\033[31mFailed\033[0m - Removing damaged targets"
        rm ../../Java/Personal/hid4java/src/main/resources/win32-x86-64/hidapi.dll
        exit
      else
        echo -e "\033[32mOK\033[0m"
        cp windows/.libs/libhidapi-0.dll ../../Java/Personal/hid4java/src/main/resources/win32-x86-64/hidapi.dll
    fi
  else
    echo -e "\033[33mSkipping win32-x86-64\033[0m"
fi
echo -e "\033[32m------------------------------------------------------------------------\033[0m"

# 32-bit
if [[ "$1" == "all" ]] || [[ "$1" == "windows" ]] || [[ "$1" == "win32-x86" ]]
  then
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
  else
    echo -e "\033[33mSkipping win32-x86\033[0m"
fi
echo -e "\033[32m------------------------------------------------------------------------\033[0m"

# Linux environments

# 64-bit
if [[ "$1" == "all" ]] || [[ "$1" == "linux" ]] || [[ "$1" == "linux-x86-64" ]]
  then
    echo -e "\033[32mBuilding Linux 64-bit\033[0m"
    # Note the use of a double sudo apt-get update here
    dockcross-linux-x64 bash -c 'sudo apt-get update || sudo apt-get update && sudo apt-get --yes install libudev-dev libusb-1.0-0-dev && sudo make clean && sudo ./bootstrap && sudo ./configure && sudo make'
    if [[ "$?" -ne 0 ]]
      then
        echo -e "\033[31mFailed\033[0m - Removing damaged targets"
        rm ../../Java/Personal/hid4java/src/main/resources/linux-x86-64/libhidapi.so
        rm ../../Java/Personal/hid4java/src/main/resources/linux-amd64/libhidapi.so
        rm ../../Java/Personal/hid4java/src/main/resources/linux-x86-64/libhidapi-libusb.so
        rm ../../Java/Personal/hid4java/src/main/resources/linux-amd64/libhidapi-libusb.so
      else
        echo -e "\033[32mOK\033[0m"
        cp linux/.libs/libhidapi-hidraw.so ../../Java/Personal/hid4java/src/main/resources/linux-x86-64/libhidapi.so
        cp linux/.libs/libhidapi-hidraw.so ../../Java/Personal/hid4java/src/main/resources/linux-amd64/libhidapi.so
        cp libusb/.libs/libhidapi-libusb.so ../../Java/Personal/hid4java/src/main/resources/linux-x86-64/libhidapi-libusb.so
        cp libusb/.libs/libhidapi-libusb.so ../../Java/Personal/hid4java/src/main/resources/linux-amd64/libhidapi-libusb.so
    fi
  else
    echo -e "\033[33mSkipping linux-x86-64\033[0m"
fi
echo -e "\033[32m------------------------------------------------------------------------\033[0m"

# 32-bit
if [[ "$1" == "all" ]] || [[ "$1" == "linux" ]] || [[ "$1" == "linux-x86" ]]
  then
    echo -e "\033[32mBuilding Linux 32-bit\033[0m"
    dockcross-linux-x86 bash -c 'sudo dpkg --add-architecture i386 && sudo apt-get update && sudo apt-get --yes install libudev-dev libusb-1.0-0-dev libudev-dev:i386 libusb-1.0-0-dev:i386 && sudo make clean && sudo ./bootstrap && sudo ./configure && sudo make'
    if [[ "$?" -ne 0 ]]
      then
        echo -e "\033[31mFailed\033[0m - Removing damaged targets"
        rm ../../Java/Personal/hid4java/src/main/resources/linux-x86/libhidapi.so
        rm ../../Java/Personal/hid4java/src/main/resources/linux-x86/libhidapi-libusb.so
      else
        echo -e "\033[32mOK\033[0m"
        cp linux/.libs/libhidapi-hidraw.so ../../Java/Personal/hid4java/src/main/resources/linux-x86/libhidapi.so
        cp libusb/.libs/libhidapi-libusb.so ../../Java/Personal/hid4java/src/main/resources/linux-x86/libhidapi-libusb.so
    fi
  else
    echo -e "\033[33mSkipping linux-x86\033[0m"
fi
echo -e "\033[32m------------------------------------------------------------------------\033[0m"

# ARM environments

# 64-bit (arm64/aarch64)
if [[ "$1" == "all" ]] || [[ "$1" == "linux" ]] || [[ "$1" == "linux-aarch64" ]]
  then
    echo -e "\033[32mBuilding ARM64/aarch64 (ARMv8)\033[0m"
    dockcross-linux-arm64 bash -c 'sudo dpkg --add-architecture arm64 && sudo apt-get update && sudo apt-get --yes install gcc-aarch64-linux-gnu g++-aarch64-linux-gnu libudev-dev:arm64 libusb-1.0-0-dev:arm64 && sudo make clean && sudo ./bootstrap && sudo ./configure --host=aarch64-linux-gnu CC=aarch64-linux-gnu-gcc && sudo make'
    if [[ "$?" -ne 0 ]]
      then
        echo -e "\033[31mFailed\033[0m - Removing damaged targets"
        rm ../../Java/Personal/hid4java/src/main/resources/linux-aarch64/libhidapi.so
        rm ../../Java/Personal/hid4java/src/main/resources/linux-aarch64/libhidapi-libusb.so
      else
        echo -e "\033[32mOK\033[0m"
        cp linux/.libs/libhidapi-hidraw.so ../../Java/Personal/hid4java/src/main/resources/linux-aarch64/libhidapi.so
        cp libusb/.libs/libhidapi-libusb.so ../../Java/Personal/hid4java/src/main/resources/linux-aarch64/libhidapi-libusb.so
    fi
  else
    echo -e "\033[33mSkipping linux-aarch64\033[0m"
fi
echo -e "\033[32m------------------------------------------------------------------------\033[0m"

# 32-bit ARMv6 EABI (linux-armel)
if [[ "$1" == "all" ]] || [[ "$1" == "linux" ]] || [[ "$1" == "linux-armel" ]]
  then
    echo -e "\033[32mBuilding ARMv6 EABI\033[0m"
    dockcross-linux-armv6 bash -c 'sudo dpkg --add-architecture armhf && sudo apt-get update && sudo apt-get --yes install gcc-arm-linux-gnueabihf libudev-dev:armhf libusb-1.0-0-dev:armhf && sudo make clean && sudo ./bootstrap && sudo ./configure --host=arm-linux-gnueabihf && sudo make'
    if [[ "$?" -ne 0 ]]
      then
        echo -e "\033[31mFailed\033[0m - Removing damaged targets"
        rm ../../Java/Personal/hid4java/src/main/resources/linux-armel/libhidapi.so
        rm ../../Java/Personal/hid4java/src/main/resources/linux-armel/libhidapi-libusb.so
      else
        echo -e "\033[32mOK\033[0m"
        cp linux/.libs/libhidapi-hidraw.so ../../Java/Personal/hid4java/src/main/resources/linux-armel/libhidapi.so
        cp libusb/.libs/libhidapi-libusb.so ../../Java/Personal/hid4java/src/main/resources/linux-armel/libhidapi-libusb.so
    fi
  else
    echo -e "\033[33mSkipping linux-armel\033[0m"
fi
echo -e "\033[32m------------------------------------------------------------------------\033[0m"

# 32-bit ARMv7 hard float (linux-arm)
if [[ "$1" == "all" ]] || [[ "$1" == "linux" ]] || [[ "$1" == "linux-arm" ]]
  then
    echo -e "\033[32mBuilding ARMv7 hard float\033[0m"
    dockcross-linux-armv7 bash -c 'sudo dpkg --add-architecture armhf && sudo rm -Rf /var/lib/apt/lists && sudo apt-get update && sudo apt-get --yes install libudev-dev:armhf libusb-1.0-0-dev:armhf gcc-arm-linux-gnueabihf && sudo make clean && sudo ./bootstrap && sudo ./configure --host=arm-linux-gnueabihf CC=arm-linux-gnueabihf-gcc && sudo make'
    if [[ "$?" -ne 0 ]]
      then
        echo -e "\033[31mFailed\033[0m - Removing damaged targets"
        rm ../../Java/Personal/hid4java/src/main/resources/linux-arm/libhidapi.so
      else
        echo -e "\033[32mOK\033[0m"
        cp linux/.libs/libhidapi-hidraw.so ../../Java/Personal/hid4java/src/main/resources/linux-arm/libhidapi.so
        cp libusb/.libs/libhidapi-libusb.so ../../Java/Personal/hid4java/src/main/resources/linux-arm/libhidapi-libusb.so
    fi
  else
    echo -e "\033[33mSkipping linux-arm\033[0m"
fi
echo -e "\033[32m------------------------------------------------------------------------\033[0m"

# OS X environments

# Darwin
if [[ "$1" == "all" ]] || [[ "$1" == "osx" ]] || [[ "$1" == "darwin" ]]
  then
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
  else
    echo -e "\033[33mSkipping darwin\033[0m"
fi
echo -e "\033[32m------------------------------------------------------------------------\033[0m"

# List all file info
echo -e "\033[32mResulting build files placed in hid4java:\033[0m"

# Windows environments
echo -e "\033[32mWindows\033[0m"

echo -e "\033[32mwin32-x86-64\033[0m"
file -b ../../Java/Personal/hid4java/src/main/resources/win32-x86-64/hidapi.dll

echo -e "\033[32mwin32-x86\033[0m"
file -b ../../Java/Personal/hid4java/src/main/resources/win32-x86/hidapi.dll

echo -e "\033[32m------------------------------------------------------------------------\033[0m"

# Linux environments
echo -e "\033[32mLinux\033[0m"

echo -e "\033[32mlinux-x86-64\033[0m"
file -b ../../Java/Personal/hid4java/src/main/resources/linux-x86-64/libhidapi.so
file -b ../../Java/Personal/hid4java/src/main/resources/linux-x86-64/libhidapi-libusb.so

echo -e "\033[32mlinux-amd64\033[0m"
file -b ../../Java/Personal/hid4java/src/main/resources/linux-amd64/libhidapi.so
file -b ../../Java/Personal/hid4java/src/main/resources/linux-amd64/libhidapi-libusb.so

echo -e "\033[32mlinux-x86\033[0m"
file -b ../../Java/Personal/hid4java/src/main/resources/linux-x86/libhidapi.so
file -b ../../Java/Personal/hid4java/src/main/resources/linux-x86/libhidapi-libusb.so

echo -e "\033[32m------------------------------------------------------------------------\033[0m"

# ARM
echo -e "\033[32mARM\033[0m"

echo -e "\033[32mlinux-arm\033[0m"
file -b ../../Java/Personal/hid4java/src/main/resources/linux-arm/libhidapi.so
file -b ../../Java/Personal/hid4java/src/main/resources/linux-arm/libhidapi-libusb.so

echo -e "\033[32mlinux-armel\033[0m"
file -b ../../Java/Personal/hid4java/src/main/resources/linux-armel/libhidapi.so
file -b ../../Java/Personal/hid4java/src/main/resources/linux-armel/libhidapi-libusb.so

echo -e "\033[32mlinux-aarch64\033[0m"
file -b ../../Java/Personal/hid4java/src/main/resources/linux-aarch64/libhidapi.so
file -b ../../Java/Personal/hid4java/src/main/resources/linux-aarch64/libhidapi-libusb.so

echo -e "\033[32m------------------------------------------------------------------------\033[0m"

# OS X
echo -e "\033[32mOS X\033[0m"

echo -e "\033[32mdarwin\033[0m"
file -b ../../Java/Personal/hid4java/src/main/resources/darwin/libhidapi.dylib

echo -e "\033[32m------------------------------------------------------------------------\033[0m"

echo -e "\033[32mDone - Check all OK in summary above.\033[0m"
