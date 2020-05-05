package org.shoulder.security.authentication;

import org.shoulder.security.SecurityConst;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

/**
 * 用户名、密码认证(表单登录)配置
 * 使用 spring security 默认提供的用户名密码认证
 * 
 * @author lym
 */
public class FormAuthenticationSecurityConfig extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

	protected AuthenticationSuccessHandler authenticationSuccessHandler;

	protected AuthenticationFailureHandler authenticationFailureHandler;

	public FormAuthenticationSecurityConfig(AuthenticationSuccessHandler authenticationSuccessHandler, AuthenticationFailureHandler authenticationFailureHandler) {
		this.authenticationSuccessHandler = authenticationSuccessHandler;
		this.authenticationFailureHandler = authenticationFailureHandler;
	}

	@Override
	public void configure(HttpSecurity http) throws Exception {
		http.formLogin()
			.loginPage(SecurityConst.URL_REQUIRE_AUTHENTICATION)
			.loginProcessingUrl(SecurityConst.URL_AUTHENTICATION_FORM)
			.successHandler(authenticationSuccessHandler)
			.failureHandler(authenticationFailureHandler);
	}
	
}
