package com.buaa.yunyun.dao;

import java.util.List;

import com.buaa.yunyun.pojo.Message;

public interface MessageDao {
	List<Message> getMessages(String keyword);
}
