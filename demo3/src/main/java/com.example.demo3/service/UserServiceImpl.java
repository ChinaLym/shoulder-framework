package com.example.demo3.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo3.entity.UserEntity;
import com.example.demo3.repository.UserMapper;
import org.shoulder.data.mybatis.base.service.BaseServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 根据用户名查找用户信息
        LambdaQueryWrapper<UserEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserEntity::getUsername, username);
        UserEntity user = this.getOne(queryWrapper);
        if (user == null) {
            throw new UsernameNotFoundException("userName(" + username + ") not exist");
        }
        //根据查找到的用户信息判断用户是否被冻结，这里直接设置为可用
        return new User(user.getUsername(), user.getPassword(),
                true, true, true, true,
                AuthorityUtils.commaSeparatedStringToAuthorityList("admin"));
    }

    /**
     * 保存时加密
     */
    @Override
    public boolean save(UserEntity entity) {
        entity.setPassword(passwordEncoder.encode(entity.getPassword()));
        return super.save(entity);
    }

}
