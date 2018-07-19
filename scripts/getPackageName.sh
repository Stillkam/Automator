#!/bin/sh

aapt dump badging $1 | grep -E -o  "package: name='([^']*)'" | awk -F"'" '{print $2}'