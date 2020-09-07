package org.shoulder.security.authentication.sms;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * 负责手机号认证
 * todo change UserDetailService
 *
 * @author lym
 */
public class SmsCodeAuthenticationProvider implements AuthenticationProvider {

    private UserDetailsService userDetailsService;

    public SmsCodeAuthenticationProvider(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        SmsCodeAuthenticationToken authenticationToken = (SmsCodeAuthenticationToken) authentication;
        String phoneNumber = (String) authenticationToken.getPrincipal();


        UserDetails user = userDetailsService.loadUserByUsername(phoneNumber);
        if (user == null) {
            throw new InternalAuthenticationServiceException("can't find any userDetail!");
        }

        // 认证通过
        SmsCodeAuthenticationToken authenticationResult =
            new SmsCodeAuthenticationToken(user, user.getAuthorities());

        authenticationResult.setDetails(authentication.getDetails());

        return authenticationResult;
    }


    @Override
    public boolean supports(Class<?> authentication) {
        return SmsCodeAuthenticationToken.class.isAssignableFrom(authentication);
    }

    public UserDetailsService getUserDetailsService() {
        return userDetailsService;
    }

    public void setUserDetailsService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

}
