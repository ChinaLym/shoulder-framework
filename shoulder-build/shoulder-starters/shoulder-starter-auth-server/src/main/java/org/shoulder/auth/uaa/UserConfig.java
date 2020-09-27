package org.shoulder.auth.uaa;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * 配置本系统用户，以通过这些用户完成认证
 * PasswordEncoder
 *
 * @author lym
 */
@Configuration(proxyBeanMethods = false)
public class UserConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        // @formatter:off
        http
            .authorizeRequests()
                .mvcMatchers("/.well-known/**")
                .permitAll()
                .anyRequest()
                .authenticated()
            .and()
                .httpBasic()
            .and()
                .csrf()
                // 验证 accessToken 是否正确的地址
                .ignoringRequestMatchers(request -> "/introspect".equals(request.getRequestURI()));
        // @formatter:on
    }

    /*@Bean
    @Override
    public UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager(
            // mock 一个用户service，代替数据库，只存在一条默认用户记录
            User.builder()
                .username("shoulder")
                .password("shoulder")
                .roles("ADMIN")
                .build());
    }*/

}
