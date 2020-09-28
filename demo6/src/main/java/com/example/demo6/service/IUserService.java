package com.example.demo6.service;

import com.example.demo6.entity.UserEntity;
import org.shoulder.data.mybatis.base.service.IBaseService;
import org.shoulder.security.authentication.sms.PhoneNumAuthenticateService;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * @author lym
 */
public interface IUserService extends IBaseService<UserEntity>, UserDetailsService, PhoneNumAuthenticateService {


}
