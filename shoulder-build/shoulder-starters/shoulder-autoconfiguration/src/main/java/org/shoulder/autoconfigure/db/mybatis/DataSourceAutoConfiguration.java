package org.shoulder.autoconfigure.db.mybatis;

import com.p6spy.engine.spy.P6DataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

/**
 * 数据源、事务管理器等建议自行配置，尤其多数据源时
 *
 * @author lym
 */
@ConditionalOnClass(P6DataSource.class)
public class DataSourceAutoConfiguration {

    /**
     * dev 环境自动打印完整 SQL
     * 需要引入 p6spy
     */
    @Primary
    @Bean
    @Profile("dev")
    public DataSource dataSource(DataSource dataSource) {
        return new P6DataSource(dataSource);
    }


}
