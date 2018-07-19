#!/bin/sh

aapt dump badging $1 | grep -E -o "launchable-activity: name='([^']*)'" | awk -F"'" '{print $2}'
