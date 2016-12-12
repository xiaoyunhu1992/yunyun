package com.buaa.yunyun;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.buaa.yunyun.pojo.MapEntry;

//全局变量类
public class Variable {
	public static Map<String, ArrayList<MapEntry>> wholeMap=new HashMap<String, ArrayList<MapEntry>>();//整个图结构
	public static List<String> allnode=new ArrayList<String>();//所有节点
	public static List<List<String>> seedsets=new ArrayList<List<String>>();//与输入节点集对应的连通种子集
	public static List<List<String>> connections=new ArrayList<List<String>>();//与输入节点集对应的连通节点
	public static List<Map<String, ArrayList<MapEntry>>> SubGraphMap=new ArrayList<Map<String, ArrayList<MapEntry>>>();////与输入节点集对应的连通图结构
	public static String path="/Users/yunyun/Documents/workspaceee/yunyun/src/main/";//文件所在路径
	public static int realCommCat=1;//真实社区是一人同意还是两人同意
	//初始化迭代次数和阿尔法
	public static int iterations = 10;
	public static double DumpingFactor = 0.8;
	//各个特征所占比例
	public static double wOfInteract=0.5546;
	public static double wOfTopic=0.6112;
	public static double wOfSameEamil=0.1141;
	public static double wOfIsCloser=0.0763;
	public static Map<String, ArrayList<MapEntry>> getWholeMap()
	{
		return wholeMap;
	}
	public static void setWholeMap(Map<String, ArrayList<MapEntry>> map)
	{
		wholeMap=map;
	}
	public static List<String> getAllnode()
	{
		return allnode;
	}
	public static void setAllnode(List<String> node)
	{
		allnode=node;
	}
	public static List<List<String>> getSeedsets()
	{
		return seedsets;
	}
	public static void setSeedsets(List<List<String>> seednodes)
	{
		seedsets=seednodes;
	}
	public static List<List<String>> getConnections()
	{
		return connections;
	}
	public static void setConnections(List<List<String>> connectionnodes)
	{
		connections=connectionnodes;
	}
	public static List<Map<String, ArrayList<MapEntry>>> getSubGraphMaps()
	{
		return SubGraphMap;
	}
	public static void setSubGraphMaps(List<Map<String, ArrayList<MapEntry>>> maps)
	{
		SubGraphMap=maps;
	}
	public static int getIterations()
	{
		return iterations;
	}
	public static void setIterations(int iter)
	{
		iterations=iter;
	}
	public static double getDumpingFactor()
	{
		return DumpingFactor;
	}
	public static void setDumpingFactor(double df)
	{
		DumpingFactor=df;
	}
	public static double getwOfInteract()
	{
		return wOfInteract;
	}
	public static void setwOfInteract(double interact)
	{
		wOfInteract=interact;
	}
	public static double getwOfTopic()
	{
		return wOfTopic;
	}
	public static void setwOfTopic(double topic)
	{
		wOfTopic=topic;
	}
	public static double getwOfSameEamil()
	{
		return wOfSameEamil;
	}
	public static void setwOfSameEamil(double email)
	{
		wOfSameEamil=email;
	}
	public static double getwOfIsCloser()
	{
		return wOfIsCloser;
	}
	public static void setwOfIsCloser(double closer)
	{
		wOfIsCloser=closer;
	}
	public static int getRealCommCat()
	{
		return realCommCat;
	}
	public static void setRealCommCat(int x)
	{
		realCommCat=x;
	}
}
