package com.still.Graph;

import com.still.Automator;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.*;

public class ViewNode {

    Logger log;
    UIGraph parent;
    String View;

    public int id;

    public String name;

    public boolean shouldWait;

    Set<String> aliasView;
    HashSet<ActionEdge> edges;

    public Boolean visited;

    public int depth;

    public HashMap<UIElement,Boolean> elementsVisited;

    public ViewNode(UIGraph graph, String view){
        log = Logger.getLogger(Automator.class);
        parent = graph;
        View = view;
        id = parent.getNewId();
        name = "node" + String.valueOf(id);
        shouldWait = false;
        aliasView = new HashSet<>();
        aliasView.add(view);
        edges = new HashSet<>();
        visited = false;
        depth = -1;
        elementsVisited = new HashMap<>();

    }

    public String View(){
        return View;
    }

    public Boolean visitComplete(){
        for(Boolean i : elementsVisited.values()){
            if(!i){
                return false;
            }
        }
        return true;
    }

    public List<UIElement> elements(){
        return new ArrayList<>(elementsVisited.keySet());
    }

    public void addElement(UIElement uiElement){
        elementsVisited.put(uiElement, false);
        uiElement.parentNode = this;
    }

    public void mergeNode(ViewNode node){
        log.info("Merge node" + String.valueOf(node.id) + " and node" + String.valueOf(id));

        if (node.depth != -1 && node.depth < this.depth){
            this.depth = node.depth;
        }
        if ( node.id < this.id) {
            this.id = node.id;
        }
        for(String v : node.aliasView){
            this.aliasView.add(v);
            parent.update(v, this);
        }

    }

    public void addAlias(String view){
        mergeNode(parent.getNode(view));
    }

    public boolean hasAlias(String view){
        return aliasView.contains(view);
    }

    public void removeElement(UIElement uiElement){
        elementsVisited.remove(uiElement);
    }

    public void addAllElement(List<UIElement> elements){
        elements.forEach(this::addElement);
    }

    public void addEdge(UIElement element){
        edges.add(new ActionEdge(parent, element));
    }

    public Document toXml(){
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("Node")
                .addAttribute("id", name)
                .addAttribute("elementCount", String.valueOf(elementsVisited.size()))
                .addAttribute("depth", String.valueOf(depth));
        Element viewsElement = root.addElement("Views");
        for(String view : this.aliasView){
            viewsElement.addElement("View")
                .addText(view);
        }
        Element edgesElement = root.addElement("Edges");
        for(ActionEdge edge : edges){
            if(!edge.Element.willJumpOutOfApp){
                edgesElement.addElement("edge")
                        .addElement("To")
                                .addText(parent.getNode(edge.destView.View()).name)
                        .addElement("Click")
                                .addText(edge.Element.toString());
            }
        }
        return document;
    }
}
