package org.shoulder.autoconfigure.security.browser;

import org.shoulder.security.SecurityConst;
import org.shoulder.security.SecurityConst.BrowserConsts;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.temporal.ChronoUnit;

/**
 * 浏览器环境配置项
 *
 * @author lym
 */
@ConfigurationProperties(prefix = SecurityConst.BrowserConsts.CONFIG_PREFIX)
public class BrowserProperties {

	/**
	 * session管理配置项
	 */
	private SessionProperties session = new SessionProperties();

	/**
	 * 登录页面，当引发登录行为的url以html结尾时，会跳到这里配置的url上
	 */
	private String signInPage = BrowserConsts.PAGE_URL_SIGN_IN;
	/**
	 * '记住我'功能的有效时间，默认一个月
	 */
	private int rememberMeSeconds = (int) ChronoUnit.MONTHS.getDuration().getSeconds();
	/**
	 * 退出成功时跳转的url，如果配置了，则跳到指定的url，如果没配置，则返回json数据。
	 */
	private String signOutUrl;
	/**
	 * 社交登录，如果需要用户注册，跳转的页面
	 */
	private String signUpUrl = BrowserConsts.PAGE_URL_SIGN_UP;
	/**
	 * 登录响应的方式，默认是json
	 */
	//private LoginResponseType signInResponseType = LoginResponseType.JSON;
	/**
	 * 登录成功后跳转的地址，如果设置了此属性，则登录成功后总是会跳到这个地址上。
	 * 只在signInResponseType为REDIRECT时生效
	 */
	private String singInSuccessUrl;


	public String getSignInPage() {
		return signInPage;
	}

	public void setSignInPage(String loginPage) {
		this.signInPage = loginPage;
	}

	public int getRememberMeSeconds() {
		return rememberMeSeconds;
	}

	public void setRememberMeSeconds(int rememberMeSeconds) {
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

	public String getSignOutUrl() {
		return signOutUrl;
	}

	public void setSignOutUrl(String signOutUrl) {
		this.signOutUrl = signOutUrl;
	}

	public String getSingInSuccessUrl() {
		return singInSuccessUrl;
	}

	public void setSingInSuccessUrl(String singInSuccessUrl) {
		this.singInSuccessUrl = singInSuccessUrl;
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
		 * session失效时跳转的地址
		 */
		private String sessionInvalidUrl = BrowserConsts.PAGE_URL_SESSION_INVALID;

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
