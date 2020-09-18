package org.shoulder.autoconfigure.security.browser;

import org.shoulder.autoconfigure.condition.ConditionalOnAuthType;
import org.shoulder.autoconfigure.security.AuthenticationBeanConfig;
import org.shoulder.security.SecurityConst;
import org.shoulder.security.authentication.AuthenticationType;
import org.shoulder.security.authentication.browser.BrowserAuthEndpoint;
import org.shoulder.security.authentication.browser.handler.BrowserAuthenticationFailureHandler;
import org.shoulder.security.authentication.browser.handler.BrowserAuthenticationSuccessHandler;
import org.shoulder.security.authentication.browser.handler.BrowserLogoutSuccessHandler;
import org.shoulder.security.authentication.browser.session.ConcurrentLogInExpiredSessionStrategy;
import org.shoulder.security.authentication.browser.session.DefaultInvalidSessionStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.session.InvalidSessionStrategy;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;

import javax.sql.DataSource;

/**
 * 浏览器相关 bean 配置：几个默认处理器、session 策略
 *
 * @author lym
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(SecurityConst.class)
@AutoConfigureAfter(AuthenticationBeanConfig.class)
@EnableConfigurationProperties(BrowserSessionAuthProperties.class)
@ConditionalOnAuthType(type = AuthenticationType.SESSION)
public class BrowserSessionAuthBeanConfiguration {

    private static final Logger log = LoggerFactory.getLogger(BrowserSessionAuthBeanConfiguration.class);

    private final BrowserSessionAuthProperties browserSessionAuthProperties;

    public BrowserSessionAuthBeanConfiguration(BrowserSessionAuthProperties browserSessionAuthProperties) {
        this.browserSessionAuthProperties = browserSessionAuthProperties;
    }

    /**
     * 记住我功能的 token 存储类
     * 负责将token写入数据库并且查询出来的bean，用于记住我功能
     * 需要存在 persistent_logins 表
     */
    @Bean
    @ConditionalOnClass(JdbcTemplate.class)
    @ConditionalOnBean(DataSource.class)
    @ConditionalOnMissingBean
    public PersistentTokenRepository persistentTokenRepository(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        // tokenRepository todo spring security 不支持修改表名 shoulder 未来支持表名修改
        JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
        tokenRepository.setDataSource(dataSource);

        // 启动的时候创建表 persistent_logins (MySQL 语法) fixme 数据库连接账号可能没有建表权限，因此推荐用户手动创建
        String tableName = JdbcTokenRepositoryImpl.CREATE_TABLE_SQL.split(" ")[2];
        String testTableExitsSql = "SELECT table_name FROM information_schema.TABLES WHERE table_name ='" + tableName + "';";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(testTableExitsSql);
        boolean isTableExits = rowSet.next();
        if (!isTableExits) {
            // 只有不存在时才创建表
            log.info("Table(" + tableName + ") not exists in your database. It will be created by spring security.");
            tokenRepository.setCreateTableOnStartup(true);
        } else {
            log.info("Table(" + tableName + ") already exists in your database.");
        }

        return tokenRepository;
    }

    /**
     * 认证成功逻辑
     */
    @Bean
    @ConditionalOnMissingBean(AuthenticationSuccessHandler.class)
    public AuthenticationSuccessHandler browserAuthenticationSuccessHandler() {
        return new BrowserAuthenticationSuccessHandler(
            browserSessionAuthProperties.getResponseType(), browserSessionAuthProperties.getSignInSuccessUrl());
    }

    /**
     * 默认认证失败逻辑
     * 登录失败后跳转到登录页面
     */
    @Bean
    @ConditionalOnMissingBean(AuthenticationFailureHandler.class)
    public AuthenticationFailureHandler browserAuthenticationFailureHandler() {
        return new BrowserAuthenticationFailureHandler(browserSessionAuthProperties.getResponseType(),
            SecurityConst.URL_REQUIRE_AUTHENTICATION);
    }

    /**
     * 默认退出登录逻辑
     */
    @Bean
    @ConditionalOnMissingBean(LogoutSuccessHandler.class)
    public LogoutSuccessHandler browserLogoutSuccessHandler() {
        return new BrowserLogoutSuccessHandler(browserSessionAuthProperties.getResponseType(),
            browserSessionAuthProperties.getSignOutSuccessUrl());
    }


    /**
     * session 无效处理策略
     */
    @Bean
    @ConditionalOnMissingBean(InvalidSessionStrategy.class)
    public InvalidSessionStrategy invalidSessionStrategy() {
        return new DefaultInvalidSessionStrategy(
            browserSessionAuthProperties.getSession().getSessionInvalidUrl(),
            browserSessionAuthProperties.getSignInPage(),
            browserSessionAuthProperties.getSignOutSuccessUrl()
        );
    }

    /**
     * 并发登录导致session失效时，默认的处理策略
     */
    @Bean
    @ConditionalOnMissingBean(SessionInformationExpiredStrategy.class)
    public SessionInformationExpiredStrategy sessionInformationExpiredStrategy() {
        return new ConcurrentLogInExpiredSessionStrategy(
            browserSessionAuthProperties.getSession().getSessionInvalidUrl(),
            browserSessionAuthProperties.getSignInPage(),
            browserSessionAuthProperties.getSignOutSuccessUrl()
        );
    }


    /**
     * 待认证请求处理器，负责将请求跳转至登陆界面
     *
     * @return 待认证请求处理器
     */
    @Bean
    @ConditionalOnProperty(value = "shoulder.security.auth.browser.default-endpoint.enable", havingValue = "true", matchIfMissing = true)
    public BrowserAuthEndpoint browserAuthEndpoint() {
        return new BrowserAuthEndpoint(browserSessionAuthProperties.getSignInPage());
    }


}
