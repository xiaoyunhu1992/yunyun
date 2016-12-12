package com.buaa.yunyun.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.buaa.yunyun.Variable;
import com.buaa.yunyun.pojo.MapEntry;
import com.buaa.yunyun.pojo.PageRankNode;
import com.buaa.yunyun.service.CommunityService;

//import org.gephi.graph.api.*;


@Controller
@RequestMapping("/Community")
public class CommunityController {
	//初始化路径
	private static String JarPath = CommunityController.class.getProtectionDomain().getCodeSource().getLocation().getPath();	
	private static String CurrentPath = JarPath.substring(0,JarPath.lastIndexOf("/"));
	private static String SrcPath = CurrentPath + "/../../../../../";
	
	@Autowired
    @Qualifier("CommunityService")
    private CommunityService commService;
	
	@RequestMapping(value ="/getConnection",method = RequestMethod.POST)
	@ResponseBody
	public Map getConnection(HttpServletRequest req, HttpServletResponse resp){
		//种子集
		String seeds=req.getParameter("seedset").trim();
		String[] seedset=seeds.split(",");
		List<String> seedsetlist=new ArrayList<String>();
		for(int i=0;i<seedset.length;i++)
			seedsetlist.add(seedset[i]);
		//获取整个图
		Map<String, ArrayList<MapEntry>> WholeGraphmap = new HashMap<String, ArrayList<MapEntry>>();
		WholeGraphmap=this.commService.getWholeGraph();
		//获取所有节点
		List<String> allnode=new ArrayList<String>();
		allnode=this.commService.getAllNode();
		//设置全局变量
		Variable.setWholeMap(WholeGraphmap);//整个图结构
		Variable.setAllnode(allnode);//所有节点
		//连通性判断
		List<List<String>> listSeedsets=new ArrayList<List<String>>();///连通种子集
		listSeedsets=findConnectedSeed(WholeGraphmap,allnode,seedsetlist);
		List<List<String>> listConnections=new ArrayList<List<String>>();//连通节点
		listConnections=findConnectedSubgraph(WholeGraphmap,allnode,seedsetlist,listSeedsets);
		List<Map<String, ArrayList<MapEntry>>> listSubMap=new ArrayList<Map<String, ArrayList<MapEntry>>>();//连通图结构
		listSubMap=findSubGraphs(listConnections);
		////////////
		for(int i=0;i<listSeedsets.size();i++)
		{
			for(int j=0;j<listSeedsets.get(i).size();j++)
			    System.out.print(listSeedsets.get(i).get(j)+",");
			System.out.println();
			for(int j=0;j<listConnections.get(i).size();j++)
			    System.out.print(listConnections.get(i).get(j)+",");
			System.out.println();
		}
		Map resultMap=new HashMap();
		resultMap.put("seedsets", listSeedsets);
		resultMap.put("Connections", listConnections);
		//设置全局变量
		Variable.setSeedsets(listSeedsets); //当前连通的种子集
        Variable.setConnections(listConnections);//连通种子集对应的连通图
        Variable.setSubGraphMaps(listSubMap);
		return resultMap;
	}
	@RequestMapping(value ="/getCommunity",method = RequestMethod.POST)
	@ResponseBody
	public Map getCommunity(HttpServletRequest req, HttpServletResponse resp) throws ClassNotFoundException, SQLException, IOException{
		Map resultMap=new HashMap();
		//当前研究的种子集，并转化为list
		String connSeed =req.getParameter("conntction").trim();
		List<String> SubSeed=new ArrayList<String>();
		String[] strs=connSeed.split(",");
		for(int i=0;i<strs.length;i++)
			SubSeed.add(strs[i]);
		//分别找到对应的连通图的节点和图结构
		List<List<String>> seedsets=new ArrayList<List<String>>();
		List<List<String>> connections=new ArrayList<List<String>>();
		List<Map<String, ArrayList<MapEntry>>> listSubMap=new ArrayList<Map<String, ArrayList<MapEntry>>>();
		seedsets=Variable.getSeedsets();
		connections=Variable.getConnections();
		listSubMap=Variable.getSubGraphMaps();
		int index=findConnforSeed(connSeed,seedsets);
		List<String> SubNodes=new ArrayList<String>();
		//System.out.println(index);
		SubNodes=connections.get(index);//所研究种子集对应的连通图节点
		Map<String, ArrayList<MapEntry>> SubGraph=new HashMap<String, ArrayList<MapEntry>>();
		SubGraph=listSubMap.get(index);//所研究种子集对应的连通图结构
		//应用改进的pagerank算法得到各个节点的pagerank值
		List<PageRankNode> subrankedList = null;
		subrankedList=rank3(Variable.iterations, Variable.DumpingFactor,SubGraph,SubNodes,SubSeed);
		//开始计算导率
		List<String> conList=new ArrayList<String>();
		List<List<String>> community=new ArrayList<List<String>>();
		double[] edges=new double[2];
		//int count=0;
		for(int i=0;i<subrankedList.size();i++)
		{
			SubSeed.add(subrankedList.get(i).getIdentifier());//将节点加入到了种子节点中
			edges=getEdges(SubSeed,SubGraph);
			double conductance;
			conductance=edges[0]/edges[1];
			
			if(isConnected(SubSeed,Variable.getAllnode()))
			{
				System.out.println("导率为："+String.valueOf(conductance));
				conList.add(String.valueOf(conductance));
				community.add(SubSeed);
			}			
		}
		
		////// 寻找最优社区
		double minCons = 1.0;
		List<String> bestComm = new ArrayList<String>();
		int count = 0;
		int size = 100;
		for(int i=0;i<conList.size();i++) {
			// 选取导率最小的社区//////////////////
			if (Double.parseDouble(conList.get(i)) <=minCons) {
				minCons = Double.parseDouble(conList.get(i));
				bestComm = community.get(i);
			}
			// 选取固定大小的社区//////////////////有问题
			/*
			 * if(linecomm.split(",").length==100) { bestComm=linecomm; break; }
			 */
		}
		String comm="";
		int i=0;
		for(i=0;i<bestComm.size()-1;i++)
			comm=comm+bestComm.get(i)+",";
		comm=comm+bestComm.get(i);
		resultMap.put("conductance", minCons);
		resultMap.put("community", bestComm);
		System.out.println("最小的导率为：" + minCons);
		System.out.println("最优的社区为：" + comm);
		//返回社区结果
		resultMap.put("type", "force");//力的导向图
		List<Map> commnode=new ArrayList<Map>();
		for(i=0;i<bestComm.size();i++)
		{
			Map mapt=new HashMap<>();
			mapt.put("name", bestComm.get(i));
			
			double weight=0;
			for(MapEntry entry:SubGraph.get(bestComm.get(i)))
			{
				weight=weight+entry.getWeight();
			}
			mapt.put("value", weight);
			mapt.put("size", weight);
			commnode.add(mapt);
		}
		resultMap.put("nodes", commnode);//添加节点
		List<Map> commlink=new ArrayList<Map>();
		for(String key:SubGraph.keySet())
		{
			if(isSeed(bestComm,key))
			{
				for(MapEntry entry:SubGraph.get(key))
				{
					if(isSeed(bestComm,entry.getIdentifier()))
					{
						Map mapt=new HashMap<>();
						mapt.put("source", key);
						mapt.put("target", entry.getIdentifier());
						mapt.put("weight", entry.getWeight());
						commlink.add(mapt);
					}
				}
			}
		}
		resultMap.put("links", commlink);
		ObjectMapper mapper=new ObjectMapper();
		mapper.writeValueAsString(resultMap);
		
		return resultMap;
	}
	/*public void DrawGraph(Map<String, ArrayList<MapEntry>> SubGraph,List<String> nodes)
	{
		//Init a project - and therefore a workspace
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        Workspace workspace = pc.getCurrentWorkspace();
        //Get a graph model - it exists because we have a workspace
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        for(String key:SubGraph.keySet())
        {
        	if(isSeed(nodes,key))
        	{
        		Node n1 = graphModel.factory().newNode(key);
                n1.getNodeData().setLabel(key);
                undirectedGraph.addNode(n1);
        		for(MapEntry entry:SubGraph.get(key))
        		{
        			if(isSeed(nodes,entry.getIdentifier()))
        			{
        				Node n2 = graphModel.factory().newNode(entry.getIdentifier());
                        n2.getNodeData().setLabel(entry.getIdentifier());
                        Edge e=graphModel.factory().newEdge(n1, n2, 1f, true);                     
                        undirectedGraph.addNode(n2);
                        undirectedGraph.addEdge(e);
        			}
        		}
        	}
        }
        System.out.println("Edges: "+undirectedGraph.getEdgeCount());
      //生成gexf文件
        ExportController ec = Lookup.getDefault().lookup(ExportController.class);
        try {
        	ec.exportFile(new File(SrcPath+"io_graph.gexf"));
        } catch (IOException ex1) {
        	ex1.printStackTrace();
        	return;
        }
        GraphExporter exporter = (GraphExporter) ec.getExporter("gexf"); 
        exporter.setExportVisible(true);  
        exporter.setWorkspace(workspace);
    	try {
    		ec.exportFile(new File(SrcPath+"io_graph.gexf"), exporter);
    	} catch (IOException ex1) {
    		ex1.printStackTrace();
    		return;
    	}
	}*/
	//寻找种子集的连通分量
	public List<List<String>> findConnectedSeed(Map<String, ArrayList<MapEntry>> map,List<String> allnode,List<String> seed)
	{
		int count=0;//连通图下标
		List<List<String>> resultlist=new ArrayList<List<String>>();
		//将所有节点放入list中
		int n=allnode.size();
		//设定遍历标志数组并初始化为0
		int[] visited=new int[n];
		for(int i=0;i<n;i++)
		{
			visited[i]=0;
		}
		//非递归的深度优先搜索
		Stack stack=new Stack();
		for(int i=0;i<seed.size();i++)
		{
			int index=findIndex(seed.get(i),allnode);
			if(visited[index]==0)
			{
				visited[index]=1;//设为已访问
				if(count>=resultlist.size())
				{
					List<String> list=new ArrayList<String>();
					list.add(seed.get(i));
					resultlist.add(list);
				}
				else
				{
					resultlist.get(count).add(seed.get(i));
				}
				count++;
				stack.push(seed.get(i));//起始节点进栈
				while(!stack.isEmpty())
				{
					String node=stack.peek().toString();
					//设置标志位看node是否存在未被访问的邻居节点
					boolean flag=false;
					//System.out.println(node);
					for(MapEntry entry : map.get(node))
					{
						if(visited[findIndex(entry.getIdentifier(),allnode)]==0)
						{
							//如果是种子节点则写入
							if(isSeed(seed,entry.getIdentifier()))
							{
								resultlist.get(count-1).add(entry.getIdentifier());
								//writeSeeds.println(entry.getIdentifier());//写入文件
							}
							visited[findIndex(entry.getIdentifier(),allnode)]=1;//设为已访问
							stack.push(entry.getIdentifier());
							flag=true;
						}
					}
					if(flag==false)
						stack.pop();
				}
			}
		}
		return resultlist;
	}
	//寻找每个种子集的最大连通子图
	public List<List<String>> findConnectedSubgraph(Map<String, ArrayList<MapEntry>> map,List<String> allnode,List<String> seed,List<List<String>> listSeedsets)
	{
		List<List<String>> resultlist=new ArrayList<List<String>>();
		//针对每个种子节点做一下操作
		for(int count=0;count<listSeedsets.size();count++)
		{
			//设定遍历标志数组并初始化为0
			int[] visited=new int[allnode.size()];
			for(int i=0;i<allnode.size();i++)
			{
				visited[i]=0;
			}
			//读取每个种子节点存入list
			List<String> seedlist=new ArrayList<String>();
			seedlist=listSeedsets.get(count);
			//每个连通图
			List<String> resultIn=new ArrayList<String>();
			//非递归方式寻找种子集的最大连通子图
			Stack stack=new Stack();
			//获得种子节点与其他所有节点的最短路径
			List<List<Integer>> shortPaths=new ArrayList<List<Integer>>();
			for(int i=0;i<seedlist.size();i++)
			{
				List<Integer> shortPath=new ArrayList<Integer>();
				shortPath=Dijkstra(seedlist.get(i),map,allnode);
				shortPaths.add(shortPath);
			}
			for(int i=0;i<seedlist.size();i++)
			{
				//获得该节点与图中所有其他节点的最短路径Dijkstra
				//int tiaoshu=0;//记录与种子集的跳数
				int index=findIndex(seedlist.get(i),allnode);//huodexiaobiao
				if(visited[index]==0)
				{
					visited[index]=1;//设为已访问
					resultIn.add(seedlist.get(i));
					stack.push(seedlist.get(i));//起始节点进栈
					while(!stack.isEmpty())
					{
						String node=stack.peek().toString();
						//设置标志位看node是否存在未被访问的邻居节点
						boolean flag=false;
						//tiaoshu++;
						for(MapEntry entry : map.get(node))
						{
							if(visited[findIndex(entry.getIdentifier(),allnode)]==0)
							{
								//判断最短路径是否小于3
								int flagPath=0;
								for(int k=0;k<seedlist.size();k++)
								{
									//System.out.println(shortPaths.get(k).get(findIndex(entry.getIdentifier())));
									//if(shortPaths.get(k).get(findIndex(entry.getIdentifier()))<=3)
									//{
									    flagPath=1;
									    break;
									//}
								}
								if(flagPath==1)
								{
									resultIn.add(entry.getIdentifier());
								}
								//if(!isSeed(seedlist,entry.getIdentifier()))
								visited[findIndex(entry.getIdentifier(),allnode)]=1;//设为已访问
								stack.push(entry.getIdentifier());
								flag=true;
							}
						}
						if(flag==false)
							stack.pop();
					}
				}
			}
			resultlist.add(resultIn);
		}
		return resultlist;
	}
	//寻找下标的函数
	public int findIndex(String node,List<String> allnode)
	{
		for(int i=0;i<allnode.size();i++)
		{
			if(allnode.get(i).equals(node))
				return i;
		}
		return -1;
	}
	//判断是否为种子
	public boolean isSeed(List<String> list,String node)
	{
		boolean flag=false;
		for(int i=0;i<list.size();i++)
		{
			if(list.get(i).equals(node))
				flag=true;
		}
		return flag;
	}
	//Dijkstra求图中所有节点到某一节点的所有最短路径
	public List<Integer> Dijkstra(String node,Map<String, ArrayList<MapEntry>> map,List<String> allnode)
	{
		List<Integer> dist=new ArrayList<Integer>();//存放最短路径
		List<Integer> s=new ArrayList<Integer>();//判断是否已存入该点到集合S
		int MAX=Integer.MAX_VALUE;
		int n=allnode.size();
		int index=findIndex(node,allnode);
		for(int i=0;i<n;i++)
		{
			dist.add(0);
			s.add(0);
		}
		int p=0;
		for(int k=0;k<n;k++)
		{
			//判断节点node到下标为k的节点是否有路径的路径
			int flag=0;
			for(MapEntry entry : map.get(node))
			{
			    if(entry.getIdentifier().equals(allnode.get(k)))
			    {
			    	flag=1;
			    	break;
			    }
			}
			if(flag==1)
			    dist.set(k, 1);
			else if(flag==0 && index!=k)
			    dist.set(k, MAX);
			else
			    dist.set(k, 0);
			s.set(k, -1);
		}
		s.set(index, 0);
		dist.set(index, 0);
		for(int m=0;m<n-1;m++)
		{
			int u=min(dist,s);
			p++;
			s.set(u, p);
			for(int i=0;i<n;i++)
			{
				if(s.get(i)==-1)
				{
					 //判断下表为u是否有到下标为i的路径
					int t;
					int flag=0;
					for(MapEntry entry : map.get(allnode.get(u)))
					{
					    if(entry.getIdentifier().equals(allnode.get(i)))
					    {
							flag = 1;
							break;
						}
					}
					if (flag == 1)
						t = 1;
					else if (flag == 0 && u != i)
						t = MAX;
					else
						t = 0;
					if (dist.get(u) != MAX && t != MAX) {
						if (dist.get(u) + t < dist.get(i))
							dist.set(i, dist.get(u) + t);
					}
				}
			}
		}
		return dist;
	}

	public int min(List<Integer> dist, List<Integer> s) {
		int min = 0;
		for (int i = 0; i < dist.size(); i++) {
			while (s.get(min) != -1) {
				min++;
			}
			if (s.get(i) == -1) {
				if (dist.get(i) < dist.get(min))
					min = i;
			}
		}
		return min;
	}
	
	public List<Map<String, ArrayList<MapEntry>>> findSubGraphs(List<List<String>> listConnections){
		List<Map<String, ArrayList<MapEntry>>> listresult=new ArrayList<Map<String, ArrayList<MapEntry>>>();
		Map<String, ArrayList<MapEntry>> Wholemap = new HashMap<String, ArrayList<MapEntry>>();
		Wholemap=Variable.getWholeMap();
		for(int i=0;i<listConnections.size();i++)
		{
			List<String> nodes=listConnections.get(i);
			Map<String, ArrayList<MapEntry>> Submap = new HashMap<String, ArrayList<MapEntry>>();
			for(String key:Wholemap.keySet())
			{
				if(isSeed(nodes,key))
				{
					for(MapEntry entry : Wholemap.get(key))
					{
						if(isSeed(nodes,entry.getIdentifier()))
						{
							//写入submap
							if(Submap.containsKey(key))
							{
								if(! Submap.get(key).contains(entry))
								{
									Submap.get(key).add(entry);
								}	
							}
							else
							{
								ArrayList<MapEntry> list = new ArrayList<MapEntry>();
								list.add(entry);
								Submap.put(key, list);
							}	
						}				
					}
				}
			}	
			listresult.add(Submap);
		}
		return listresult;
	}
	
	
	//寻找当前研究的种子集所对应的连通图
	public int findConnforSeed(String seed,List<List<String>> seedsets)
	{
		for(int i=0;i<seedsets.size();i++)
		{
			String seedsearch="";
			for(int j=0;j<seedsets.get(i).size();j++)
				seedsearch=seedsearch+seedsets.get(i).get(j)+",";
			if(seedsearch.equals(seed+","))
				return i;
		}
		return -1;
	}

	// 考虑相似性的pagerank迭代／
	/*
	 * iterations：迭代次数 dampingFactor：阿尔法参数 mapSub：连通子图图结构 listnode：所有节点
	 * list：种子节点
	 */
	public  List<PageRankNode> rank3(int iterations, double dampingFactor,Map<String, ArrayList<MapEntry>> mapSub, List<String> listnode, List<String> list) throws ClassNotFoundException, SQLException, IOException{
		HashMap<String, Double> lastRanking = new HashMap<String, Double>();
		HashMap<String, Double> nextRanking = new HashMap<String, Double>();
		// List<String> listnode=new ArrayList<String>();
		// 初始化lastranking为0
		for (String key : mapSub.keySet()) {
			lastRanking.put(key, 0.0);
			for (MapEntry entry : mapSub.get(key)) {
				if (!mapSub.containsKey(entry.getIdentifier()))
					lastRanking.put(entry.getIdentifier(), 0.0);
			}
		}
		// 初始化lastranking为节点分之一
		Double startRank = 1.0 / lastRanking.size();
		for (String key : mapSub.keySet()) {
			lastRanking.put(key, startRank);
			for (MapEntry entry : mapSub.get(key)) {
				if (!mapSub.containsKey(entry.getIdentifier()))
					lastRanking.put(entry.getIdentifier(), startRank);
			}
		}
		double dampingFactorComplement = 1.0 - dampingFactor;
		// 获取节点相似性
		HashMap<String, Double> similarity = new HashMap<String, Double>();
		double sum = 0;
		// 单线程实现相似性向量/////////////////////整个算一个节点和种子节点的相似性
		/*
		 * for(String key : lastRanking.keySet()) { double s=getS(key,list);
		 * sum=sum+s; similarity.put(key, s);
		 * //System.out.println(similarity.get(key)); }
		 */
		// 单线程实现相似性向量/////////////////////分别算各个特征的相似性向量，归一化后再相加，再归一化（归一化在后边）
		getSimilarity(list, lastRanking, similarity);// 获取相似性向量，返回值为该向量所有元素的和（种子集，各个节点）；
		// pagerank迭代10次
		for (int times = 0; times < iterations; times++) {
			// 初始化nextranking为0
			for (String key : mapSub.keySet()) {
				nextRanking.put(key, 0.0);
				for (MapEntry entry : mapSub.get(key)) {
					if (!mapSub.containsKey(entry.getIdentifier()))
						nextRanking.put(entry.getIdentifier(), 0.0);
				}
			}
			// pagerank
			for (String key : mapSub.keySet()) {
				for (MapEntry entry : mapSub.get(key)) {
					double t;
					t = nextRanking.get(entry.getIdentifier()) + lastRanking.get(key) / mapSub.get(key).size();
					nextRanking.put(entry.getIdentifier(), t);
				}
			}
			//
			for (String key : nextRanking.keySet()) {
				nextRanking.put(key,
						dampingFactor * nextRanking.get(key) + dampingFactorComplement * similarity.get(key));
			}
			// 将nextranking值赋给lastranking
			for (String identifier : nextRanking.keySet()) {
				lastRanking.put(identifier, nextRanking.get(identifier));
			}
		}
		System.out.println(iterations + " pagerank迭代结束...");
		return PageRankVector(lastRanking);
	}

	/*
	 * PageRankֵ值排序
	 */
	public List<PageRankNode> PageRankVector(final HashMap<String, Double> LastRanking) {
		List<PageRankNode> nodeList = new LinkedList<PageRankNode>();
		for (String identifier : LastRanking.keySet()) {
			PageRankNode node = new PageRankNode(identifier, LastRanking.get(identifier));
			nodeList.add(node);
		}
		Collections.sort(nodeList);
		System.out.println("各个节点pagerank值为：");
		for (int i=0;i<nodeList.size();i++)
		{
		    System.out.println(nodeList.get(i).getIdentifier()+","+nodeList.get(i).getRank());
		}
		return nodeList;
	}
	/*
     * seeds:种子集
     * lastRanking: 存放各个节点
     * similarity:存放相似性，最后需要改变
     * 返回值为最终向量的和
     */
    public void getSimilarity(List<String> seeds,HashMap<String, Double> lastRanking,HashMap<String, Double> similarity) throws ClassNotFoundException, SQLException, IOException
    {
//    	FileWriter writer=null;
//  		File f=new File(SrcPath+"similarity.txt");
//  		if(!f.exists())
//  			f.createNewFile();
//  		PrintWriter out =null;
    	//ComputeSimilarity com=new ComputeSimilarity();
    	//先计算交互相似性 并对其归一化
    	HashMap<String, Double> interact = new HashMap<String, Double>();
    	for(String key : lastRanking.keySet())
    	{
    		Double countInter=getInterCount(key,seeds);//该节点跟种子集的交互总次数
    		interact.put(key, countInter);//写入交互记录的map中
    		System.out.println("节点"+key+"和种子集的交互向量："+countInter);
    	}
    	Normalization(interact);//归一化
    	//计算话题相似性
    	HashMap<String, Double> topic = new HashMap<String, Double>();
    	for(String key : lastRanking.keySet())
    	{
    		Double countTopic=getTopicCount(key,seeds);//该节点跟种子集的交互总次数
    		topic.put(key, countTopic);//写入交互记录的map中
    		System.out.println("节点"+key+"和种子集的话题向量："+countTopic);
    	}
    	Normalization(topic);//归一化
    	//是否抄送过同一篇邮件
    	HashMap<String, Double> sameEmail = new HashMap<String, Double>();
    	for(String key : lastRanking.keySet())
    	{
    		Double countSameEmail=getSameEmailCount(key,seeds);//该节点跟种子集的交互总次数
    		sameEmail.put(key, countSameEmail);//写入交互记录的map中
    		System.out.println("节点"+key+"和种子集的抄送同一篇邮件："+countSameEmail);
    	}
    	Normalization(sameEmail);//归一化
    	//是否相邻并联系紧密
    	HashMap<String, Double> IsCloser = new HashMap<String, Double>();
    	for(String key : lastRanking.keySet())
    	{
    		Double countIsCloser=getIsCloserCount(key,seeds);//该节点跟种子集的交互总次数
    		IsCloser.put(key, countIsCloser);//写入交互记录的map中
    		System.out.println("节点"+key+"和种子集的是否相邻并联系紧密："+countIsCloser);
    	}
    	Normalization(IsCloser);//归一化
    	//对所有特征进行合并并归一化
    	for(String key : lastRanking.keySet())
    	{
    		Double s=Variable.wOfInteract*interact.get(key)+Variable.wOfTopic*topic.get(key)+Variable.wOfSameEamil*sameEmail.get(key)+Variable.wOfIsCloser*IsCloser.get(key);
    		System.out.println("节点"+key+"和种子集的相似性为："+s);
//    		try{
//      			writer=new FileWriter(f,true);
//      			out=new PrintWriter(writer);
//      			out.println(key+"和种子集的相似性为："+s);
//        	}catch(IOException e)
//      		{
//      			e.printStackTrace();
//      		}
//      		writer.close();
//      		out.close();
    		similarity.put(key, s);
    	}
    	Normalization(similarity);//归一化
    	//将种子节点的元素值设高////////////////////////
    	for(String key : lastRanking.keySet())
    	{
    		if(isSeed(seeds,key))
    			similarity.put(key, 1.0);
    	}
    	//return sum;
    }
  //归一化Double
    public void Normalization(HashMap<String, Double> hs)
    {
    	Double sum=0.0;
    	//对向量进行归一化处理
    	for(String key : hs.keySet())
    		sum=sum+hs.get(key);
    	for(String key : hs.keySet())
    	{
    		if(sum==0)
    			hs.put(key, 0.0);
    		else
    			hs.put(key, hs.get(key)/sum);
    	}
    }

    ///////////*********计算相似性************/////////////
  	//返回节点和种子节点之间总的交互次数
  	public Double getInterCount(String key,List<String> seeds) throws SQLException, ClassNotFoundException
  	{
  		Double result=0.0;
  		for(int i=0;i<seeds.size();i++)
  		{
  			result=result+this.commService.getInterFeature(key, seeds.get(i));
  		}
  		return result;		
  	}
  	//返回节点和种子节点之间的话题相似性
  	public Double getTopicCount (String key,List<String> seeds) throws SQLException, ClassNotFoundException, IOException
  	{
  		Double result=0.0;
  		for(int i=0;i<seeds.size();i++)
  		{
  			result=result+getTopic(key,seeds.get(i));
  		}
  		return result;		
  	}
  	//返回节点和种子节点之间是否抄送过同一篇邮件
  	public Double getSameEmailCount (String key,List<String> seeds) throws SQLException, ClassNotFoundException, IOException
  	{
  		Double result=0.0;
  		for(int i=0;i<seeds.size();i++)
  		{
  			result=result+getsameEmail(key,seeds.get(i));
  		}
  		return result;		
  	}
  	//返回节点和种子节点之间是否相邻并且联系紧密
  	public Double getIsCloserCount (String key,List<String> seeds) throws SQLException, ClassNotFoundException, IOException
  	{
  		Double result=0.0;
  		for(int i=0;i<seeds.size();i++)
  		{
  			result=result+getCloser(key,seeds.get(i));
  		}
  		return result;		
  	}
  	
  //话题特征
  	private Double getTopic(String node1, String node2) throws SQLException, ClassNotFoundException, IOException {
  		double result=0.0;
  		/////////自己分析话题
  		//去停用词直接计算相似性,去掉the a an等
  		// 先获取停用的英文单词
  		List<String> stopword=new ArrayList<String>();
  		String t=null;
  		File f=new File(Variable.path+"stopword.txt");
  		BufferedReader br=new BufferedReader(new FileReader(f));
  		while((t=br.readLine())!=null)
  		{
  			t=t.trim();
  			stopword.add(t);
  		}
  		br.close();
  		//开始查找消息
  		List<String> list1=new ArrayList<String>();
  		list1=this.commService.getTopicFeature(node1);//////////这个可能需要去除掉空的
  		List<String> list2=new ArrayList<String>();
  		list2=this.commService.getTopicFeature(node2);
  		int count=0;
  		for(int i=0;i<list1.size();i++)
  		{
  			if(list1.get(i)!=null&&list1.get(i)!="")
  			{
  				for(int j=0;j<list2.size();j++)
  				{
  					if(list2.get(j)!=null&list2.get(j)!="")
  					{
  						count++;
  						String s1=DeleteStopWordEnglish(stopword,list1.get(i));
  						String s2=DeleteStopWordEnglish(stopword,list2.get(j));
  						result=result+similarByCosEnglish(s1,s2);
  					}
  				}
  			}
  		}
//  		ResultSet resultset1=connection.selectSubject(node1);
//  		while(resultset1.next())
//  		{
//  			String s=resultset1.getString(1);
//  			if(s!=null&&s!="")
//  				list1.add(s);
//  		}
//  		resultset1.close();
//  		int count=0;
//  		ResultSet resultset2=connection.selectSubject(node2);
//  		while(resultset2.next())
//  		{
//  			String s=resultset2.getString(1);
//  			if(s!=null&&s!="")
//  			{
//  				String s1=DeleteStopWordEnglish(stopword,s);
//  				count++;
//  				for(int i=0;i<list1.size();i++)
//  				{			
//  					String s2=DeleteStopWordEnglish(stopword,list1.get(i));
//  					result=result+similarByCosEnglish(s1,s2);
//  				}
//  			}
//  		}
//  		resultset2.close();
  		if(list1.size()==0||count==0)
  			return 0.0;
  		result=result/(list1.size()*count);
  		return result;
  	}
  //去停用词函数,去英文
  	public String DeleteStopWordEnglish(List<String> stopword,String str)
  	{
  		String result="";
  		// 先要处理英文
  		//System.out.println("原邮件为："+str);
  		str=str.replaceAll("[\\p{Punct}\\pP]", " ");//去除标点
  		//System.out.println("去掉标点之后为："+str);
  		String[] strs=str.split(" ");
  		for(int i=0;i<strs.length;i++)
  		{
  			String s=strs[i].toLowerCase();//全部变为小写		
  			if(!isSeed(stopword,s))
  			{
  				result=result+s+" ";
  			}
  		}
  		//for(int i=0;i<stopword.size();i++)
  			//str=str.replaceAll(stopword.get(i), "");
  		//System.out.println("去停用此之后为："+result);
  		return result;
  	}

	//英文文本的相似性cos值
	public double similarByCosEnglish(String str1,String str2)
	{
		Map<String,int[]> vectorSpace=new HashMap<String,int[]>();
		int[] itemCountArray=null;
		String strArray[]=str1.split(" ");
		for(int i=0;i<strArray.length;++i)
		{
			if(vectorSpace.containsKey(strArray[i]))
				++(vectorSpace.get(strArray[i])[0]);
			else
			{
				itemCountArray=new int[2];
				itemCountArray[0]=1;
				itemCountArray[1]=0;
				vectorSpace.put(strArray[i], itemCountArray);
			}				
		}
		strArray=str2.split(" ");
		for(int i=0;i<strArray.length;++i)
		{
			if(vectorSpace.containsKey(strArray[i]))
				++(vectorSpace.get(strArray[i])[1]);
			else
			{
				itemCountArray=new int[2];
				itemCountArray[0]=0;
				itemCountArray[1]=1;
				vectorSpace.put(strArray[i], itemCountArray);
			}				
		}
		double vector1Modulo=0.0;
		double vector2Modulo=0.0;
		double vectorProduct=0.0;
		Iterator iter=vectorSpace.entrySet().iterator();
		while(iter.hasNext())
		{
			Map.Entry entry=(Map.Entry)iter.next();
			itemCountArray=(int[])entry.getValue();
			vector1Modulo+=itemCountArray[0]*itemCountArray[0];
			vector2Modulo+=itemCountArray[1]*itemCountArray[1];
			vectorProduct+=itemCountArray[0]*itemCountArray[1];
		}
		if(vectorProduct==0)
			return 0.0;
		else
		{
			vector1Modulo=Math.sqrt(vector1Modulo);
			vector2Modulo=Math.sqrt(vector2Modulo);
			return (vectorProduct/(vector1Modulo*vector2Modulo));
		}
	}

	public Integer getsameEmail(String node1, String node2) throws ClassNotFoundException, SQLException {
		//Double result=0.0;
		//邮件id一样，并且有相同的发件人
		int count=this.commService.getEmailFeature(node1,node2);		
		//if(count!=0)
			//System.out.println(count);
		//if(count>=1)
			//result=1.0;	
		return count;
	}

	public Integer getCloser(String node1,String node2) throws ClassNotFoundException, SQLException
	{
		//Double result=0.0;
		//邮件id一样，并且有相同的发件人
		int count=0;
		count=this.commService.getCloserFeature(node1, node2);
		//if(count>0)
			//result=1.0;
		return count;
	}

	/*计算导率用的参数，两类边的数量
     */
    public  double[] getEdges(List<String> list,Map<String, ArrayList<MapEntry>> map)
    {
    	//定义Subgraph
//    	Map<String, ArrayList<MapEntry>> Subgraph=new HashMap<String, ArrayList<MapEntry>>();
//    	Subgraph=getSubGraph();
    	double[] array=new double[2];//array[0]为社区内节点指向社区外节点的边array[1]min(社区内的边，社区外的边)
    	double in=0.0;
    	double out=0.0;
    	
    	//System.out.println(inEdges+","+outEdges+","+conduct);
    	for (String key : map.keySet())
    	{
    		if(isSeed(list,key))
    		{
    			for(MapEntry entry : map.get(key))
    			{
    				if(!isSeed(list,entry.getIdentifier()))
    					array[0]=array[0]+entry.getWeight();
    				else
    					in=in+entry.getWeight();
    			}
    		}
    		else
    		{
    			for(MapEntry entry : map.get(key))
    			{
    				if(isSeed(list,entry.getIdentifier()))
    					array[0]=array[0]+entry.getWeight();	
    				else
    					out=out+entry.getWeight();
    			}
    		}		
    	}
    	if(in>out)
    		array[1]=out+array[0];
    	else
    		array[1]=in+array[0];
    	return array;
    }

  //连通行判断ͨ
    public boolean isConnected(List<String> list,List<String> allnode) throws IOException
    {
    	Map<String, ArrayList<MapEntry>> mapGraph=new HashMap<String, ArrayList<MapEntry>>();
    	mapGraph=Variable.getWholeMap();
    	int n=allnode.size();
    	//初始化每个节点状态为未访问
    	int[] visited=new int[n];
    	for(int i=0;i<n;i++)
    	{
    		visited[i]=0;
    	}
    	//初始化栈
    	Stack stack=new Stack();
    	for(int i=0;i<list.size();i++)
    	{
   			int index=findIndex(list.get(i),allnode);
    		if(visited[index]==0)
    		{
    			if(i!=0)
    				return false;
    			else
    			{
    				visited[index]=1;//设访问位为1
    				stack.push(list.get(i));//入栈
    				while(!stack.isEmpty())
    				{
    					String node=stack.peek().toString();
    					
    					boolean flag=false;
    					if(mapGraph.containsKey(node))
    					{
    						for(MapEntry entry : mapGraph.get(node))
    						{
    							if(visited[findIndex(entry.getIdentifier(),allnode)]==0)
    							{
    								visited[findIndex(entry.getIdentifier(),allnode)]=1;//访问位为1
    								stack.push(entry.getIdentifier());
    								flag=true;
    							}
    						}
    					}
    					if(flag==false)
    						stack.pop();
    				}
    			}
    		}
    	}
    	return true;
    }

  //将真实社区情况存入文件
  	//数据库中一共有14种类别，分两种情况，因为有两个人去标记数据，有的分类有一个人同意，有的分类有两个人同意，以下将一人和两人的情况存入两个文件中
  	//可以执行一次之后就不再执行
//  	private  void getRealCommunity() throws SQLException, IOException
//  	{
//  		//新建文件，realcomm1.txt和realcomm2.txt
//  		File file1=new File(SrcPath+"realcomm/realcomm1.txt");
//  		if(!file1.exists())
//  			file1.createNewFile();
//  		File file2=new File(SrcPath+"realcomm/realcomm2.txt");
//  		if(!file2.exists())
//  			file2.createNewFile();
//  		//总共的类别
//  		String[] cats={"base","cat01","cat02","cat03","cat04","cat05","cat06","cat07","cat08","cat09","cat10","cat11","cat12","cat13"};
//  		//String[] cats={"cat01","cat02","cat03","cat04","cat05","cat06","cat07","cat08","cat09","cat10","cat11","cat12","cat13"};
//  		//开始写入文件
//  		PrintWriter pw1 = null;
//  		PrintWriter pw2 = null;
//  		try {
//  			pw1 = new PrintWriter(file1);
//  			pw2 = new PrintWriter(file2);
//  			for(int i=0;i<cats.length;i++)
//  			{
//  				//一个人同意的社区
//  				List<String> list1=new ArrayList<String>();							
//  				ResultSet result1=connection.selectCats(cats[i],1);
//  				//将节点写入list
//  				while(result1.next())
//  				{
//  					String s=result1.getString(1);
//  					String r=result1.getString(2);
//  					if(!isCommunity(s,list1))
//  						list1.add(s);
//  					if(!isCommunity(r,list1))
//  						list1.add(r);
//  				}
//  				result1.close();
//  				//将list中的节点写入文件
//  				System.out.println(cats[i]+"的长度为："+list1.size());
//  				pw1.print(cats[i]+":");
//  				for(int j=0;j<list1.size();j++)
//  					pw1.print(list1.get(j)+",");
//  				pw1.println();
//  				//两个人同意的社区
//  				List<String> list2=new ArrayList<String>();			
//  				ResultSet result2=connection.selectCats(cats[i],2);
//  				while(result2.next())
//  				{
//  					String s=result2.getString(1);
//  					String r=result2.getString(2);
//  					if(!isCommunity(s,list2))
//  						list2.add(s);
//  					if(!isCommunity(r,list2))
//  						list2.add(r);
//  				}
//  				result2.close();
//  				System.out.println(cats[i]+"的长度为："+list2.size());
//  				pw2.print(cats[i]+":");
//  				for(int j=0;j<list2.size();j++)
//  					pw2.print(list2.get(j)+",");				
//  				pw2.println();
//  			}	
//  		} catch (FileNotFoundException e) {
//  			e.printStackTrace();
//  		}	
//  		pw1.close();
//  		pw2.close();
//  	}*／
}
