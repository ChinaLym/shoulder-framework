package org.shoulder.autoconfigure.security;

import org.shoulder.security.authentication.FormAuthenticationSecurityConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

/**
 * 
 * 认证相关的扩展点配置。配置在这里的bean，业务系统都可以通过声明同类型或同名的bean来覆盖安全
 * 模块默认的配置。
 * 
 * @author lym
 *
 */
@Configuration
public class AuthenticationBeanConfig {

	/**
	 * 默认密码处理器
	 */
	@Bean
	@ConditionalOnMissingBean(PasswordEncoder.class)
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	/**
	 * 提醒实现默认认证器
	 */
	@Bean
	@ConditionalOnMissingBean(UserDetailsService.class)
	public UserDetailsService userDetailsService() {
		throw new RuntimeException("Please implements a UserDetailsService for spring security！");
	}

	/**
	 * 用户名、密码认证(表单登录)配置
	 */
	@Bean
	public FormAuthenticationSecurityConfig formAuthenticationConfig(@Nullable AuthenticationSuccessHandler authenticationSuccessHandler,
																	 @Nullable AuthenticationFailureHandler authenticationFailureHandler) {
		return new FormAuthenticationSecurityConfig(authenticationSuccessHandler, authenticationFailureHandler);
	}

}
