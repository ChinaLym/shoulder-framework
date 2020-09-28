package com.example.demo6.config;

import org.shoulder.autoconfigure.security.code.ValidateCodeSecurityConfig;
import org.shoulder.security.SecurityConst;
import org.shoulder.security.authentication.FormAuthenticationSecurityConfig;
import org.shoulder.security.authentication.sms.PhoneNumAuthenticationSecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.oauth2.server.resource.introspection.NimbusOpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * todo {@link NimbusOpaqueTokenIntrospector#adaptToNimbusResponse(org.springframework.http.ResponseEntity)} 可能NPE
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

    //@Autowired
    //private LogoutSuccessHandler logoutSuccessHandler;

    @Autowired(required = false)
    private OpaqueTokenIntrospector opaqueTokenIntrospector;

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

        if(opaqueTokenIntrospector == null){
            opaqueTokenIntrospector = new MockOpaqueTokenIntrospector();
        }

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
                    SecurityConst.URL_AUTHENTICATION_SMS

                )
                .permitAll()

                // 其余请求全部开启认证（需要登录）
                .anyRequest().authenticated()

            .and()
                // 关闭 csrf
                .csrf()
            .disable()
                .oauth2ResourceServer()
                    .opaqueToken()
            // token 校验地址
                        .introspectionUri("http://localhost:8080/token/introspect")
            // 自己的 ak/sk
                        .introspectionClientCredentials("shoulder", "shoulder")

        ;

        //authorizeConfigManager.config(http.authorizeRequests());

        // @formatter:on
    }


    class MockOpaqueTokenIntrospector implements OpaqueTokenIntrospector {

        @Override
        public OAuth2AuthenticatedPrincipal introspect(String s) {
            Map<String, Object> auth = new HashMap<>(1);
            auth.put("name", "testOAuth2UserAuthority");
            Map<String, Object> map = new HashMap<>(1);
            map.put("name", "shoulder-name");
            return new DefaultOAuth2User(List.of(new OAuth2UserAuthority(auth)), map, "name");
        }
    }
}
