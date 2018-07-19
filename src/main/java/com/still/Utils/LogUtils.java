package com.still.Utils;

import com.still.Automator;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.util.Date;

public class LogUtils {

    static Logger log = Logger.getLogger(Automator.class);

    public static Logger getLogger(){
        return log;
    }

    public static void printException(Exception e){
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        log.warn(sw.toString());
    }

    // 每个app对应的log 目录
    public static String packagelogDir = "OutputFiles";

    // 每次测试的log目录
    public static String caseLogDir = "OutputFiles";

    public static void initLogDirectory(String currentPackage){
        String packagelogDir = Paths.get("log", currentPackage).toString();
        String caseLogDir = Paths.get(packagelogDir, new Date().toString().replace(' ', '_')).toString();
        File file = new File(caseLogDir);
        file.mkdirs();
    }

    public static void logLayout(String view, String layout){
        String viewLayoutPath = Paths.get(caseLogDir, view + ".xml").toString();
        OutputFormat format = OutputFormat.createPrettyPrint();
        try {
            Document doc = DocumentHelper.parseText(layout);
            FileOutputStream xml_out = new FileOutputStream(viewLayoutPath);
            XMLWriter xml_writer = new XMLWriter(xml_out, format);
            xml_writer.write(doc);
            xml_writer.close();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public static String siteXmlPath(){
        return Paths.get(packagelogDir, "site.xml").toString();
    }

    public static String dotFilePath(){
        return Paths.get(packagelogDir, "site.dot").toString();
    }

    public static String caseLogPath(){
        return Paths.get(caseLogDir, "log.out").toString();
    }

}
