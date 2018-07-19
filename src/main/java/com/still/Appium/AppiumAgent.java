package com.still.Appium;

import com.still.Automator;
import com.still.Utils.AndroidUtils;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidElement;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public class AppiumAgent {
    DesiredCapabilities capabilities;

    public AndroidDriver driver;

    Logger log;
    int screenShotCounter;

    public AppiumAgent(String appPath, String Package, String Activity){
        capabilities = new DesiredCapabilities();
        capabilities.setCapability("deviceName", "OnePlus 5T");
        capabilities.setCapability("platformVersion", "8.1.0");
        capabilities.setCapability("platformName", "Android");
        capabilities.setCapability("app", appPath);
        capabilities.setCapability ("appWaitPackage", Package);
        capabilities.setCapability ("appWaitActivity", Activity);

        try {
            driver = new AndroidDriver<AndroidElement>(new URL("http://localhost:4723/wd/hub"), capabilities);
            driver.setLogLevel(Level.INFO);
        }catch (Exception e){
            e.printStackTrace();
        }
        log = Logger.getLogger(Automator.class);

        screenShotCounter = 0;

    }

    public void takeScreenShot(String logDir, String fn){
        byte[] screenShotFile = driver.getScreenshotAs(OutputType.BYTES);

        String filename;
        if(fn == null){
            filename = String.valueOf(screenShotCounter) + ".png";
        }else {
            filename = fn + ".png";
        }

        try {
            FileUtils.writeByteArrayToFile(Paths.get(logDir, filename).toFile(), screenShotFile);
        }catch (Exception e){
            e.printStackTrace();
        }

        screenShotCounter += 1;
    }

    public void swipeToLeft(){
        int width = driver.manage().window().getSize().width;
        int height = driver.manage().window().getSize().height;
        driver.swipe(width*3/4, height/2, width/4, height/2, 500);
    }

    public String currentActivity(){
        return driver.currentActivity();
    }

    public String currentPackage(){
        return AndroidUtils.getCurrentPackage();
    }

    public void pressKeyCode(int key){
        driver.pressKeyCode(key);
    }

    public List<AndroidElement> findElements(By by){
        return driver.findElements(by);
    }

    public void getUrl(){
        Set<String> contextNames = driver.getContextHandles();
        for(String i : contextNames){
            log.info("contextNames:"+i);
        }

        String saveContext = driver.getContext();
        for(String i : contextNames){
            if(i.contains("WEBVIEW")){
                driver.context(i);
                log.info("WebView:"+driver.toString());
                try {
                    log.info("CurrentUrl:" + driver.getCurrentUrl());
                }catch (org.openqa.selenium.WebDriverException e){
                    log.info("GetUrl meet WebDriverException");
                }
            }
        }
        driver.context(saveContext);
    }

    public void quit(){
        driver.quit();
    }

    public void closeApp(){
        driver.closeApp();
    }

    public void installApp(String apkPath){
        driver.installApp(apkPath);
    }

    public void removeApp(String bundleId){
        driver.removeApp(bundleId);
    }

    public void startActivity(String appPackage, String activity){
        driver.startActivity(appPackage, activity);
    }
}
