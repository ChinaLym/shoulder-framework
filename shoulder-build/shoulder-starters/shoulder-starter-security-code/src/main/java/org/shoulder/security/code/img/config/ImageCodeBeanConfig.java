package org.shoulder.security.code.img.config;

import org.shoulder.autoconfigure.security.code.ValidateCodeBeanConfig;
import org.shoulder.code.store.ValidateCodeStore;
import org.shoulder.security.code.img.ImageCodeGenerator;
import org.shoulder.security.code.img.ImageCodeProcessor;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 图片验证码自动配置
 *
 * @author lym
 */
@Configuration(
    proxyBeanMethods = false
)
@AutoConfigureBefore(ValidateCodeBeanConfig.class)
@EnableConfigurationProperties(ImageCodeProperties.class)
public class ImageCodeBeanConfig {

    @Bean
    @ConditionalOnMissingBean(ImageCodeGenerator.class)
    public ImageCodeGenerator imageCodeGenerator(ImageCodeProperties imageCodeProperties) {
        return new ImageCodeGenerator(imageCodeProperties);
    }


    @Bean
    @ConditionalOnMissingBean(ImageCodeProcessor.class)
    public ImageCodeProcessor imageCodeProcessor(ImageCodeProperties imageCodeProperties,
                                                 ImageCodeGenerator imageCodeGenerator,
                                                 ValidateCodeStore validateCodeStore) {

        return new ImageCodeProcessor(imageCodeProperties, imageCodeGenerator, validateCodeStore);

    }

}
