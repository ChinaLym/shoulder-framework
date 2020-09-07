package org.shoulder.auth.token.config;

import org.shoulder.auth.token.properties.OAuth2Properties;
import org.shoulder.code.config.ValidateCodeSecurityConfig;
import org.shoulder.security.authentication.FormAuthenticationSecurityConfig;
import org.shoulder.security.authentication.sms.SmsCodeAuthenticationSecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

/**
 * 资源服务器配置
 *
 * @author lym
 */
@Configuration(
    proxyBeanMethods = false
)
@EnableResourceServer
@EnableConfigurationProperties(OAuth2Properties.class)
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

    /**
     * 认证成功处理器
     */
    @Autowired
    protected AuthenticationSuccessHandler authenticationSuccessHandler;
    /**
     * 认证失败处理器
     */
    @Autowired
    protected AuthenticationFailureHandler authenticationFailureHandler;
    /**
     * 验证码功能
     */
    @Autowired
    private ValidateCodeSecurityConfig validateCodeSecurityConfig;
    /**
     * 短信验证码认证方式
     */
    @Autowired
    private SmsCodeAuthenticationSecurityConfig smsCodeAuthenticationSecurityConfig;
    /**
     * openId 认证方式
     */
    //@Autowired
    //private OpenIdAuthenticationSecurityConfig openIdAuthenticationSecurityConfig;


    //@Autowired
    //private SpringSocialConfigurer socialSecurityConfig;

    //@Autowired
    //private AuthorizeConfigManager authorizeConfigManager;

    @Autowired
    private FormAuthenticationSecurityConfig formAuthenticationConfig;

    @Override
    public void configure(HttpSecurity http) throws Exception {

        formAuthenticationConfig.configure(http);

        http.apply(validateCodeSecurityConfig)
            .and()
            .apply(smsCodeAuthenticationSecurityConfig)
            .and()
            /*.apply(socialSecurityConfig)
                .and()
            .apply(openIdAuthenticationSecurityConfig)
                .and()*/
            .csrf().disable();

        //authorizeConfigManager.config(http.authorizeRequests());
    }

}
