package com.still;

import akka.actor.UntypedAbstractActor;
import com.still.Appium.AppiumAgent;
import com.still.Graph.UIElement;
import com.still.Graph.UIGraph;
import com.still.Graph.ViewNode;
import com.still.Utils.AndroidUtils;
import com.still.Utils.HierarchyUtil;
import com.still.Utils.LogUtils;
import com.still.Utils.Timer;
import io.appium.java_client.android.AndroidElement;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;
import org.joda.time.Period;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;



class StartTravel{
    String apkFilePath;
    public StartTravel(String apkFilePath){
        this.apkFilePath = apkFilePath;
    }
}
class NextApk{}
class Active{}
class Done{}

class TravelRS{
    Period cost;
    TravelResult status;
    String pkgName;
    String apkFileName;

    public TravelRS(Period cost, TravelResult st, String pkgName, String apkFileName){
        this.cost = cost;
        this.status = st;
        this.pkgName = pkgName;
        this.apkFileName = apkFileName;
    }
}

enum TravelResult{
    Complete, LoginUiFound, Fail
}

public class AppTraversal {
    String screenShotLogDir;

    public String getActivityName(String view) {
        String[] s = view.split("_");
        String name = "";
        for (int i = 0; i < s.length - 1; i++) {
            name += s[i];
        }
        return name;
    }

    String appPath;
    String appPackage;
    private AppiumAgent appiumAgent;

    int maxDepth;
    Logger log;
    Map<String, Integer> depthMap;
    Set<String> pageSourceMap;

    UIElement lastClickedElement;

    Stack<String> jumpStack;

    String lastView() {
        if (jumpStack.isEmpty()) {
            return "";
        } else {
            return jumpStack.peek();
        }
    }

    UIGraph uiGraph;

    class ShouldRestartAppException extends RuntimeException {
    }

    ;

    class UnexpectedViewException extends RuntimeException {
    }

    ;

    class LoginUiFoundException extends RuntimeException {
        String loginUi;

        public LoginUiFoundException(String loginUi) {
            this.loginUi = loginUi;
        }
    }

    ;

    Map<String, UIElement> elements;

    public final List<UIElement> getClickableElements(String view) {
        return getClickableElements(view, 1);
    }

    public final List<UIElement> getClickableElements(String view, int retryTime) {
        List<AndroidElement> elems = appiumAgent.findElements(By.xpath("//*[@clickable='true']"));
        List<UIElement> uiElements = new ArrayList<>();
        for (AndroidElement i : elems) {
            uiElements.add(new UIElement(i));
        }
        if (retryTime == 0 || !uiElements.isEmpty()) {
            return uiElements;
        } else {
            log.info("Cannot find any element; Sleep and try again");
            uiGraph.getNode(view).shouldWait = true;
            try {
                Thread.sleep(3000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return getClickableElements(view, 0);
        }
    }

    public void checkPermissions() {
        log.info("Checking All Permissions");
        log.info("Confirm");
        List<AndroidElement> elems = appiumAgent.findElements(By.xpath(
                "//*[@resource-id='com.android.packageinstaller:id/switchWidget']"));
        for (AndroidElement i : elems) {
            UIElement uiElement = new UIElement(i);
            if (uiElement.text != "开") {
                uiElement.click();
            }
        }
    }

    public String getCurrentView() {
        try {
            return appiumAgent.driver.currentActivity() + "_" + HierarchyUtil.uiStructureHashDigest(appiumAgent.driver.getPageSource());
        } catch (NullPointerException e) {
            throw new ShouldRestartAppException();
        }
    }

    public void checkCurrentPackage() {
        if (appiumAgent.currentPackage() != appPackage) throw new ShouldRestartAppException();
    }

    public void checkAutoChange(String currentView, ViewNode currentNode) {
        String changed = getCurrentView();
        if (!changed.equals(currentView) && !currentNode.hasAlias(changed)) {
            changed = getCurrentView();
            log.info("Automated changed to view: " + changed);
            if (getActivityName(changed) == getActivityName(currentView)) {
                currentNode.addAlias(changed);
            } else {
                setCurrentViewAndNode(currentNode.depth);
            }
        }
    }

    Boolean loginUiFound;
    String currentView;
    ViewNode currentNode;

    public void setCurrentViewAndNode(int originDepth) {
        currentView = getCurrentView();
        currentNode = uiGraph.getNode(currentView);
        if (currentNode.depth == -1) {
            currentNode.depth = originDepth + 1;
        }
        appiumAgent.takeScreenShot(screenShotLogDir, currentNode.name);
        log.info("Current at " + currentView);
        log.info("Current at node" + currentNode.id);
        log.info("Current traversal depth is " + currentNode.depth);
    }

    public void allowPermission() {
        List<AndroidElement> elems = appiumAgent.findElements(By.xpath(
                "//*[@resource-id='com.android.packageinstaller:id/permission_allow_button']"));
        List<UIElement> permissionAllow = new ArrayList<>();
        for (AndroidElement i : elems) {
            permissionAllow.add(new UIElement(i));
        }
        while (!permissionAllow.isEmpty()) {
            log.info("Got the button");
            permissionAllow.forEach(UIElement::click);
            try {
                Thread.sleep(3000);
            } catch (Exception e) {
                e.printStackTrace();
            }

            elems.clear();
            elems = appiumAgent.findElements(By.xpath(
                    "//*[@resource-id='com.android.packageinstaller:id/permission_allow_button']"));
            permissionAllow.clear();
            for (AndroidElement i : elems) {
                permissionAllow.add(new UIElement(i));
            }
        }
    }

    public void tryLogin() {
        List<AndroidElement> elems = appiumAgent.findElements(By.xpath("//*[contains(@text,'微信登录')]"));
        List<UIElement> weixinLogin = new ArrayList<>();
        for (AndroidElement i : elems) {
            weixinLogin.add(new UIElement(i));
        }
        if (!weixinLogin.isEmpty()) {
            try {
                log.info("Find WeiXin Login");
                appiumAgent.takeScreenShot(screenShotLogDir, "weixinlogin");
                weixinLogin.get(0).click();
                Thread.sleep(5000);
                appiumAgent.takeScreenShot(screenShotLogDir, "weixin");
                Thread.sleep(5000);
                Runtime.getRuntime().exec("adb shell input tap 500 1200");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            elems.clear();
            elems = appiumAgent.findElements(By.xpath("//*[contains(@text,'QQ登录')]"));
            List<UIElement> qqLogin = new ArrayList<>();
            for (AndroidElement i : elems) {
                qqLogin.add(new UIElement(i));
            }
            if (!qqLogin.isEmpty()) {
                try {
                    log.info("Find QQ Login");
                    appiumAgent.takeScreenShot(screenShotLogDir, "QQlogin");
                    qqLogin.get(0).click();
                    Thread.sleep(5000);
                    appiumAgent.takeScreenShot(screenShotLogDir, "QQ");
                    Thread.sleep(5000);

                    elems.clear();
                    elems = appiumAgent.findElements(By.xpath("//*[@text='授权并登录']"));
                    List<UIElement> qqAllowButton = new ArrayList<>();
                    for (AndroidElement i : elems) {
                        qqAllowButton.add(new UIElement(i));
                    }
                    qqAllowButton.get(0).click();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                elems.clear();
                elems = appiumAgent.findElements(By.xpath("//*[contains(@text,'微博登录')]"));
                List<UIElement> weiboLogin = new ArrayList<>();
                for (AndroidElement i : elems) {
                    weiboLogin.add(new UIElement(i));
                }
                if (!weiboLogin.isEmpty()) {
                    try {
                        log.info("Find weibo Login");
                        appiumAgent.takeScreenShot(screenShotLogDir, "weibologin");
                        weiboLogin.get(0).click();
                        Thread.sleep(5000);
                        appiumAgent.takeScreenShot(screenShotLogDir, "weibo");
                        Thread.sleep(5000);
                        elems.clear();
                        elems = appiumAgent.findElements(By.xpath("//*[@text='确定']"));
                        List<UIElement> weiboAllowButton = new ArrayList<>();
                        for (AndroidElement i : elems) {
                            weiboAllowButton.add(new UIElement(i));
                        }
                        weiboAllowButton.get(0).click();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void heuristicMethod() {
        ArrayList<String> list = new ArrayList<>(Arrays.asList("login",
                "登录",
                "我",
                "个人中心"));

        List<AndroidElement> elems = appiumAgent.findElements(By.xpath("//*"));
        List<UIElement> precedenceElements = new ArrayList<>();
        for (AndroidElement i : elems) {
            precedenceElements.add(new UIElement(i));
        }
    }

    public void traversal(String expectView, int steps, int number) {
        log.info("Steps:" + steps);
        if (steps <= 0) return;

        //安卓6.0以后的动态权限，需要确认
        allowPermission();
        log.info("Permissions Allowed!");

        //第三方登录
        log.info("Try Login");
        tryLogin();
        log.info("Login Finished");

        currentView = getCurrentView();
        currentNode = uiGraph.getNode(currentView);
        appiumAgent.takeScreenShot(screenShotLogDir, currentNode.name);

        log.info("Current at " + currentView);
        log.info("Current at node" + currentNode.id);
        log.info("Current traversal depth is " + currentNode.depth);

        System.out.println(AndroidUtils.getCurrentPackage()+" : "+appPackage);
        if (!AndroidUtils.getCurrentPackage().equals(appPackage)) {
            log.info("Quit because of CurrentPackage!=appPackage");
            return;
        }

        if (!expectView.equals("") && !currentView.equals(expectView)) {
            log.info("Automated changed to view: " + currentView + "; Add alias");
            if (getActivityName(expectView) == getActivityName(currentView)) {
                currentNode.addAlias(expectView);
            }
        }

        if (!pageSourceMap.contains(currentView)) {
            LogUtils.logLayout(currentView, appiumAgent.driver.getPageSource());
            appiumAgent.takeScreenShot(screenShotLogDir, currentView);
            pageSourceMap.add(currentView);
            log.info("pageSourceMap add currentView" + currentView);
            checkAutoChange(currentView, currentNode);
        }

        Boolean nodeVisited = currentNode.visited;
        currentNode.visited = true;

        //可以在此处添加启发式策略
        //heuristicMethod()
        //scala code

        List<UIElement> clickableElements = getClickableElements(currentView);

        log.info(clickableElements.size() + " clickable elements found on view");

        int sum = 0;
        try {
            for (UIElement element : clickableElements) {
                currentNode.elementsVisited.put(element, true);
                sum = sum + 1;
                if (sum >= number) {
                    try {
                        log.info("Click " + element.toString());
                        element.click();
                        Thread.sleep(3000);
                        lastClickedElement = element;

                        allowPermission();

                        tryLogin();

                        String viewAfterClick = getCurrentView();
                        element.destView = viewAfterClick;

                        // 判断是否变换了view应当根据node而非view
                        if (!currentNode.hasAlias(viewAfterClick)) {
                            log.info("Jumped to view " + viewAfterClick);
                            if (!appiumAgent.currentPackage().equals(appPackage)) {
                                log.info("Jumped out of App");
                                log.info("Current at app " + appPackage);

                                element.willJumpOutOfApp = true;
                                //if (pkg == "com.sec.android.app.capabilitymanager") checkPermissions()
                                //samsung's android 6.0 have changed the package name
                                //if (pkg == "com.samsung.android.packageinstaller") checkPermissions()


                                log.info("Try back to app");

                                back();

                                // 如果无法回到原App, 重新启动App
                                checkCurrentPackage();
                            } else {
                                currentNode.addEdge(element);

                                //jumpStack.push(currentView)
                                traversal(viewAfterClick, steps - 1, 1);

                                if (!currentView.equals(getCurrentView())) back();
                                log.info("View after back: " + getCurrentView());
                                if (!currentView.equals(getCurrentView())) back();
                                if (!currentView.equals(getCurrentView())) throw new ShouldRestartAppException();
                            }
                        }

                    } catch (org.openqa.selenium.NoSuchElementException e1) {
                        // 这个exception会在某个element被点击但不存在的时候出现
                        // 该被点击的Element在上面已经被标记为visited, 但并未实际访问过
                        // 因此需要将状态复原
                        currentNode.elementsVisited.put(element, false);

                        checkCurrentPackage();

                        // 如果是在App内的某个View, 但不是当前应该在的View, 则跳过当前所有element
                        // 可能由于上一个element访问后未back到本View导致
                        if (!currentNode.hasAlias(getCurrentView())) throw new UnexpectedViewException();

                        //                  checkAutoChange(currentView, currentNode)


                        log.info("Cannot locate element");
                        log.info("Reload clickable elements");
                        clickableElements = getClickableElements(currentView);
                        currentNode.addAllElement(clickableElements);
                        checkAutoChange(currentView, currentNode);
                        log.info(clickableElements.size() + " elements found");
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
            }
            if (currentNode.hasAlias(getCurrentView())) back();
        } catch (ShouldRestartAppException e1) {
            log.warn("ShouldRestartApp");
            restartApp();
            traversal("", steps, sum);
        } catch (UnexpectedViewException e2) {
            log.info("View changed unexpected");
            log.info("Current view is " + getCurrentView());
            // Do nothing but jump out of inner foreach loop
        }

    }

    public void back() {
        log.info("Back");
        appiumAgent.driver.navigate().back();
    }

    public void restartApp() {
        log.info("Restart App");
        appiumAgent.driver.resetApp();
    }

    int traversalTimeout;

    public void initAppiumAgent(String currentPackage, String currentActivity) {
        try {
            appiumAgent = new AppiumAgent(appPath, currentPackage, currentActivity);
        } catch (org.openqa.selenium.SessionNotCreatedException e1) {
            GlobalConfig.server.restart();
            try {
                Thread.sleep(5000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            initAppiumAgent(currentActivity, currentActivity);
        } catch (org.openqa.selenium.remote.UnreachableBrowserException e2) {
            GlobalConfig.server.restart();
            try {
                Thread.sleep(5000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            initAppiumAgent(currentActivity, currentActivity);
        }
    }

    public TravelResult start() {
        try {
            log.info("Start testing apk: " + appPath);
            log.info("Package name: " + appPackage);

            AndroidUtils.installApk(appPath);

            log.info("Apk installed!");
            Thread.sleep(3000);
            log.info("After 3 seconds");

            AndroidUtils.startApk(appPath);

            log.info("Apk started!");
            Thread.sleep(3000);
            log.info("After 3 seconds");

            String currentPackage = AndroidUtils.getCurrentPackage();
            String currentActivity = AndroidUtils.getCurrentActivity();
            log.info("currentPackage: " + currentPackage);
            log.info("currentActivity: " + currentActivity);

            // 如果初始化不了drvier就给我一直重启吧
            initAppiumAgent(currentPackage, currentActivity);

            log.info("AppiumAgent started");

            screenShotLogDir = LogUtils.caseLogDir;

            log.info("Traversal started");

            FutureTask traversalTask = new FutureTask(new Callable() {
                @Override
                public Object call() throws Exception {
                    for (int x = 1; x < 6; x++) {
                        log.info("Traversal start: " + x + " steps");
                        traversal("", x, 1);
                        log.info("Traversal stop: " + x + " steps");
                        restartApp();
                        Thread.sleep(5000);
                    }
                    return null;
                }
            });

            try {
                new Thread(traversalTask).start();
                traversalTask.get(traversalTimeout, TimeUnit.MINUTES);
            } catch (java.util.concurrent.ExecutionException e) {
                e.getCause();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (loginUiFound) return TravelResult.LoginUiFound;
            else return TravelResult.Complete;
        } catch (LoginUiFoundException e) {
            log.warn("Login Ui Found: " + e.loginUi + " in package " + this.appPackage + " at $appPath");
            return TravelResult.LoginUiFound;
        } catch (TimeoutException e) {
            log.warn("Timeout!");
            return TravelResult.Fail;
        } catch (org.openqa.selenium.WebDriverException e) {
            LogUtils.printException(e);
            log.error("Unknown appium exception");
            return TravelResult.Fail;
        } catch (Exception e) {
            LogUtils.printException(e);
            return TravelResult.Fail;
        } finally {
            log.info("Take screenShot on quit");
            if (appiumAgent != null) {
                appiumAgent.takeScreenShot(screenShotLogDir, "Quit");
                uiGraph.saveXmlAndDotFile(LogUtils.siteXmlPath());
                try {
                    Runtime.getRuntime().exec("dot " + LogUtils.packagelogDir + "/site.dot -Tpng -o " + LogUtils.packagelogDir + "/site.png");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                log.info("Remove app from device");
                appiumAgent.removeApp(appPackage);
                log.info("Quit");
                appiumAgent.quit();
                appiumAgent = null;
            }
        }
    }

    public AppTraversal(String apkFullPath, String pkgName){
        screenShotLogDir = "";
        appPath = apkFullPath;

        appPackage = pkgName;
        appiumAgent = null;

        maxDepth = 100;
        log = Logger.getLogger(Automator.class);
        depthMap = new HashMap<>();
        pageSourceMap = new HashSet<>();

        lastClickedElement = null;
        jumpStack = new Stack<>();

        uiGraph = new UIGraph(appPackage, appPath);

        elements = new HashMap<>();

        loginUiFound = false;
        currentView = null;
        currentNode = null;

        traversalTimeout = 30;

    }
}

class TravelMonitor extends UntypedAbstractActor{

    Logger log = LogUtils.getLogger();
    Timer timer = new Timer();

    @Override
    public void onReceive(Object message) {

        if(message instanceof Active){

            getSender().tell(new NextApk(), getSelf());
        }else if(message instanceof StartTravel){
            String apkFilePath = ((StartTravel) message).apkFilePath;
            switch (apkFilePath){
                case "":
                    getSender().tell(new Done(), getSelf());
                    break;
                default:
                    String packageName = AndroidUtils.getPackageName(apkFilePath);
                    LogUtils.initLogDirectory(packageName);

                    // 初始化WriterAppender, 将这段log发送往指定文件
                    Writer writer = null;
                    try {
                        writer = new PrintWriter(LogUtils.caseLogPath());
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    WriterAppender appender = new WriterAppender(new PatternLayout("%-d{yyyy-MM-dd HH:mm:ss} [%p] %m%n"), writer);
                    appender.setName(packageName);
                    appender.setImmediateFlush(true);
                    log.addAppender(appender);

                    timer.start();

                    TravelResult travelResult = null;
                    try{
                        travelResult = new AppTraversal(apkFilePath, packageName).start();
                    }catch (Exception e){
                        LogUtils.printException(e);
                        travelResult =TravelResult.Fail;
                    }

                    Period period = timer.stop();
                    //重新去除appender
                    log.removeAppender(appender);
                    try {
                        writer.flush();
                        writer.close();
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    getSender().tell(new TravelRS(period, travelResult, apkFilePath, packageName), getSelf());
                    getSender().tell(new NextApk(), getSelf());
                    break;
            }
        }

    }
}