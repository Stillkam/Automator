package com.still.Utils;
import org.joda.time.DateTime;
import org.joda.time.Period;

public class Timer {
    DateTime startTime;
    DateTime stopTime;

    public Timer(){
        startTime = null;
        stopTime = null;
    }

    public void start(){
        startTime = DateTime.now();
    }

    public Period stop(){
        stopTime = DateTime.now();
        return new Period(startTime,stopTime);
    }
}
