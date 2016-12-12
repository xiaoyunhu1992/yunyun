package com.buaa.yunyun.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.buaa.yunyun.dao.MessageDao;
import com.buaa.yunyun.pojo.Message;

@Service("MessageService") 
public class MessageService {
	@Resource  
    private MessageDao messageDao;
	
	public Map getMessages(String keyword){
		Map<String, Message> messages = new HashMap<String, Message>();
		List<Message> queryresult=messageDao.getMessages(keyword);
		for(int i=0;i<queryresult.size();i++)
		{
			messages.put(String.valueOf(i), queryresult.get(i));
			System.out.println(queryresult.get(i).getContent());
		}
		return messages;
	}
}
