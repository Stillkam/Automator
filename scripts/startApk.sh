#!/bin/sh

packageName=`./scripts/getPackageName.sh $1`
activityName=`./scripts/getActivityName.sh $1`
name=$packageName/$activityName
adb shell am start -n $name
