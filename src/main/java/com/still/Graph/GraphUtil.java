package com.still.Graph;

import com.still.Utils.LogUtils;
import org.dom4j.Document;
import org.dom4j.Node;

import java.nio.file.Paths;
import java.util.List;

public class GraphUtil {
    public static String xmlToDot(Document xml, String graphName){
        StringBuilder sb = new StringBuilder();
        sb.append("digraph " + graphName + " {\\n");
        List<Node> nodes = xml.selectNodes("//Node");
        for(Node node : nodes){
            String nodeName = node.valueOf("@id");
            sb.append(nodeName + " [shape=box, label=<\\n");
            sb.append("<table border=\"0\">");
            String imageTr = String.format("<tr><td width=\"250\" height=\"400\" fixedsize=\"true\"><img scale=\"true\" src=\"%s.png\"></img></td></tr>", Paths.get(LogUtils.caseLogDir, nodeName).toAbsolutePath().toString());
            sb.append(imageTr);
            sb.append("</table>");
            sb.append(">]\n");
            List<Node> edges = node.selectNodes("//Edge");
            for(Node edge : edges){
                sb.append(String.format("%s -> %s [label=\"%s\"]\n", nodeName, edge.selectSingleNode("To").getText(), edge.selectSingleNode("Click").getText()));
            }

        }
        sb.append("}\n");
        return sb.toString();
    }
 }
