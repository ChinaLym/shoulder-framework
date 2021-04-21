package com.example.demo4.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo4.entity.UserEntity;
import com.example.demo4.repository.UserMapper;
import org.shoulder.data.mybatis.base.service.BaseServiceImpl;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * @author lym
 */
@Service
public class UserServiceImpl extends BaseServiceImpl<UserMapper, UserEntity> implements IUserService {

    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 根据用户名查找用户信息，在这打断点可以追踪 spring security 是如何运作的
        LambdaQueryWrapper<UserEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserEntity::getUsername, username);
        UserEntity user = this.getOne(queryWrapper);
        if (user == null) {
            throw new UsernameNotFoundException("userName(" + username + ") not exist");
        }
        return toUserDetail(user);
    }

    /**
     * 保存时加密
     */
    @Override
    public boolean save(UserEntity entity) {
        entity.setPassword(passwordEncoder.encode(entity.getPassword()));
        return super.save(entity);
    }

    @Override
    public UserDetails loadUserByPhoneNum(String phoneNum) throws UsernameNotFoundException {
        // 已经自动注入 bizService，即 IUserService
        LambdaQueryWrapper<UserEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(UserEntity::getPhoneNum, phoneNum);
        UserEntity user = this.getOne(queryWrapper);

        if (user == null) {
            throw new UsernameNotFoundException("phoneNum(" + phoneNum + ") not exist");
        }
        return toUserDetail(user);
    }


    /**
     * 转为 spring security 默认认证器定义的 user 类型
     */
    private UserDetails toUserDetail(UserEntity user) {

        //根据查找到的用户信息判断用户是否被冻结，这里直接设置为可用
        return new User(user.getUsername(), user.getPassword(),
                true, true, true, true,
                AuthorityUtils.commaSeparatedStringToAuthorityList("admin"));
    }
}
