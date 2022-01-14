Stegosuite  
==========

----------------------------------------------

Stegosuite is a free steganography tool written in Java.  
With Stegosuite you can hide information in image files.

## Features
* BMP, GIF, JPG and PNG supported
* AES encryption of embedded data
* Automatic avoidance of homogenous areas (only embed data in noisy areas)
* Embed text messages and multiple files of any type
* Easy to use

## Build instructions
To build the jar-file, Apache Maven and Java 8 need to be installed.


**Just run:** 

    mvn package

and Maven will create a jar-file for you depending on your operating system.


Users of Debian-based Linux distributions (Debian, Ubuntu, Mint...) can run

    mvn jdeb:jdeb
afterwards to build a deb-package.

## How to run
Run the jar file with java -jar.

## source
https://archive.org/download/stegosuite
