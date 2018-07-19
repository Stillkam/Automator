package com.still.Utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class AndroidUtils {

    public static String executeCmd(String cmd){
        try {
            Process process = Runtime.getRuntime().exec(cmd);
            InputStream std_err = process.getErrorStream();
            InputStream std_out = process.getInputStream();
            InputStreamReader isr_err = new InputStreamReader(std_err);
            InputStreamReader isr_out = new InputStreamReader(std_out);
            BufferedReader br_err = new BufferedReader(isr_err);
            BufferedReader br_out = new BufferedReader(isr_out);
            String s;
            if((s = br_out.readLine()) != null){
                return s;
            }else if((s = br_err.readLine()) != null){
                return s;
            }else {
                return "";
            }
        }catch (Exception e){
            e.printStackTrace();
            return "";
        }
    }

    public static String installApk(String apkPath){
        return executeCmd("scripts/installApk.sh " + apkPath);
    }

    public static String startApk(String apkPath){
        return executeCmd("scripts/startApk.sh " + apkPath);
    }

    public static String getPackageName(String apkPath){
        return executeCmd("scripts/getPackageName.sh " + apkPath);
    }

    public static String getCurrentPackage(){
        return executeCmd("scripts/getCurrentPackage.sh");
    }

    public static String getCurrentActivity(){
        return executeCmd("scripts/getCurrentActivity.sh");
    }

}
