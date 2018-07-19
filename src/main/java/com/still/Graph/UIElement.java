package com.still.Graph;

import io.appium.java_client.android.AndroidElement;

import java.util.ArrayList;
import java.util.Arrays;

public class UIElement {

    static ArrayList<String> noClickTags = new ArrayList<>(Arrays.asList("android.widget.EditText", "android.widget.Spinner"));

    static ArrayList<String> backRegex = new ArrayList<>(Arrays.asList(".*[Bb]ack.*", ".*nav_left.*", "left_icon", "返回"));

    static ArrayList<String> blackListRegex = new ArrayList<>(Arrays.asList("否|([Nn]o)", "[cC]lear", "安装", "[Ii]nstall", "下载", "[dD]ownload", "下载"));

    public static String formatAndroidElement(AndroidElement elm){
        String format = "Tag:" + elm.getTagName() + ";"
                + "Text:" + elm.getText() + ";"
                + "resourceId:" + elm.getAttribute("resourceId") + ";"
                + "contentDesc:" + elm.getAttribute("name") + ";";
        return format;
    }

    public static String toUrl(String view, AndroidElement elm){
        return formatAndroidElement(elm);
    }

    AndroidElement androidElement;
    String id;

    public String text;

    String tagName;
    String resourceId;
    String contentDesc;
    ViewNode parentNode;
    Boolean willChangeCurrentUI;
    Boolean isBack;
    Boolean validTag;

    Boolean inBlackList;

    public String destView;

    String url;
    Boolean clicked;
    public Boolean willJumpOutOfApp;

    public UIElement(AndroidElement element){
        id = element.getId();
        text = element.getText();
        tagName = element.getTagName();
        String rsid = element.getAttribute ("resourceId");
        resourceId = Boolean.valueOf(rsid)? rsid : "";

        contentDesc = element.getAttribute("name");
        parentNode = null;

        willChangeCurrentUI = false;

        isBack = false;
        for(String i : Arrays.asList(resourceId, text, contentDesc)){
            if(this.isInBackRegex(i)){
                isBack = true;
                break;
            }
        }

        inBlackList = false;
        for(String i : Arrays.asList(resourceId, text, contentDesc)){
            if(this.isInBlackList(i)){
                inBlackList = true;
                break;
            }
        }

        validTag = !this.noClickTags.contains(tagName);

        destView = null;
        url = this.toString();
        clicked = false;
        willJumpOutOfApp = false;
        androidElement = element;
    }

    public Boolean isInBackRegex(String s){
        for(String i : UIElement.backRegex){
            if(i.indexOf(s) > -1){
                return true;
            }
        }
        return false;
    }

    public Boolean isInBlackList(String s){
        for(String i : UIElement.blackListRegex){
            if(i.indexOf(s) > -1){
                return true;
            }
        }
        return false;
    }

    public void click(){
        androidElement.click();
        clicked = true;
    }

    public Boolean shouldClick(){
        return !inBlackList && !isBack && validTag &&  //  Valid Check
                androidElement.isDisplayed() &&  // Display Check
                !parentNode.hasAlias(destView); // Route Check
    }

    public Boolean visited(){
        return clicked;
    }

    public Boolean destViewVisitComplete(){
        return willJumpOutOfApp || parentNode.parent.getNode(destView).visitComplete();
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof UIElement){
            return obj.toString() == this.toString();
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "Tag:" + tagName + ";"
                + "Text:" + text + ";"
                + "resourceId:" + resourceId + ";"
                + "contentDesc:" + contentDesc + ";";
    }
}
