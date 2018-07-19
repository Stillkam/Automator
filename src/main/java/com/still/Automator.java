package com.still;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.util.Timeout;
import com.still.Appium.AppiumServer;
import org.apache.commons.cli.*;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static akka.pattern.Patterns.ask;

public class Automator {

    public static void main(String[] args) {
//        System.out.println(AndroidUtils.executeCmd("which aapt"));

        System.setProperty("log4j.appender.file.File", "log");
        System.setProperty("log4j.configurationFile", "Resources/config/log4j.xml");

        PropertyConfigurator.configure("Resources/properties/log4j.properties");
        Logger log = Logger.getLogger(Automator.class);

        Options options = new Options( );
        options.addOption("h", "help", false, "Print this usage information");
        options.addOption("m","mode",true,"Test mode for automator");
        options.addOption("af","apkfile",true,"Apk to test");
        options.addOption("ad","apkDirectory",true,"Directory contains apks to test");
        options.addOption("cf","configFile",true,"specified config file");
        options.addOption("afl","apkFileList",true,"apks to test");
        options.addOption("f","fast",true,"Fast mode(Skip tested apps)");

        Configure conf = new Configure();
        Boolean success = parseOptions(options, args, conf);
        if(success){
            System.out.println("Parse Options successfully");
        }else {
            System.out.println("Invalid command line options");
            return;
        }

        if(conf.configFile == null){
            if(conf.mode.equals("SingleAppTest")){
                if(conf.apkFile == null){
                    System.out.println("apk file not specified");
                    return;
                }
            } else if(conf.mode.equals("MultiAppTest")){
                if(conf.apkDirectory == null && conf.apkFileList == null){
                    System.out.println("apk files not specified");
                    return;
                }
            } else {
                System.out.println("invalid mode");
                return;
            }
        }

        ActorRef mainTester = null;

        ActorSystem system = ActorSystem.create("Automator");
        AppiumServer appiumServer = new AppiumServer();

        if(conf.configFile != null){
            SAXReader reader = new SAXReader();
            Document document = null;
            try {
                document = reader.read(new File(conf.configFile));
            }catch (Exception e){
                e.printStackTrace();
            }
            String apkDirectoryRoot = document.selectSingleNode("//apkDirectory").getText();
            String mode = document.selectSingleNode("//mode").getText();
            switch (mode){
                case "SingleAppTest":
                    List<String> list = new ArrayList<>(Arrays.asList(apkDirectoryRoot + "/" + document.selectSingleNode("apkFile").getText()));
                    mainTester = system.actorOf(Props.create(MultiAppTester.class, list));
                    break;
                case "MultiAppTest":
                    mainTester = system.actorOf(Props.create(MultiAppTester.class, apkDirectoryRoot));
                    break;
                default:
                    System.out.println("Invalid mode");
                    break;
            }
        }else {
            switch (conf.mode){
                case "SingleAppTest":
                    List<String> list = new ArrayList<>(Arrays.asList(Paths.get(conf.apkDirectory, conf.apkFile).toString()));
                    mainTester = system.actorOf(Props.create(MultiAppTester.class, list));
                    break;
                case "MultiAppTest":
                    mainTester = system.actorOf(Props.create(MultiAppTester.class, conf.apkFileList));
                    break;
                default:
                    System.out.println("Invalid mode");
                    break;
            }
        }

        GlobalConfig.fast = conf.fast;
        log.info("Automator test started");
        GlobalConfig.server = appiumServer;
        appiumServer.start();

//         For infinite timeout
        Timeout timeout = new Timeout(Duration.create(200, TimeUnit.HOURS));

        Future future = ask(mainTester, new TraversalTestStart(),timeout);
        try {
            Await.result(future, Duration.create(200, TimeUnit.HOURS));
        }catch (Exception e){
            e.printStackTrace();
        }
        log.info("Automator test Done");
        appiumServer.stop();
        system.terminate();
    }

    private static Boolean parseOptions(Options opt,String[] args, Configure conf){
        HelpFormatter formatter = new HelpFormatter();
        CommandLineParser parser = new DefaultParser();
        CommandLine cl = null;
        String format = "automator -m <MODE> -ad <APKDIRECOTRY> [-af] [afl] [f]";
        try {
            // 处理Options和参数
            cl = parser.parse( opt, args );
        } catch (ParseException e) {
            formatter.printHelp(format, opt); // 如果发生异常，则打印出帮助信息
        }
        if(cl.hasOption("h")){
            formatter.printHelp(format, opt);
            return false;
        }
        if(cl.hasOption("cf")){
            conf.configFile = cl.getOptionValue("cf");
        }
        if(cl.hasOption("m")){
            conf.mode = cl.getOptionValue("m");
        }else {
            System.out.println("Mode must be specified!");
            return false;
        }
        if(cl.hasOption("af")){
            conf.apkFile = cl.getOptionValue("af");
        }
        if(cl.hasOption("ad")){
            conf.apkDirectory = cl.getOptionValue("ad");
        }

        if(cl.hasOption("afl")){
            conf.apkFileList = cl.getOptionValues("afl");
        }
        if(cl.hasOption("f")){
            conf.fast = Boolean.valueOf(cl.getOptionValue("f"));
        }
        return true;
    }

}
