package com.still.Utils;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.tree.DefaultElement;

import java.util.List;

public class HierarchyUtil {
    public static Element getStructure(Element hierarchy){
        Element element = new DefaultElement("node");
        List<Node> nodes = hierarchy.selectNodes("@class");
        for(Node i : nodes){
            element.add(getSubStructure(i));
        }
        return element;
    }

    public static Element getSubStructure(Node elem){
        Element element = new DefaultElement("node")
                .addAttribute("class", elem.valueOf("class"))
                .addAttribute("resource-id", elem.valueOf("resource-id"));
        List<Node> nodes = elem.selectNodes("@class");
        for(Node i : nodes){
            element.add(getSubStructure(i));
        }

        return element;
    }

    public static String uiStructureHashDigest(String hierachy){
        Document doc = null;
        try {
            doc = DocumentHelper.parseText(hierachy);
        }catch (Exception e){
            e.printStackTrace();
        }
        return MessageDigest.Md5(doc.asXML());
    }
}
