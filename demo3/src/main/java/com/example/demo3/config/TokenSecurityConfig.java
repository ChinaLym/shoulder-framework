package com.example.demo3.config;

import org.shoulder.autoconfigure.security.code.ValidateCodeSecurityConfig;
import org.shoulder.core.util.ContextUtils;
import org.shoulder.security.SecurityConst;
import org.shoulder.security.authentication.FormAuthenticationSecurityConfig;
import org.shoulder.security.authentication.sms.PhoneNumAuthenticationSecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.server.resource.authentication.OpaqueTokenAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationFilter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.http.HttpServletRequest;

/**
 * token 相关安全配置
 *
 * @author lym
 */
@Configuration
public class TokenSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired(required = false)
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

    @Autowired(required = false)
    MyTokenIntrospector myTokenIntrospector;

    @Autowired(required = false)
    ApplicationContext applicationContext;


    @Bean
    @Override
    protected AuthenticationManager authenticationManager() throws Exception {
        return super.authenticationManager();
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

        http
                .exceptionHandling()
                // 访问拒绝
                .accessDeniedHandler(accessDeniedHandler)
                // 认证拒绝
                .authenticationEntryPoint(authenticationEntryPoint)
        ;

        http
            .userDetailsService(userDetailsService)
            // 配置校验规则（哪些请求要过滤）
            .authorizeRequests()
                .antMatchers(
                    // 未认证的跳转
                    SecurityConst.URL_REQUIRE_AUTHENTICATION,
                    "/token/introspect",

                    // 获取验证码请求
                    SecurityConst.URL_VALIDATE_CODE,

                    // 登录请求
                    // 用户名、密码登录请求
                    SecurityConst.URL_AUTHENTICATION_FORM,
                    // 手机验证码登录请求
                    SecurityConst.URL_AUTHENTICATION_SMS,

                    "/.well-known/jwks.json",
                    "/oauth/**"

                )
                .permitAll()
                //.antMatchers("/user/1").hasAuthority("SCOPE_all")
                //.antMatchers("/user/getOne").hasAuthority("SCOPE_cant_access")

                // 其余请求全部开启认证（需要登录）
                .anyRequest().authenticated()

            .and()
                // 关闭 csrf f
                .csrf()
            .disable();

        AuthenticationManagerResolver<HttpServletRequest> resolver = httpServletRequest -> ContextUtils.getBean(AuthenticationManager.class);
        http.addFilter(new BearerTokenAuthenticationFilter(resolver))
            .authenticationProvider(new OpaqueTokenAuthenticationProvider(myTokenIntrospector))
        ;
        // 添加 token 解析器
        // 添加 token

        //new OAuth2ResourceServerConfigurer<HttpSecurity>(applicationContext).configure(http);


        //authorizeConfigManager.config(http.authorizeRequests());

        // @formatter:on
    }

}
