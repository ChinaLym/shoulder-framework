package org.shoulder.core.util;

import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.ServletContext;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Spring 上下文工具类，方便获取bean
 * 提供：从 Spring 环境中获取 Bean、配置信息、上下文路径、BeanFactory、应用上下文
 *
 * @author lym
 */
public class ContextUtils {

    /**
     * Spring应用 beanFactory，一般为 ConfigurableBeanFactory
     */
    private static ConfigurableListableBeanFactory beanFactory;

    /**
     * Spring应用上下文 ApplicationContext
     */
    private static ApplicationContext applicationContext;

    /**
     * resource loader
     */
    private static final ResourcePatternResolver RESOURCE_PATTERN_RESOLVER = new PathMatchingResourcePatternResolver();

    private static boolean contextHasRefreshed = false;

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
    @Nonnull
    public static String[] getAliases(String beanName) throws NoSuchBeanDefinitionException {
        return getBeanFactory().getAliases(beanName);
    }

    @Nonnull
    public static <T> Map<String, T> getBeansOfType(Class<T> cls) {
        try {
            return getBeanFactory().getBeansOfType(cls);
        } catch (BeansException | IllegalStateException e) {
            return Collections.emptyMap();
        }
    }

    @Nonnull
    public static <T> Map<String, T> getBeansOfType(Class<T> cls, ServletContext servletContext) {
        if (servletContext == null) {
            throw new IllegalArgumentException("servletContext can not be null!");
        }
        try {
            return Objects.requireNonNull(WebApplicationContextUtils.getWebApplicationContext(servletContext)).getBeansOfType(cls);
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

    @Nonnull
    public static Resource getResource(String location) {
        return applicationContext != null ? applicationContext.getResource(location) : RESOURCE_PATTERN_RESOLVER.getResource(location);
    }

    @Nullable
    public static Environment getEnvironment() {
        return applicationContext != null ? applicationContext.getEnvironment() : null;
    }

    @Nullable
    public static String getProperty(String propertyKey) {
        Environment env = getEnvironment();
        return env != null ? env.getProperty(propertyKey) : null;
    }

    @Nullable
    public static String getProperty(String propertyKey, String defaultValue) {
        String v = getProperty(propertyKey);
        return v != null ? v : defaultValue;
    }

    /**
     * 获取aop代理对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T getAopProxy(T invoker) {
        return (T) AopContext.currentProxy();
    }

    public static void setBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        ContextUtils.beanFactory = beanFactory;
    }

    @Nullable
    public static ConfigurableListableBeanFactory getBeanFactory() throws IllegalStateException {
        return beanFactory;
    }

    public static void setApplicationContext(ApplicationContext applicationContext) {
        ContextUtils.applicationContext = applicationContext;
    }

    @Nullable
    public static ApplicationContext getApplicationContext() throws IllegalStateException {
        return applicationContext;
    }

    public static void setContextRefreshed(boolean refreshed) {
        ContextUtils.contextHasRefreshed = true;
    }

    public static boolean hasContextRefreshed() {
        return ContextUtils.contextHasRefreshed;
    }

}
