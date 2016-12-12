package com.buaa.yunyun.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.buaa.yunyun.pojo.Message;
import com.buaa.yunyun.service.MessageService;

@Controller
@RequestMapping("/Message")
public class MessageController {
	@Autowired
    @Qualifier("MessageService")
    private MessageService messageService;
	
	@RequestMapping(value ="/GetMessageByKeyWord",method = RequestMethod.POST)
	@ResponseBody
	public Map getMessageByKeyWord(HttpServletRequest req, HttpServletResponse resp){
		Map resultMap=new HashMap();		
		Map<String, Message> messages = new HashMap<String, Message>();
		//System.out.println(req);
		String keyword=req.getParameter("keyword").trim();
		System.out.println(keyword);
		messages=messageService.getMessages(keyword);
		for(String key:messages.keySet())
		{
			resultMap.put(key, messages.get(key).getContent());
			
		}
		return resultMap;
	}
}
