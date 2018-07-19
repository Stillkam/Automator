#!/bin/sh

packageName=`./getPackageName.sh $1`
activityName=`./getActivityName.sh $1`
name=$packageName/$activityName
adb shell am start -n $name
