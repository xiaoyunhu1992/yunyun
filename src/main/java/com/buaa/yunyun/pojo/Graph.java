package com.buaa.yunyun.pojo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Graph {
	Map<String, ArrayList<MapEntry>> Graphmap = new HashMap<String, ArrayList<MapEntry>>();
	public Map<String, ArrayList<MapEntry>> getGraphmap() {
        return Graphmap;
    }
    public void getGraphmap(Map<String, ArrayList<MapEntry>> graph) {
        this.Graphmap = graph;
    }
}
