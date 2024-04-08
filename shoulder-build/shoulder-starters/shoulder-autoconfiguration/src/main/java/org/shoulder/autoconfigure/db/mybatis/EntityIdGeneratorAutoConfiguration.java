/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2024 All Rights Reserved.
 */
package org.shoulder.autoconfigure.db.mybatis;

import org.shoulder.autoconfigure.db.DatabaseProperties;
import org.shoulder.autoconfigure.db.sequence.ShoulderSequenceAutoConfiguration;
import org.shoulder.autoconfigure.guid.GuidAutoConfiguration;
import org.shoulder.core.guid.LongGuidGenerator;
import org.shoulder.core.guid.StringGuidGenerator;
import org.shoulder.data.sequence.SequenceGenerator;
import org.shoulder.data.uid.CompositeBizIdGenerator;
import org.shoulder.data.uid.ConditionalBizIdGenerator;
import org.shoulder.data.uid.DefaultEntityIdGenerator;
import org.shoulder.data.uid.EntityIdGenerator;
import org.shoulder.data.uid.KeyFieldsBizIdGenerator;
import org.shoulder.data.uid.ReuseIdBizIdGenerator;
import org.shoulder.data.uid.SequenceBizIdGenerator;
import org.shoulder.data.uid.SequenceEntityIdGenerator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;

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


    /**
     * 有 sequence 优先用 sequence
     */
    @Bean
    @ConditionalOnBean(SequenceGenerator.class)
    @ConditionalOnMissingBean(value = EntityIdGenerator.class)
    public EntityIdGenerator sequenceEntityIdGenerator(SequenceGenerator sequenceGenerator) {
        return new SequenceEntityIdGenerator(sequenceGenerator);
    }
    /**
     * 没有 sequence 尝试用 guidGenerator
     */
    @Bean
    @ConditionalOnMissingBean(value = {EntityIdGenerator.class, SequenceGenerator.class})
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
