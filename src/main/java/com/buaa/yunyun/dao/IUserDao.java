package com.buaa.yunyun.dao;

import org.springframework.stereotype.Repository;

import com.buaa.yunyun.pojo.User;
@Repository
public interface IUserDao {
	int deleteByPrimaryKey(String userName);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(String userName);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);
}
