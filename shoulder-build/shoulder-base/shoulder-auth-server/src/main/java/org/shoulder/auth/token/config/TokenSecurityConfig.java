package org.shoulder.auth.token.config;

import org.shoulder.code.config.ValidateCodeSecurityConfig;
import org.shoulder.security.SecurityConst;
import org.shoulder.security.authentication.FormAuthenticationSecurityConfig;
import org.shoulder.security.authentication.sms.SmsCodeAuthenticationSecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * 浏览器环境下安全配置主类
 *
 * @author lym
 */
@Configuration(
    proxyBeanMethods = false
)
public class TokenSecurityConfig extends WebSecurityConfigurerAdapter {


    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private ValidateCodeSecurityConfig validateCodeSecurityConfig;

    @Autowired
    private SmsCodeAuthenticationSecurityConfig smsCodeAuthenticationSecurityConfig;

    @Autowired
    private FormAuthenticationSecurityConfig formAuthenticationSecurityConfig;


    @Override
    protected void configure(HttpSecurity http) throws Exception {

        formAuthenticationSecurityConfig.configure(http);

        //apply 方法：<C extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity>> C apply(C configurer)

        http.apply(validateCodeSecurityConfig).and()
                .apply(smsCodeAuthenticationSecurityConfig).and()

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

                // 关闭 csrf
                .csrf().disable();

        //authorizeConfigManager.config(http.authorizeRequests());

    }

}
