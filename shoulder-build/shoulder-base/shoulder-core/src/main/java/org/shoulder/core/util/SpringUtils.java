package org.shoulder.core.util;

import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * spring工具类 方便获取bean
 *
 * @author lym
 */
public final class SpringUtils {
    /**
     * Spring应用 beanFactory，一般为 ConfigurableBeanFactory
     */
    private static ConfigurableListableBeanFactory beanFactory;

    /**
     * Spring应用上下文 ApplicationContext
     */
    private static ApplicationContext applicationContext;

    /**
     * 根据 bean 名称获取对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name) throws BeansException {
        return (T) getBeanFactory().getBean(name);
    }

    /**
     * 获取类型为requiredType的对象
     */
    public static <T> T getBean(Class<T> clz) throws BeansException {
        return (T) getBeanFactory().getBean(clz);
    }

    /**
     * 如果BeanFactory包含一个与所给名称匹配的bean定义，则返回true
     *
     * @param name beanName
     * @return 是否有名称为 beanName 的 Bean
     */
    public static boolean containsBean(String name) {
        return getBeanFactory().containsBean(name);
    }

    /**
     * 判断给定名字注册的bean定义是一个singleton还是一个prototype。
     * 如果与给定名字相应的bean定义没有被找到，将会抛出一个异常（NoSuchBeanDefinitionException）
     */
    public static boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
        return getBeanFactory().isSingleton(name);
    }

    /**
     * 获取名称为 name 的 bean 的类型
     */
    public static Class<?> getType(String name) throws NoSuchBeanDefinitionException {
        return getBeanFactory().getType(name);
    }

    /**
     * 如果给定的bean名字在bean定义中有别名，则返回这些别名
     */
    public static String[] getAliases(String name) throws NoSuchBeanDefinitionException {
        return getBeanFactory().getAliases(name);
    }


    public static <T> Map<String, T> getBeansOfType(Class<T> cls) {
        try {
            return getBeanFactory().getBeansOfType(cls);
        } catch (BeansException | IllegalStateException e) {
            return Collections.emptyMap();
        }
    }

    public static <T> Map<String, T> getBeansOfType(Class<T> cls, ServletContext sc) {
        if (sc == null) {
            throw new IllegalStateException("can not find servlet context.");
        }
        try {
            return Objects.requireNonNull(WebApplicationContextUtils.getWebApplicationContext(sc)).getBeansOfType(cls);
        } catch (BeansException e) {
            // 这里直接返回空
            return Collections.emptyMap();
        }
    }

    /**
     * 根据WebApp的虚拟路径获取文件的绝对路径
     */
    public static String getAbsolutePathInWeb(String path) {
        return Objects.requireNonNull(((WebApplicationContext) applicationContext).getServletContext()).getRealPath(path);
    }

    public static Resource getResource(String location) {
        return applicationContext.getResource(location);
    }

    public static Environment getEnvironment() {
        return applicationContext.getEnvironment();
    }

    public static String getProperty(String propertyKey) {
        return getEnvironment().getProperty(propertyKey);
    }

    public static String getProperty(String propertyKey, String defaultValue) {
        return getEnvironment().getProperty(propertyKey, defaultValue);
    }

    /**
     * 获取aop代理对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T getAopProxy(T invoker) {
        return (T) AopContext.currentProxy();
    }

    public static void setBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        SpringUtils.beanFactory = beanFactory;
    }

    public static ConfigurableListableBeanFactory getBeanFactory() throws IllegalStateException {
        ConfigurableListableBeanFactory tmp = SpringUtils.beanFactory;
        if (tmp == null) {
            throw new IllegalStateException("beanFactory has not set!");
        }
        return tmp;
    }

    public static void setApplicationContext(ApplicationContext applicationContext) {
        SpringUtils.applicationContext = applicationContext;
    }

    public static ApplicationContext getApplicationContext() throws IllegalStateException {
        ApplicationContext tmp = SpringUtils.applicationContext;
        if (tmp == null) {
            throw new IllegalStateException("applicationContext has not set!");
        }
        return tmp;
    }

}
