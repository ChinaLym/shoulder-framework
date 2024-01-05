package org.shoulder.security.authentication.sms;

import jakarta.annotation.Nullable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * 手机号认证
 *
 * 使用方：框架SPI，使用者强感知，需要实现该接口获取用户信息，否则sms认证能力无法生效
 * 类定位：根据手机号获取用户信息
 *
 * @author lym
 */
public interface PhoneNumAuthenticateService {

    /**
     * 通过收集号码查询用户信息
     *
     * @param phoneNum 手机号
     * @return 用户信息，不存在则为 null
     * @throws UsernameNotFoundException 用户不存在
     */
    @Nullable
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
