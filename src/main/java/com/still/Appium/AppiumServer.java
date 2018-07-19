package com.still.Appium;


import com.still.Automator;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.URL;

public class AppiumServer {

    static Logger log;
    PrintWriter out;
    Process server;


    public AppiumServer(){
        log = Logger.getLogger(Automator.class);
        out = null;
        server = null;
    }

    public void start(){
        log.info("Starting Appium server");
        String logPath = "/Users/Still/Documents/Project/Automator/log/AppiumServer.log";
        try {

            DefaultExecutor server = new DefaultExecutor();
            OutputStream out = new FileOutputStream(logPath);
            PumpStreamHandler psh = new PumpStreamHandler(out);
            server.setStreamHandler(psh);
            DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
            server.execute(CommandLine.parse("appium"), resultHandler);

        }catch (Exception e){
            e.printStackTrace();
        }
        checkStatus();
        log.info("Appium Server started");
    }

    public void restart(){
        log.info("Restart Server");
        stop();
        start();
    }

    public String getStatus(){
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new URL("http://127.0.0.1:4723/wd/hub/status").openStream(), "utf-8"));
            return br.readLine();
        }catch (java.net.ConnectException e1){
            return null;
        }catch (Exception e2){
            e2.printStackTrace();
        }
        return null;
    }

    public void checkStatus(){
        String status = getStatus();
        if(status != null){
            log.info(status);
        }else {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            checkStatus();
        }
    }


    public void stop(){
        if (server != null) {
            server.destroy();
        }
        log.info("Appium server stopped");
    }

}
