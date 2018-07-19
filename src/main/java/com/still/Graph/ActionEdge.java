package com.still.Graph;

public class ActionEdge {
    ActionType action;
    UIGraph parent;
    UIElement Element;
    ViewNode destView;

    public ActionEdge(UIGraph graph, UIElement uiElement){
        this.action = ActionType.Click;
        this.parent = graph;
        this.Element = uiElement;
        this.destView = graph.getNode(uiElement.destView);

    }
}
