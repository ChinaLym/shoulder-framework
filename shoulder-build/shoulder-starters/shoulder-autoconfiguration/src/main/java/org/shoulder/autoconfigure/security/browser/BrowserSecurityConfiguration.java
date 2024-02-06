package org.shoulder.autoconfigure.security.browser;

import org.shoulder.autoconfigure.condition.ConditionalOnAuthType;
import org.shoulder.autoconfigure.security.AuthenticationBeanConfig;
import org.shoulder.autoconfigure.security.code.ValidateCodeSecurityConfig;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;
import org.shoulder.security.SecurityConst;
import org.shoulder.security.SecurityConst.DefaultPage;
import org.shoulder.security.authentication.AuthenticationType;
import org.shoulder.security.authentication.FormAuthenticationSecurityConfig;
import org.shoulder.security.authentication.sms.PhoneNumAuthenticationSecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.session.InvalidSessionStrategy;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;

/**
 * session 认证安全配置默认类。支持深度定制，建议安全配置时继承或复制出来自己写，该类仅作默认的配置与写法参考
 *
 * @author lym
 */
@AutoConfiguration(after = {AuthenticationBeanConfig.class, BrowserSessionAuthBeanConfiguration.class})
@EnableWebSecurity
@ConditionalOnClass(SecurityConst.class)
@ConditionalOnAuthType(type = AuthenticationType.SESSION)
@ConditionalOnProperty(name = "shoulder.security.auth.session.default-config", havingValue = "enable", matchIfMissing = true)
public class BrowserSecurityConfiguration {

    @Autowired
    private BrowserSessionAuthProperties browserSessionAuthProperties;

    /**
     * 用户表 service
     */
    @Autowired
    private UserDetailsService userDetailsService;

    /**
     * 表单登录
     */
    @Autowired
    private FormAuthenticationSecurityConfig formAuthenticationSecurityConfig;

    @Autowired
    private SessionInformationExpiredStrategy sessionInformationExpiredStrategy;

    @Autowired
    private InvalidSessionStrategy invalidSessionStrategy;

    /**
     * 验证码相关配置（可选）
     */
    @Autowired(required = false)
    private ValidateCodeSecurityConfig validateCodeSecurityConfig;

    /**
     * 短信验证码认证（可选）
     */
    @Autowired(required = false)
    private PhoneNumAuthenticationSecurityConfig phoneNumAuthenticationSecurityConfig;

    /**
     * remember （可选）
     */
    @Autowired(required = false)
    private PersistentTokenRepository persistentTokenRepository;


    public BrowserSecurityConfiguration() {
        // 提示使用了默认的，一般都是自定义
        Logger log = LoggerFactory.getLogger(getClass());
        log.warn("use default BrowserSecurityConfiguration, csrf protect was closed.");
    }

    @Bean
    public SecurityFilterChain securityFilterChain_shoulderBrowserSecurityDefaultConfigure(HttpSecurity http) throws Exception {
        // @formatter:off
        // @formatter:on

        formAuthenticationSecurityConfig.setLoginPageUrl(browserSessionAuthProperties.getSignInPage());
        formAuthenticationSecurityConfig.configure(http);

        //apply 方法：<C extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity>> C apply(C configurer)

        if (validateCodeSecurityConfig != null) {
            http.with(validateCodeSecurityConfig, c -> {});
        }

        if (phoneNumAuthenticationSecurityConfig != null) {
            http.with(phoneNumAuthenticationSecurityConfig, c -> {});
        }

        // 记住我配置，采用 spring security 的默认实现
        // 如果想在'记住我'登录时记录日志，可以注册一个 InteractiveAuthenticationSuccessEvent 事件的监听器
        if (persistentTokenRepository != null) {
            http.rememberMe(rememberMeConfigurer -> {
                // 用token拿到用户名
                rememberMeConfigurer.tokenRepository(persistentTokenRepository)
                        //token有效时间
                        .tokenValiditySeconds((int) browserSessionAuthProperties.getRememberMeSeconds().toSeconds())
                        // 认证类
                        .userDetailsService(userDetailsService);
            });

        }

        http.sessionManagement(sessionManagementConfigurer -> // session 无效策略（首次请求必定无效）
                        // 会话管理器
                        sessionManagementConfigurer.invalidSessionStrategy(invalidSessionStrategy)
                                // 同一个用户在系统中的最大session数
                                .maximumSessions(browserSessionAuthProperties.getSession().getMaximumSessions())
                                // 登录同一个用户达到最大数量后，阻止后来的session登录 还是将原有 session 顶替
                                .maxSessionsPreventsLogin(browserSessionAuthProperties.getSession().isMaxSessionsPreventsLogin())
                                // session 过期策略
                                .expiredSessionStrategy(sessionInformationExpiredStrategy))

                // 退出登录相关配置
                .logout(logoutConfigurer ->
                    logoutConfigurer.logoutUrl(SecurityConst.URL_AUTHENTICATION_CANCEL)
                        .logoutSuccessUrl(browserSessionAuthProperties.getSignOutSuccessUrl())
                        .deleteCookies("JSESSIONID"))


                // 配置过滤规则
                .authorizeHttpRequests(authenticationSecurityConfigurer ->
                        authenticationSecurityConfigurer.requestMatchers(
                                        // error
                                        "/error",

                                        // 未认证的跳转
                                        SecurityConst.URL_REQUIRE_AUTHENTICATION,

                                        // 登录页面
                                        browserSessionAuthProperties.getSignUpUrl(),
                                        // 获取验证码请求
                                        SecurityConst.URL_VALIDATE_CODE,

                                        // 登录请求
                                        // 用户名、密码登录请求
                                        browserSessionAuthProperties.getSignInPage(),
                                        // 手机验证码登录请求
                                        SecurityConst.URL_AUTHENTICATION_SMS,

                                        // 退出登录跳转的请求
                                        browserSessionAuthProperties.getSignOutSuccessUrl(),

                                        // 注册页面
                                        DefaultPage.SIGN_UP,
                                        // 注册请求
                                        SecurityConst.URL_REGISTER,

                                        // session失效默认的跳转地址
                                        browserSessionAuthProperties.getSession().getSessionInvalidUrl()
                                )
                                .permitAll()

                                // 其余请求全部开启认证（需要登录）
                                .anyRequest().authenticated());
        // 不需要登录认证可以访问的


        // csrf 在开发阶段不友好，默认关闭
        http.csrf(AbstractHttpConfigurer::disable);

        // authorizeConfigManager.config(http.authorizeRequests());
        return http.build();
    }

}
