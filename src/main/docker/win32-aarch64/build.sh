#!/usr/bin/env bash

# Script to build Dockcross extension for Win32 Aarch64 on Clang

# Build the image
docker build -t win32-aarch64-clang .

## Creates a helper script named linux-armv7.
docker run win32-aarch64-clang > linux-armv7

## Gives the script execution permission.
chmod +x linux-armv7

## Runs the helper script with the argument "bash", which starts an interactive container using your extended image.
./linux-armv7 bash
