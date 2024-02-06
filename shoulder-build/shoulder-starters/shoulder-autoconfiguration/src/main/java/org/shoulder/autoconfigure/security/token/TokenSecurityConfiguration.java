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
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.server.resource.authentication.OpaqueTokenAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationFilter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.ExceptionTranslationFilter;

/**
 * token 认证安全配置默认类。支持深度定制，建议安全配置时继承或复制出来自己写，该类仅作默认的配置与写法参考
 *
 * @author lym
 */
@EnableWebSecurity
@AutoConfiguration(after = TokenAuthBeanConfiguration.class)
@ConditionalOnClass(SecurityConst.class)
@ConditionalOnAuthType(type = AuthenticationType.TOKEN)
@ConditionalOnProperty(name = "shoulder.security.auth.token.default-config", havingValue = "enable", matchIfMissing = true)
public class TokenSecurityConfiguration {

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

    @Autowired(required = false)
    private OpaqueTokenAuthenticationProvider tokenAuthenticationProvider;

    @Lazy
    @Autowired
    private AuthenticationManager authenticationManager = null;

    public TokenSecurityConfiguration() {
        // 提示使用了默认的，一般都是自定义
        Logger log = LoggerFactory.getLogger(getClass());
        log.warn("use default TokenSecurityConfiguration, csrf protect was closed.");
    }

    @Bean
    public SecurityFilterChain securityFilterChain_shoulderBrowserSecurityDefaultConfigure(HttpSecurity http) throws Exception {
        // @formatter:off
        // @formatter:on
        formAuthenticationSecurityConfig.configure(http);
        //apply 方法：<C extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity>> C apply(C configurer)

        if (validateCodeSecurityConfig != null) {
            http.with(validateCodeSecurityConfig, c -> {});
        }

        if (phoneNumAuthenticationSecurityConfig != null) {
            http.with(phoneNumAuthenticationSecurityConfig, c -> {});
        }

        if (accessDeniedHandler != null) {
            // 403 权限不够，拒绝访问
            http.exceptionHandling(exceptionHandlingConfigurer -> exceptionHandlingConfigurer.accessDeniedHandler(accessDeniedHandler));
        }
        if (authenticationEntryPoint != null) {
            // 401 未认证，拒绝访问
            http.exceptionHandling(exceptionHandlingConfigurer -> exceptionHandlingConfigurer.authenticationEntryPoint(authenticationEntryPoint));
        }

        http
                .userDetailsService(userDetailsService)
                // 配置校验规则（哪些请求要过滤）
                .authorizeHttpRequests(authenticationSecurityConfigurer ->
                        authenticationSecurityConfigurer.requestMatchers(
                                        // error
                                        "/error",
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
                                .anyRequest().authenticated());
        // csrf 在开发阶段不友好，默认关闭
        http.csrf(AbstractHttpConfigurer::disable);

        if (tokenAuthenticationProvider != null) {
            // oauth2 的未继承 AuthenticationFilter，顺序，异常都需要自行处理，更完善的使用要么定制要么用停止维护的 spring oauth2
            http.addFilterAfter(new BearerTokenAuthenticationFilter((AuthenticationManager)
                            authentication -> tokenAuthenticationProvider.authenticate(authentication)),
                    ExceptionTranslationFilter.class);
            http.authenticationProvider(tokenAuthenticationProvider);

        }
        return http.build();
    }

}
