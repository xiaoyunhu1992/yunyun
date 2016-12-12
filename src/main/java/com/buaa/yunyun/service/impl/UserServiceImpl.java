package com.buaa.yunyun.service.impl;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.buaa.yunyun.dao.IUserDao;
import com.buaa.yunyun.pojo.User;
import com.buaa.yunyun.service.IUserService;

@Service("userService")  
public class UserServiceImpl implements IUserService {  
    @Resource  
    private IUserDao userDao;  
    @Override  
    public User getUserById(String userName) {  
        // TODO Auto-generated method stub  
        return this.userDao.selectByPrimaryKey(userName);  
    }  
  
}  
