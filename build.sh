#!/bin/bash
set -e
rm -rf bin
mkdir bin
javac -encoding UTF-8 -d bin -cp 'src:jcom.jar' src/ui/Applet.java
cd bin
jar cf autoredistrict.jar .
