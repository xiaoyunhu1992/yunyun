package com.buaa.yunyun.service;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.buaa.yunyun.dao.CommunityDao;
import com.buaa.yunyun.pojo.Edge;
import com.buaa.yunyun.pojo.MapEntry;

@Service("CommunityService") 
public class CommunityService {
	
	@Resource  
    private CommunityDao commDao; 
	/**获取整个图结构
	 * @param 
	 * @return 图结构map
	 */
	public Map<String, ArrayList<MapEntry>> getWholeGraph()
	{
		Map<String, ArrayList<MapEntry>> WholeGraphmap = new HashMap<String, ArrayList<MapEntry>>();
		//获取整个图
		List list=new ArrayList<>();
		list=commDao.getGraph();
		//将整个图存入WholeGraphmap//单向的
		for(int j=0;j<list.size();j++)
		{
			Edge edge=(Edge) list.get(j);
//			System.out.println(edge.getSource()+","+edge.getTarget()+","+edge.getWeight());
			String source=String.valueOf(edge.getSource());
			String target=String.valueOf(edge.getTarget());
			double weight=(double)edge.getWeight();
			
			MapEntry mapEntry1 = new MapEntry(target, weight);
			MapEntry mapEntry2 = new MapEntry(source, weight);
			if(WholeGraphmap.containsKey(source))
			{
				int flag=0;
				for(int i=0;i<WholeGraphmap.get(source).size();i++)
				{
					if(WholeGraphmap.get(source).get(i).getIdentifier().equals(target))
					{
						MapEntry mapEntrySum=new MapEntry(target,WholeGraphmap.get(source).get(i).getWeight()+weight);
						WholeGraphmap.get(source).remove(i);
						WholeGraphmap.get(source).add(mapEntrySum);
						//WholeGraphmap.get(source).set(i, mapEntrySum);
						flag=1;
						break;
					}
				}
				if(flag==0)
					WholeGraphmap.get(source).add(mapEntry1);
			}
			else if(WholeGraphmap.containsKey(target))
			{
				int flag=0;
				for(int i=0;i<WholeGraphmap.get(target).size();i++)
				{
					if(WholeGraphmap.get(target).get(i).getIdentifier().equals(source))
					{
						MapEntry mapEntrySum=new MapEntry(source,WholeGraphmap.get(target).get(i).getWeight()+weight);
						WholeGraphmap.get(target).remove(i);
						WholeGraphmap.get(target).add(mapEntrySum);
						//WholeGraphmap.get(target).set(i, mapEntrySum);
						flag=1;
						break;
					}
				}
				if(flag==0)
					WholeGraphmap.get(target).add(mapEntry2);
			}
			else
			{
				ArrayList<MapEntry> listin = new ArrayList<MapEntry>();
				listin.add(mapEntry1);
				WholeGraphmap.put(source, listin);
			}
		}
		//变成双向的
		List<String> listin=new ArrayList<String>();
		for(String key:WholeGraphmap.keySet())
		{
			for(MapEntry entry : WholeGraphmap.get(key))
			{
//				System.out.println(key+","+entry.getIdentifier()+","+entry.getWeight());
				listin.add(key+","+entry.getIdentifier()+","+String.valueOf(entry.getWeight()));
			}
		}
		for(int i=0;i<listin.size();i++)
		{
			String[] str=listin.get(i).split(",");
			if(WholeGraphmap.containsKey(str[1]))
				WholeGraphmap.get(str[1]).add(new MapEntry(str[0], Double.parseDouble(str[2])));
			else
			{
				ArrayList<MapEntry> listinin = new ArrayList<MapEntry>();
				listinin.add(new MapEntry(str[0], Double.parseDouble(str[2])));
				WholeGraphmap.put(str[1], listinin);
			}
		}
//		for(String key:WholeGraphmap.keySet())
//		{
//			for(MapEntry entry : WholeGraphmap.get(key))
//			{
//				System.out.println(key+","+entry.getIdentifier()+","+entry.getWeight());
//			}
//		}
		return WholeGraphmap;
	}

	public List<String> getAllNode()
	{
		List<Integer> allnodet=new ArrayList<Integer>();
		List<String> allnode=new ArrayList<String>();
		allnodet=commDao.getNode();
		for(int i=0;i<allnodet.size();i++)
		{
			//System.out.println(allnodet.get(i));
			allnode.add(String.valueOf(allnodet.get(i)));
		}
		return allnode;
	}

	public int getInterFeature(String node1,String node2)
	{
		int result=0;
		result=commDao.getInterNum(Integer.parseInt(node1), Integer.parseInt(node2)).get(0);
		return result;
	}
	public List<String> getTopicFeature(String node)
	{
		List<String> result=new ArrayList<String>();
		result=commDao.getSubject(Integer.parseInt(node));
		return result;
	}
	public int getEmailFeature(String node1,String node2)
	{
		int result=0;
		
		List<Integer> tlist1=new ArrayList<Integer>();
		List<String> list1=new ArrayList<String>();
		tlist1=this.commDao.getMessageid(Integer.parseInt(node1));
		for(int i=0;i<tlist1.size();i++)
			list1.add(String.valueOf(tlist1.get(i)));
		
		List<Integer> tlist2=new ArrayList<Integer>();
		List<String> list2=new ArrayList<String>();
		tlist2=this.commDao.getMessageid(Integer.parseInt(node2));
		for(int i=0;i<tlist2.size();i++)
			list2.add(String.valueOf(tlist2.get(i)));
		
		for(int i=0;i<list1.size();i++)
		{
			for(int j=0;j<list2.size();j++)
			{
				if(list1.get(i).equals(list2.get(j)))
				{
					result++;
					//break;
				}
			}
		}
		return result;
	}
	public int getCloserFeature(String node1,String node2)
	{
		int result=0;
		result=this.commDao.getCloser(Integer.parseInt(node1),Integer.parseInt(node2)).get(0);
		return result;
	}
}
