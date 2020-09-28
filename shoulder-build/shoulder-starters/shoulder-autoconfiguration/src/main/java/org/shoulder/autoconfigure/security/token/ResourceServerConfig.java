package org.shoulder.autoconfigure.security.token;

import org.shoulder.autoconfigure.condition.ConditionalOnAuthType;
import org.shoulder.autoconfigure.security.code.ValidateCodeSecurityConfig;
import org.shoulder.security.authentication.AuthenticationType;
import org.shoulder.security.authentication.FormAuthenticationSecurityConfig;
import org.shoulder.security.authentication.sms.PhoneNumAuthenticationSecurityConfig;
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
@Deprecated
@Configuration(proxyBeanMethods = false)
@EnableResourceServer
@EnableConfigurationProperties(TokenProperties.class)
@ConditionalOnAuthType(type = AuthenticationType.TOKEN)
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {


    @Autowired
    private FormAuthenticationSecurityConfig formAuthenticationConfig;
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
     * 验证码功能（可选）
     */
    @Autowired(required = false)
    private ValidateCodeSecurityConfig validateCodeSecurityConfig;

    /**
     * 短信验证码认证（可选）
     */
    @Autowired(required = false)
    private PhoneNumAuthenticationSecurityConfig phoneNumAuthenticationSecurityConfig;
    /**
     * openId 认证方式
     */
    //@Autowired
    //private OpenIdAuthenticationSecurityConfig openIdAuthenticationSecurityConfig;


    //@Autowired
    //private SpringSocialConfigurer socialSecurityConfig;

    //@Autowired
    //private AuthorizeConfigManager authorizeConfigManager;

    @Override
    public void configure(HttpSecurity http) throws Exception {

        formAuthenticationConfig.configure(http);

        if (validateCodeSecurityConfig != null) {
            http.apply(validateCodeSecurityConfig);
        }

        if (phoneNumAuthenticationSecurityConfig != null) {
            http.apply(phoneNumAuthenticationSecurityConfig);
        }

        http
            /*.apply(socialSecurityConfig)
                .and()
            .apply(openIdAuthenticationSecurityConfig)
                .and()*/
            .csrf().disable();

        //authorizeConfigManager.config(http.authorizeRequests());
    }

}
