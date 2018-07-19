package com.still;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedAbstractActor;
import com.still.Utils.LogUtils;
import com.still.Utils.Timer;
import org.apache.log4j.Logger;
import org.joda.time.Period;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

class TraversalTestStart{}
class TraversalTestDone{
    int testedApkNumber;
    public TraversalTestDone(int testedApkNumber){
        this.testedApkNumber = testedApkNumber;
    }
}

public class MultiAppTester extends UntypedAbstractActor {

    Logger log;
    int appTested;
    int appIgnored;
    List<String> failedAppList;
    List<String> loginFoundAppList;
    int totalApkCount;
    int totalTimeInSeconds;
    int loginUiFoundAppTimeCost;

    Timer fullTimer;

    List<ActorRef> travelers;

    int travelerNumber;
    ActorRef starter;

    Iterator<String> apkFileIterator;

    public MultiAppTester(List<String> ApkFiles){

        log = LogUtils.getLogger();
        appTested = 0;
        appIgnored = 0;
        failedAppList = new ArrayList<>();
        loginFoundAppList = new ArrayList<>();
        totalApkCount = ApkFiles.size();
        totalTimeInSeconds = 0;
        loginUiFoundAppTimeCost = 0;

        fullTimer = new Timer();

        starter = null;

        apkFileIterator =ApkFiles.iterator();

        travelers = new ArrayList<>(Arrays.asList(context().actorOf(Props.create(TravelMonitor.class))));
        travelerNumber = travelers.size();
    }

    public MultiAppTester(String apkDirectoryRoot){
        List<String> ApkFiles = new ArrayList<>();
        for(String apkPath : new File(apkDirectoryRoot).list()){
            ApkFiles.add(Paths.get(apkDirectoryRoot, apkPath).toString());
        }

        log = LogUtils.getLogger();
        appTested = 0;
        appIgnored = 0;
        failedAppList = new ArrayList<>();
        loginFoundAppList = new ArrayList<>();
        totalApkCount = ApkFiles.size();
        totalTimeInSeconds = 0;
        loginUiFoundAppTimeCost = 0;

        fullTimer = new Timer();

        starter = null;

        apkFileIterator =ApkFiles.iterator();

        travelers = new ArrayList<>(Arrays.asList(context().actorOf(Props.create(TravelMonitor.class))));
        travelerNumber = travelers.size();
    }

    public String nextApkFile(){
        if(apkFileIterator.hasNext()) return apkFileIterator.next(); else return null;
    }

    @Override
    public void onReceive(Object message) {
        switch (message.getClass().getName()){
            case "com.still.TraversalTestStart":
                fullTimer.start();
                starter = getSender();

                for(ActorRef i : travelers){
                    i.tell(new Active(), getSelf());
                }
                break;

            case "com.still.NextApk":

                getSender().tell(new StartTravel(nextApkFile()), getSelf());
                break;

            case "com.still.Done":
                travelerNumber -= 1;
                if (travelerNumber == 0) {
                    Period period = fullTimer.stop();
                    log.info("Automator test finish");
                    log.info(period.getHours() + " hours " + period.getMinutes() + " minutes " + period.getSeconds() + " seconds cost");

                    log.info(totalApkCount + " apps in total  " + appTested + " apps tested, " + appIgnored + " apps ignored, " + failedAppList.size() + " apps test failed, found " + loginFoundAppList.size() + " login Ui");

                    if (!failedAppList.isEmpty()) {
                        log.info("Failed App List");
                        for(String i : failedAppList){
                            log.info(i);
                        }
                    }

                    if (!loginFoundAppList.isEmpty()) {
                        log.info("Login Ui found in Apps: ");
                        for(String i : loginFoundAppList){
                            log.info(i);
                        }
                    }
                    getSender().tell(new TraversalTestDone(appTested), getSelf());
                }
                break;

            case "com.still.TravelRS":
                TravelRS tr = (TravelRS)message;
                appTested += 1;
                int seconds = tr.cost.toStandardSeconds().getSeconds();
                totalTimeInSeconds += seconds;
                double averageTime = Double.valueOf(totalTimeInSeconds) / appTested;

                switch (tr.status){
                    case LoginUiFound:
                        loginFoundAppList.add(tr.apkFileName);
                        loginUiFoundAppTimeCost += seconds;
                        break;

                    case Fail:
                        failedAppList.add(tr.apkFileName);
                        break;

                    case Complete:
                        break;
                }

                double loginUiFoundAppAverageTime = Double.valueOf(loginUiFoundAppTimeCost) / loginFoundAppList.size();

                log.info(seconds + " seconds used for " + tr.apkFileName);
                log.info(String.format("%.2f", averageTime) + " seconds cost for each apk in average ");
                log.info(appTested+ "/" + totalApkCount+ " apks already tested");
                log.info(loginFoundAppList.size() + " login Ui found");
                log.info(String.format("%.2f",loginUiFoundAppAverageTime) + " seconds cost for each apps in which login ui found ");
                log.info(String.format("%.2f", (totalApkCount - appTested) * averageTime) + " seconds remained");
                break;
        }

    }

}
