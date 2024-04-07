/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2024 All Rights Reserved.
 */
package org.shoulder.autoconfigure.db.mybatis;

import org.shoulder.autoconfigure.db.DatabaseProperties;
import org.shoulder.autoconfigure.db.mybatis.MybatisPlusAutoConfiguration;
import org.shoulder.autoconfigure.db.sequence.ShoulderSequenceAutoConfiguration;
import org.shoulder.autoconfigure.guid.GuidAutoConfiguration;
import org.shoulder.core.guid.LongGuidGenerator;
import org.shoulder.core.guid.StringGuidGenerator;
import org.shoulder.data.sequence.DefaultSequenceGenerator;
import org.shoulder.data.sequence.SequenceGenerator;
import org.shoulder.data.sequence.dao.JdbcSequenceDAO;
import org.shoulder.data.sequence.dao.SequenceDao;
import org.shoulder.data.uid.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;

import javax.sql.DataSource;
import java.util.List;

/**
 * sequence 相关bean
 *
 * @author lym
 */
@AutoConfiguration(after = {ShoulderSequenceAutoConfiguration.class, GuidAutoConfiguration.class})
@ConditionalOnClass(EntityIdGenerator.class)
@EnableConfigurationProperties(DatabaseProperties.class)
public class EntityIdGeneratorAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public EntityIdGenerator uidGenerator(LongGuidGenerator longGuidGenerator,
                                          StringGuidGenerator stringGuidGenerator) {
        // 没 bean 可注入会提前报错，避免 NPE
        return new DefaultEntityIdGenerator(longGuidGenerator, stringGuidGenerator);
    }

    /**
     * 根据 @BizIdSource 注解拼装
     */
    @Bean
    @Order(value = -1000)
    public ConditionalBizIdGenerator bizIdGenerator() {
        return new KeyFieldsBizIdGenerator("#$#");
    }

    /**
     * 序列生成
     */
    @Bean
    @Order(value = 0)
    @ConditionalOnBean(SequenceGenerator.class)
    @ConditionalOnProperty(name = "shoulder.db.generate-biz-id-by-sequence.", havingValue = "true", matchIfMissing = true)
    public ConditionalBizIdGenerator sequenceBizIdGenerator(SequenceGenerator sequenceGenerator) {
        return new SequenceBizIdGenerator(sequenceGenerator);
    }

    @Bean
    @Order(value = Integer.MAX_VALUE)
    public ConditionalBizIdGenerator reuseIdBizIdGenerator() {
        return new ReuseIdBizIdGenerator();
    }

    @Bean
    @Primary
    public CompositeBizIdGenerator compositeBizIdGenerator(List<ConditionalBizIdGenerator> bizIdGeneratorList) {
        return new CompositeBizIdGenerator(bizIdGeneratorList);
    }
}
