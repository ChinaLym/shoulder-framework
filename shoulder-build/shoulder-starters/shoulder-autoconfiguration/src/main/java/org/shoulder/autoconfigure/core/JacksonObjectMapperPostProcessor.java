package org.shoulder.autoconfigure.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.shoulder.core.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.lang.NonNull;

/**
 * 为 JsonUtils 设置，使得即使用户主动设置了 ObjectMapper ，则使用用户设置的格式
 *
 * @author lym
 */
public class JacksonObjectMapperPostProcessor implements BeanPostProcessor {

	private static final Logger log = LoggerFactory.getLogger(JacksonObjectMapperPostProcessor.class);

    /**
     * 初始化前
     */
    @Override
    public Object postProcessBeforeInitialization(@NonNull Object bean, String beanName)
        throws BeansException {
        // do nothing
        return bean;
    }

    /**
     * 覆盖 JsonUtils 中的默认 ObjectMapper
     *
     * @param bean bean
     * @param beanName beanName
     * @return bean
     * @throws BeansException never throw
     */
	@Override
	public Object postProcessAfterInitialization(@NonNull Object bean, String beanName)
			throws BeansException {
	    // 只处理 Executor
        if(bean instanceof ObjectMapper){
            JsonUtils.setJsonMapper((ObjectMapper) bean);
        }
		return bean;
	}

}

