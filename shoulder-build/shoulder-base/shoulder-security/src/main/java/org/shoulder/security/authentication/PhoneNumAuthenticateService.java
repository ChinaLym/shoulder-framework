package org.shoulder.security.authentication;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * 手机号认证
 *
 * @author lym
 */
public interface PhoneNumAuthenticateService {

    /**
     * 通过收集号码查询用户信息
     *
     * @param phoneNum 手机号
     * @return 用户信息
     * @throws UsernameNotFoundException 用户不存在
     */
    default UserDetails loadUserByPhoneNum(String phoneNum) throws UsernameNotFoundException {
        throw new UsernameNotFoundException("not support");
    }

    /**
     * 使用第三方账号查询用户信息
     * @param type 第三方账号类型（策略）
     * @param identity 第三方账号标识
     * @return 用户信息
     * @throws UsernameNotFoundException 用户不存在
     */
    /*@Deprecated
    default UserDetails loadUserBy(String type, String identity) throws UsernameNotFoundException {
        throw new UsernameNotFoundException("not support");
    }*/


}
