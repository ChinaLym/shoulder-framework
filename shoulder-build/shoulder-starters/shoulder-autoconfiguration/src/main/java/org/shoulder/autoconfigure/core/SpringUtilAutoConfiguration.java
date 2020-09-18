package org.shoulder.autoconfigure.core;

import org.shoulder.core.log.LoggerFactory;
import org.shoulder.core.util.SpringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;

/**
 * 为 SpringUtils 设置值
 *
 * @author lym
 */
@Configuration(proxyBeanMethods = false)
public class SpringUtilAutoConfiguration implements BeanFactoryAware, BeanFactoryPostProcessor, ApplicationContextAware {

    /**
     * BeanFactoryPostProcessor 获取时机稍晚，但类型安全
     */
    @Override
    public void postProcessBeanFactory(@NonNull ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
        try {
            SpringUtils.getBeanFactory();
        } catch (IllegalStateException e){
            // BeanFactoryAware 类型转换失败了
            SpringUtils.setBeanFactory(configurableListableBeanFactory);
        }
    }

    /**
     * BeanFactoryAware 获取时机可更早，但需要类型强转，在极端情况下可能存在异常
     */
    @Override
    public void setBeanFactory(@NonNull BeanFactory beanFactory) throws BeansException {
        try {
            SpringUtils.setBeanFactory((ConfigurableListableBeanFactory) beanFactory);
        }catch (ClassCastException e){
            LoggerFactory.getLogger(getClass()).debug("SpringUtils.setBeanFactory fail when BeanFactoryAware.", e);
        }
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        SpringUtils.setApplicationContext(applicationContext);
    }

}
