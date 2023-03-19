package org.shoulder.autoconfigure.guid;

import org.shoulder.core.guid.InstanceIdProvider;
import org.shoulder.core.guid.LongGuidGenerator;
import org.shoulder.core.guid.StringGuidGenerator;
import org.shoulder.core.guid.impl.CompressedUUIDGenerator;
import org.shoulder.core.guid.impl.SnowFlakeGenerator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 实例标识分配
 *
 * @author lym
 */
@AutoConfiguration
@AutoConfigureAfter(InstanceIdProviderAutoConfiguration.class)
@EnableConfigurationProperties(GuidProperties.class)
public class GuidAutoConfiguration {

    private final GuidProperties guidProperties;

    public GuidAutoConfiguration(GuidProperties guidProperties) {
        this.guidProperties = guidProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(CompressedUUIDGenerator.class)
    public StringGuidGenerator stringGuidGenerator() {
        return new CompressedUUIDGenerator();
    }


    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(SnowFlakeGenerator.class)
    public LongGuidGenerator longGuidGenerator(InstanceIdProvider instanceIdProvider) {
        return new SnowFlakeGenerator(guidProperties.getTimeEpoch(), 0L, instanceIdProvider.getCurrentInstanceId());
    }


}
