#!/bin/sh

version=2.2.0
platform=mac

exec java -d32 -XstartOnFirstThread -jar dist/spindleEditor_${platform}_${version}.jar
