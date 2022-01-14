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

**build** 

    $ mvn package

and Maven will create a jar-file for you depending on your operating system.

**run**
Run the jar file with 

    $ java -jar target/stegosuite-0.8.0.jar

or

    $ cd /home/MYUSERNAME/git/stegosuite ; \ 
    /usr/bin/env /usr/lib/jvm/java-11-openjdk-amd64/bin/java \
    @/tmp/cp_3b4a96dt4lsiq0kj8rkf0pfav.argfile \
    org.stegosuite.Stegosuite 

users of Debian-based Linux distributions (Debian, Ubuntu, Mint...) can run

    mvn jdeb:jdeb

afterwards to build a deb-package.

## source
https://archive.org/download/stegosuite
