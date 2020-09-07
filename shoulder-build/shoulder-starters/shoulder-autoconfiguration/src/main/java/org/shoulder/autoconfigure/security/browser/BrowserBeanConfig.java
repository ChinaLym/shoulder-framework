package org.shoulder.autoconfigure.security.browser;

import org.shoulder.security.SecurityConst;
import org.shoulder.security.authentication.browser.handler.BrowserAuthenticationFailureHandler;
import org.shoulder.security.authentication.browser.handler.BrowserAuthenticationSuccessHandler;
import org.shoulder.security.authentication.browser.handler.BrowserLogoutSuccessHandler;
import org.shoulder.security.authentication.browser.session.ConcurrentLogInExpiredSessionStrategy;
import org.shoulder.security.authentication.browser.session.DefaultInvalidSessionStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
@Configuration(
    proxyBeanMethods = false
)
@ConditionalOnClass(SecurityConst.class)
@EnableConfigurationProperties(BrowserProperties.class)
public class BrowserBeanConfig {

    private static final Logger log = LoggerFactory.getLogger(BrowserBeanConfig.class);

    @Autowired
    private BrowserProperties browserProperties;

    /**
     * 记住我功能的 token 存储类
     * 负责将token写入数据库并且查询出来的bean，用于记住我功能
     */
    @Bean
    @ConditionalOnClass(JdbcTemplate.class)
    public PersistentTokenRepository persistentTokenRepository(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
        tokenRepository.setDataSource(dataSource);

        //启动的时候创建表
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
     * 认证成功处理器
     */
    @Bean
    @ConditionalOnMissingBean(AuthenticationSuccessHandler.class)
    public AuthenticationSuccessHandler browserAuthenticationSuccessHandler() {
        return new BrowserAuthenticationSuccessHandler(browserProperties.getSingInSuccessUrl());
    }

    /**
     * 认证失败处理器
     */
    @Bean
    @ConditionalOnMissingBean(AuthenticationFailureHandler.class)
    public AuthenticationFailureHandler browserAuthenticationFailureHandler() {
        return new BrowserAuthenticationFailureHandler();
    }

    /**
     * 退出登录处理器
     */
    @Bean
    @ConditionalOnMissingBean(LogoutSuccessHandler.class)
    public LogoutSuccessHandler browserLogoutSuccessHandler() {
        return new BrowserLogoutSuccessHandler(browserProperties.getSignOutUrl());
    }


    /**
     * InvalidSessionStrategy
     */
    @Bean
    @ConditionalOnMissingBean(InvalidSessionStrategy.class)
    public InvalidSessionStrategy invalidSessionStrategy() {
        return new DefaultInvalidSessionStrategy(
            browserProperties.getSession().getSessionInvalidUrl(),
            browserProperties.getSignInPage(),
            browserProperties.getSignOutUrl()
        );
    }

    /**
     * SessionInformationExpiredStrategy
     */
    @Bean
    @ConditionalOnMissingBean(SessionInformationExpiredStrategy.class)
    public SessionInformationExpiredStrategy sessionInformationExpiredStrategy() {
        return new ConcurrentLogInExpiredSessionStrategy(
            browserProperties.getSession().getSessionInvalidUrl(),
            browserProperties.getSignInPage(),
            browserProperties.getSignOutUrl()
        );
    }

}
