#!/bin/sh

adb shell dumpsys activity | grep top-activity | awk -F":" '{print $4}' | awk -F "/" '{print $1}'
