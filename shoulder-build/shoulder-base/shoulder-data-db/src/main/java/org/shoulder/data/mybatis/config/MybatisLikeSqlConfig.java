package org.shoulder.data.mybatis.config;

import org.shoulder.data.mybatis.interceptor.like.MybatisLikeSqlInterceptor;
import org.springframework.context.annotation.Bean;

/**
 * mybatis/mybatis-plus模糊查询语句特殊字符转义配置
 *
 * @author lym
 */
//@AutoConfiguration // fixme 该类未测试！！！ 未自动装配
public class MybatisLikeSqlConfig {

    @Bean
    public MybatisLikeSqlInterceptor mybatisSqlInterceptor() {
        return new MybatisLikeSqlInterceptor();
    }

}