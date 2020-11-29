package org.shoulder.autoconfigure.security.token;

import org.shoulder.autoconfigure.condition.ConditionalOnAuthType;
import org.shoulder.autoconfigure.security.code.ValidateCodeSecurityConfig;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;
import org.shoulder.security.SecurityConst;
import org.shoulder.security.authentication.AuthenticationType;
import org.shoulder.security.authentication.FormAuthenticationSecurityConfig;
import org.shoulder.security.authentication.sms.PhoneNumAuthenticationSecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

/**
 * token 模式下安全配置主类
 * 使用者继承 WebSecurityConfigurerAdapter 或本类完成更多配置
 *
 * @author lym
 */
@EnableWebSecurity// 就算不写 spring boot 也会自动识别。WebSecurityEnablerConfiguration
@Configuration
@ConditionalOnClass(SecurityConst.class)
@AutoConfigureAfter(value = TokenAuthBeanConfig.class)
@ConditionalOnAuthType(type = AuthenticationType.TOKEN)
@ConditionalOnMissingBean(WebSecurityConfigurerAdapter.class)
@ConditionalOnProperty(name = "shoulder.security.auth.token.default-config", havingValue = "enable", matchIfMissing = true)
public class TokenSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private FormAuthenticationSecurityConfig formAuthenticationSecurityConfig;
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

    @Autowired(required = false)
    AuthenticationEntryPoint authenticationEntryPoint;

    @Autowired(required = false)
    AccessDeniedHandler accessDeniedHandler;

    public TokenSecurityConfig() {
        // 提示使用了默认的，一般都是自定义
        Logger log = LoggerFactory.getLogger(getClass());
        log.warn("use default TokenSecurityConfig, csrf protect was closed.");
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        formAuthenticationSecurityConfig.configure(http);
        //apply 方法：<C extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity>> C apply(C configurer)

        if (validateCodeSecurityConfig != null) {
            http.apply(validateCodeSecurityConfig);
        }

        if (phoneNumAuthenticationSecurityConfig != null) {
            http.apply(phoneNumAuthenticationSecurityConfig);
        }

        if(accessDeniedHandler != null){
             http
                .exceptionHandling()
                // 403 权限不够，拒绝访问
                .accessDeniedHandler(accessDeniedHandler);
        }
        if(authenticationEntryPoint != null){
             http
                .exceptionHandling()
                // 401 未认证，拒绝访问
                .authenticationEntryPoint(authenticationEntryPoint);
        }

        http
            .userDetailsService(userDetailsService)
            // 配置校验规则（哪些请求要过滤）
            .authorizeRequests()
                .antMatchers(
                    // 未认证的跳转
                    SecurityConst.URL_REQUIRE_AUTHENTICATION,

                    // 获取验证码请求
                    SecurityConst.URL_VALIDATE_CODE,

                    // 登录请求
                    // 用户名、密码登录请求
                    SecurityConst.URL_AUTHENTICATION_FORM,
                    // 手机验证码登录请求
                    SecurityConst.URL_AUTHENTICATION_SMS

                )
                .permitAll()

                // 其余请求全部开启认证（需要登录）
                .anyRequest().authenticated()

            .and()
                .csrf()
            .disable()
        ;

        // @formatter:on
    }

}
