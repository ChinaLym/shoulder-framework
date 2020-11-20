package org.shoulder.autoconfigure.core;

import org.shoulder.core.log.LoggerFactory;
import org.shoulder.core.util.ContextUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Nonnull;

/**
 * 为 ContextUtils 设置值
 *
 * @author lym
 */
@Configuration(proxyBeanMethods = false)
public class ContextUtilsAutoConfiguration implements BeanFactoryAware, BeanFactoryPostProcessor, ApplicationContextAware {

    /**
     * BeanFactoryAware 获取时机可更早，但需要类型强转，在极端情况下可能存在异常
     */
    @Override
    public void setBeanFactory(@Nonnull BeanFactory beanFactory) throws BeansException {
        try {
            ContextUtils.setBeanFactory((ConfigurableListableBeanFactory) beanFactory);
        } catch (ClassCastException e) {
            LoggerFactory.getLogger(getClass()).debug("SpringUtils.setBeanFactory fail when BeanFactoryAware.", e);
        }
    }

    /**
     * BeanFactoryPostProcessor 获取时机稍晚，但类型安全
     */
    @Override
    public void postProcessBeanFactory(@Nonnull ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
        try {
            ContextUtils.getBeanFactory();
        } catch (IllegalStateException e) {
            // BeanFactoryAware 类型转换失败了
            ContextUtils.setBeanFactory(configurableListableBeanFactory);
        }
    }

    /**
     * 设置 Spring 上下文
     */
    @Override
    public void setApplicationContext(@Nonnull ApplicationContext applicationContext) throws BeansException {
        ContextUtils.setApplicationContext(applicationContext);
    }

}
