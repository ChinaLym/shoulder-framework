package org.shoulder.security.authentication.sms;

import org.shoulder.security.authentication.PhoneNumAuthenticateService;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 负责手机号认证处理，可以结合其他安全过滤器实现 手机号 + 短信验证码登录、手机号 + 密码登录、手机号 + 邮件验证码登录等。
 *
 * @author lym
 */
public class PhoneNumAuthenticationProvider implements AuthenticationProvider {

    private PhoneNumAuthenticateService phoneNumAuthenticateService;

    public PhoneNumAuthenticationProvider(@NonNull PhoneNumAuthenticateService phoneNumAuthenticateService) {
        this.phoneNumAuthenticateService = phoneNumAuthenticateService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        // 从 authentication 中获取手机号
        PhoneNumAuthenticationToken authenticationToken = (PhoneNumAuthenticationToken) authentication;
        String phoneNumber = (String) authenticationToken.getPrincipal();

        // 通过手机号获取用户信息
        UserDetails user = phoneNumAuthenticateService.loadUserByPhoneNum(phoneNumber);
        if (user == null) {
            // 如果用户信息不存在，则登录失败
            throw new InternalAuthenticationServiceException("can't find any userDetail!");
        }

        // 只要查到用户信息即认证通过，因为手机短信验证是通过验 证码过滤器负责，这样可以实现验证码过滤器更好的灵活性
        PhoneNumAuthenticationToken authenticationResult =
            new PhoneNumAuthenticationToken(user, user.getAuthorities());

        authenticationResult.setDetails(authentication.getDetails());

        return authenticationResult;
    }

    /**
     * 支持验证什么类型的 authentication
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return PhoneNumAuthenticationToken.class.isAssignableFrom(authentication);
    }

    public PhoneNumAuthenticateService getPhoneNumAuthenticateService() {
        return phoneNumAuthenticateService;
    }

    @Deprecated
    public void setPhoneNumAuthenticateService(PhoneNumAuthenticateService userDetailsService) {
        this.phoneNumAuthenticateService = userDetailsService;
    }

}
