package org.shoulder.data.mybatis.config;

import org.shoulder.data.mybatis.interceptor.like.MybatisLikeSqlInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * mybatis/mybatis-plus模糊查询语句特殊字符转义配置
 *
 * @author lym
 */
@Configuration
public class MybatisLikeSqlConfig {

    @Bean
    public MybatisLikeSqlInterceptor mybatisSqlInterceptor() {
        return new MybatisLikeSqlInterceptor();
    }

}