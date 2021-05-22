package org.shoulder.autoconfigure.security.browser;

import org.shoulder.security.SecurityConst.DefaultPage;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * 浏览器环境配置项
 * 如果使用者自行实现登录成功、退出成功处理器，则这些配置将失效，使用者实现的优先生效
 *
 * @author lym
 */
@ConfigurationProperties(prefix = "shoulder.security.auth")
public class BrowserSessionAuthProperties {

    /**
     * session管理配置项
     */
    private SessionProperties session = new SessionProperties();

    /**
     * 登录页面，当引发登录行为的url以html结尾时，会跳到这里配置的url上
     */
    private String signInPage = DefaultPage.SIGN_IN;
    /**
     * 注册页面 url。社交登录，若需要用户注册，跳转的页面（使用第三方授权后，先检查系统中是否已经存在对应的用户，若不存在则引导至注册补全信息页面）
     */
    private String signUpUrl = DefaultPage.SIGN_UP;

    /**
     * '记住我'功能的有效时间，默认一个月
     */
    @DurationUnit(ChronoUnit.DAYS)
    private Duration rememberMeSeconds = Duration.ofDays(30);
    /**
     * 登录成功后跳转的地址，如果设置了此属性，则登录成功后总是会跳到这个地址上。
     * 只在 signInResponseType 为REDIRECT时生效
     */
    private String signInSuccessUrl;
    /**
     * 退出成功时跳转的url，如果配置了，则总是跳到指定的url，如果没配置，则跳到主页
     */
    private String signOutSuccessUrl;

    public String getSignInPage() {
        return signInPage;
    }

    public void setSignInPage(String loginPage) {
        this.signInPage = loginPage;
    }

    public Duration getRememberMeSeconds() {
        return rememberMeSeconds;
    }

    public void setRememberMeSeconds(Duration rememberMeSeconds) {
        this.rememberMeSeconds = rememberMeSeconds;
    }

    public String getSignUpUrl() {
        return signUpUrl;
    }

    public void setSignUpUrl(String signUpUrl) {
        this.signUpUrl = signUpUrl;
    }

    public SessionProperties getSession() {
        return session;
    }

    public void setSession(SessionProperties session) {
        this.session = session;
    }

    public String getSignOutSuccessUrl() {
        return signOutSuccessUrl;
    }

    public void setSignOutSuccessUrl(String signOutSuccessUrl) {
        this.signOutSuccessUrl = signOutSuccessUrl;
    }

    public String getSignInSuccessUrl() {
        return signInSuccessUrl;
    }

    public void setSignInSuccessUrl(String signInSuccessUrl) {
        this.signInSuccessUrl = signInSuccessUrl;
    }

    /**
     * session 相关配置
     */
    public static class SessionProperties {

        /**
         * 同一个用户在系统中的最大session数，默认1
         */
        private int maximumSessions = 1;
        /**
         * 达到最大session时是否阻止新的登录请求，默认为false，不阻止，新的登录会将老的登录失效掉
         */
        private boolean maxSessionsPreventsLogin = false;
        /**
         * session失效时跳转的地址，为空则跳转到登录页面
         */
        private String sessionInvalidUrl;

        public int getMaximumSessions() {
            return maximumSessions;
        }

        public void setMaximumSessions(int maximumSessions) {
            this.maximumSessions = maximumSessions;
        }

        public boolean isMaxSessionsPreventsLogin() {
            return maxSessionsPreventsLogin;
        }

        public void setMaxSessionsPreventsLogin(boolean maxSessionsPreventsLogin) {
            this.maxSessionsPreventsLogin = maxSessionsPreventsLogin;
        }

        public String getSessionInvalidUrl() {
            return sessionInvalidUrl;
        }

        public void setSessionInvalidUrl(String sessionInvalidUrl) {
            this.sessionInvalidUrl = sessionInvalidUrl;
        }

    }
}
