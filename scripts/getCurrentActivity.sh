#!/bin/sh

adb shell dumpsys activity | grep mFocusedActivity | awk -F " " '{print $4}' | awk -F "/" '{print $2}'
