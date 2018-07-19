package com.still.Graph;

import com.still.Utils.LogUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

public class UIGraph {

    String ApkPath;
    String name;
    Map<String,ViewNode> nodes;
    int nodeCounter;

    public UIGraph(String packageName, String apkPath){
        ApkPath = apkPath;
        name = packageName.replace(".", "_");
        nodes = new HashMap<>();
        nodeCounter = 0;

    }

    public int getNewId(){
        nodeCounter +=1;
        return nodeCounter;
    }

    public ViewNode getNode(String view){
        if(nodes.containsKey(view)){
            return nodes.get(view);
        }else {
            nodes.put(view, new ViewNode(this, view));
            return nodes.get(view);
        }
    }

    public void addNode(String view){
        nodes.put(view, new ViewNode(this, view));
    }

    public void update(String view, ViewNode node){
        nodes.put(view, node);
    }

    public Document toXml(){
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("UI")
                .addAttribute("path", ApkPath);
        for(ViewNode node : nodes.values()){
            if(node.View() != null){
                Element nodeElement = root.addElement("Node")
                        .addAttribute("id", name)
                        .addAttribute("elementCount", String.valueOf(node.elementsVisited.size()))
                        .addAttribute("depth", String.valueOf(node.depth));
                Element viewsElement = nodeElement.addElement("Views");
                for(String view : node.aliasView){
                    viewsElement.addElement("View")
                            .addText(view);
                }
                Element edgesElement = nodeElement.addElement("Edges");
                for(ActionEdge edge : node.edges){
                    if(!edge.Element.willJumpOutOfApp){
                        edgesElement.addElement("edge")
                                .addElement("To")
                                .addText(node.parent.getNode(edge.destView.View()).name)
                                .addElement("Click")
                                .addText(edge.Element.toString());
                    }
                }
            }
        }
        return document;
    }

    public void saveXmlAndDotFile(String path){
        OutputFormat format = OutputFormat.createPrettyPrint();
        try {
            FileOutputStream xml_out = new FileOutputStream(path);
            XMLWriter xml_writer = new XMLWriter(xml_out, format);
            Document document = this.toXml();
            xml_writer.write(document);

            String dotFilePath = LogUtils.dotFilePath();
            FileOutputStream dot_out = new FileOutputStream(dotFilePath);
            String dot = GraphUtil.xmlToDot(document, name);
            OutputStreamWriter dot_writer = new OutputStreamWriter(dot_out, "UTF-8");
            dot_writer.write(dot);

            xml_writer.close();
            dot_writer.close();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
