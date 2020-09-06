package com.example.demo2.service;

import com.example.demo2.entity.UserEntity;
import com.example.demo2.repository.UserMapper;
import org.shoulder.data.mybatis.base.service.BaseServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @author lym
 */
@Service
public class UserServiceImpl extends BaseServiceImpl<UserMapper, UserEntity> implements IUserService {


}
