package org.shoulder.autoconfigure.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nonnull;
import org.shoulder.core.log.ShoulderLoggers;
import org.shoulder.core.util.JsonUtils;
import org.slf4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

/**
 * 为 JsonUtils 设置，使得即使用户主动设置了 ObjectMapper ，则使用用户设置的格式
 *
 * @author lym
 */
@ConditionalOnClass(ObjectMapper.class)
public class JacksonObjectMapperPostProcessor implements BeanPostProcessor {

    private static final Logger log = ShoulderLoggers.SHOULDER_CONFIG;

    public JacksonObjectMapperPostProcessor() {
        // just for debug
    }

    /**
     * 初始化前
     */
    @Override
    public Object postProcessBeforeInitialization(@Nonnull Object bean, @Nonnull String beanName)
            throws BeansException {
        // do nothing
        return bean;
    }

    /**
     * 覆盖 JsonUtils 中的默认 ObjectMapper
     *
     * @param bean     bean
     * @param beanName beanName
     * @return bean
     * @throws BeansException never throw
     */
    @Override
    public Object postProcessAfterInitialization(@Nonnull Object bean, @Nonnull String beanName)
            throws BeansException {
        if (bean instanceof ObjectMapper) {
            // 注入 ObjectMapper 可改变工具类的效果
            JsonUtils.setJsonMapper((ObjectMapper) bean);
        }
        return bean;
    }

}

