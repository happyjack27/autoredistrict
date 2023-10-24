#!/bin/bash
set -e
rm -rf bin
mkdir bin
javac -encoding UTF-8 -d bin java
cd bin
jar cf autoredistrict.jar .
