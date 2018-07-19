package com.still;

import com.still.Appium.AppiumServer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Configure {

    static String SingleAppTest = "SingleAppTest";
    static String MultiAppTest = "MultiAppTest";
    List<String> Modes = new ArrayList<>(Arrays.asList(SingleAppTest, MultiAppTest));

    String mode;
    String[] apkFileList;
    String apkFile;
    String configFile;
    String apkDirectory;
    Boolean fast;

    public Configure(){
        mode = "";
        apkFileList = null;
        apkFile = null;
        configFile = null;
        apkDirectory = "";
        fast = true;
    }

}

class GlobalConfig{
    static Boolean fast = true;
    static String currentPackage = "";
    static AppiumServer server = null;
}