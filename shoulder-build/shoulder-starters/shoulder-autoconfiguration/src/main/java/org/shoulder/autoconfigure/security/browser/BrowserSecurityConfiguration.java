package org.shoulder.autoconfigure.security.browser;

import org.shoulder.autoconfigure.condition.ConditionalOnAuthType;
import org.shoulder.autoconfigure.security.AuthenticationBeanConfig;
import org.shoulder.autoconfigure.security.code.ValidateCodeSecurityConfig;
import org.shoulder.security.SecurityConst;
import org.shoulder.security.SecurityConst.DefaultPage;
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
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.session.InvalidSessionStrategy;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;

/**
 * session 认证安全配置默认类。支持深度定制，可参考/复制出该类，自己写
 *
 * @author lym
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(SecurityConst.class)
@AutoConfigureAfter(value = {AuthenticationBeanConfig.class, BrowserSessionAuthBeanConfiguration.class})
@ConditionalOnAuthType(type = AuthenticationType.SESSION)
@ConditionalOnMissingBean(WebSecurityConfigurerAdapter.class)
@ConditionalOnProperty(name = "shoulder.security.auth.session.default-config", havingValue = "enable", matchIfMissing = true)
public class BrowserSecurityConfiguration extends WebSecurityConfigurerAdapter {

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

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        formAuthenticationSecurityConfig.configure(http);

        //apply 方法：<C extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity>> C apply(C configurer)

        if (validateCodeSecurityConfig != null) {
            http.apply(validateCodeSecurityConfig);
        }

        if (phoneNumAuthenticationSecurityConfig != null) {
            http.apply(phoneNumAuthenticationSecurityConfig);
        }

        if(persistentTokenRepository != null){
            http
            // 记住我配置，采用 spring security 的默认实现
            // 如果想在'记住我'登录时记录日志，可以注册一个 InteractiveAuthenticationSuccessEvent 事件的监听器
            .rememberMe()
            // 用token拿到用户名
                .tokenRepository(persistentTokenRepository)
                //token有效时间
                .tokenValiditySeconds((int) browserSessionAuthProperties.getRememberMeSeconds().toSeconds())
                // 认证类
                .userDetailsService(userDetailsService);
        }

        http
            // 会话管理器
            .sessionManagement()
                // session 无效策略（首次请求必定无效）
                .invalidSessionStrategy(invalidSessionStrategy)
                    // 同一个用户在系统中的最大session数
                    .maximumSessions(browserSessionAuthProperties.getSession().getMaximumSessions())
                    // 登录同一个用户达到最大数量后，阻止后来的session登录 还是将原有 session 顶替
                    .maxSessionsPreventsLogin(browserSessionAuthProperties.getSession().isMaxSessionsPreventsLogin())
                    // session 过期策略
                    .expiredSessionStrategy(sessionInformationExpiredStrategy)
                .and()
            .and()

            // 退出登录相关配置
            .logout()
                .logoutUrl(SecurityConst.URL_AUTHENTICATION_CANCEL)
                .logoutSuccessUrl(browserSessionAuthProperties.getSignOutSuccessUrl())
                .deleteCookies("JSESSIONID")
            .and()


            // 配置过滤规则
            .authorizeRequests()
                // 不需要登录认证可以访问的
                .antMatchers(
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
                .anyRequest().authenticated()
            .and()

                .csrf()
            .disable()
            // 关闭 csrf

        ;
        // authorizeConfigManager.config(http.authorizeRequests());
        // @formatter:on
    }

}
