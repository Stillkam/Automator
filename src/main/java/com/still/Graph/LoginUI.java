package com.still.Graph;

import java.util.ArrayList;
import java.util.List;

public class LoginUI {
    static String[] loginRegex = {"登录", "login"};

    public static Boolean isLoginUI(UIElement lastClickedElement, List<UIElement> elementsOnActivity, String activityName){
        ArrayList<String> elementsToCheck = new ArrayList<>();
        if (lastClickedElement != null){
            elementsToCheck.add(lastClickedElement.text);
        }
        elementsToCheck.add(activityName);
        for(UIElement i : elementsOnActivity){
            elementsToCheck.add(i.resourceId);
            elementsToCheck.add(i.text);
        }
        for(String i : elementsToCheck){
            if(i.toLowerCase().indexOf(loginRegex[0]) + i.toLowerCase().indexOf(loginRegex[1]) > -2){
                return true;
            }
        }
        return false;
    }
}
