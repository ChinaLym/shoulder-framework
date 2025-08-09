package org.shoulder.autoconfigure.core.current.enhancer;

import jakarta.annotation.Nonnull;
import org.aopalliance.aop.Advice;
import org.shoulder.core.concurrent.enhance.*;
import org.shoulder.core.log.ShoulderLoggers;
import org.slf4j.Logger;
import org.springframework.aop.framework.AopConfigException;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.task.AsyncListenableTaskExecutor;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Supplier;

/**
 * 自动包装所有线程池，实现自动增强
 *
 * @author lym
 * @see org.springframework.cloud.sleuth.instrument.async.ExecutorInstrumentor 参考了该类的设计
 */
public class EnhanceableExecutorBeanPostProcessor implements BeanPostProcessor {

    private static final Logger log = ShoulderLoggers.SHOULDER_CONFIG;

    // todo 1.2
//    private final Supplier<List<String>> ignoredBeans = () -> C.ignoredBeans;

    @Override
    public Object postProcessBeforeInitialization(@Nonnull Object bean, @Nonnull String beanName)
            throws BeansException {
        // do nothing
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(@Nonnull Object bean, @Nonnull String beanName)
            throws BeansException {
        boolean unWrappedExecutor = bean instanceof Executor && !(bean instanceof EnhanceableExecutorMark);
        // 只处理 Executor
        if (!unWrappedExecutor) {
            return bean;
        }
        log.info("EnhanceableExecutorSupport: Wrapped Executor " + beanName);
        // spring 的可监听的异步线程池
        if (bean instanceof AsyncListenableTaskExecutor) {
            return wrapAsyncListenableTaskExecutor(bean);
        }
        // spring 的可监听的异步线程池
        else if (bean instanceof AsyncTaskExecutor) {
            return wrapAsyncTaskExecutor(bean);
        }
        // spring 任务执行器 todo 1.2
//        else if (bean instanceof TaskScheduler) {
//            return wrapTaskScheduler(bean);
//        }
        // jdk 的线程池
        else if (bean instanceof ThreadPoolExecutor) {
            return wrapThreadPoolExecutor(bean);
        }
        // jdk 的执行器接口
        else if (bean instanceof ExecutorService) {
            return wrapExecutorService(bean);
        }
        // jdk 的执行器
        else {
            return wrapExecutor(bean);
        }
    }

//    private Object wrapTaskScheduler(Object bean) {
//        boolean classFinal = Modifier.isFinal(bean.getClass().getModifiers());
//        boolean cglibProxy = !classFinal;
//        TaskScheduler executor = (TaskScheduler) bean;
//        return createAsyncTaskExecutorProxy(bean, cglibProxy, executor);
//    }


    // =========================== 包装 =====================================

    private Object wrapExecutor(Object bean) {
        Method execute = ReflectionUtils.findMethod(bean.getClass(), "execute",
                Runnable.class);
        Assert.notNull(execute, () -> "not a executor bean:" + bean.getClass());
        boolean methodFinal = Modifier.isFinal(execute.getModifiers());
        boolean classFinal = Modifier.isFinal(bean.getClass().getModifiers());
        boolean cglibProxy = !methodFinal && !classFinal;
        Executor executor = (Executor) bean;
        try {
            return createProxy(bean, cglibProxy,
                    new ExecutorMethodInterceptor<>(executor));
        } catch (AopConfigException ex) {
            if (cglibProxy) {
                if (log.isDebugEnabled()) {
                    log.debug(
                            "Exception occurred while trying to create a proxy, falling back to JDK proxy",
                            ex);
                }
                return createProxy(bean, false,
                        new ExecutorMethodInterceptor<>(executor));
            }
            throw ex;
        }
    }


    private Object wrapAsyncListenableTaskExecutor(Object bean) {
        boolean classFinal = Modifier.isFinal(bean.getClass().getModifiers());
        boolean cglibProxy = !classFinal;
        AsyncListenableTaskExecutor executor = (AsyncListenableTaskExecutor) bean;
        return createAsyncListenableTaskExecutorProxy(bean, cglibProxy, executor);
    }

    private Object wrapAsyncTaskExecutor(Object bean) {
        boolean classFinal = Modifier.isFinal(bean.getClass().getModifiers());
        boolean cglibProxy = !classFinal;
        AsyncTaskExecutor executor = (AsyncTaskExecutor) bean;
        return createAsyncTaskExecutorProxy(bean, cglibProxy, executor);
    }

    private Object wrapThreadPoolExecutor(Object bean) {
        boolean classFinal = Modifier.isFinal(bean.getClass().getModifiers());
        boolean cglibProxy = !classFinal;
        ThreadPoolExecutor executor = (ThreadPoolExecutor) bean;
        return createThreadPoolExecutorProxy(bean, cglibProxy, executor);
    }

    private Object wrapExecutorService(Object bean) {
        boolean classFinal = Modifier.isFinal(bean.getClass().getModifiers());
        boolean cglibProxy = !classFinal;
        ExecutorService executor = (ExecutorService) bean;
        return createExecutorServiceProxy(bean, cglibProxy, executor);
    }


    // =========================== 包装实现（创建代理） =====================================

    private Object createTaskSchedulerProxy(Object bean, boolean cglibProxy, TaskScheduler executor) {
        /// todo 1.2
        return null;
//        return getProxiedObject(bean, cglibProxy, executor,
//                () -> new EnhanceableTaskScheduler(executor));
    }

    private Object createAsyncListenableTaskExecutorProxy(Object bean, boolean cglibProxy, AsyncListenableTaskExecutor executor) {
        return getProxiedObject(bean, cglibProxy, executor,
                () -> new EnhanceableAsyncListenableTaskExecutor(executor));
    }

    private Object createAsyncTaskExecutorProxy(Object bean, boolean cglibProxy, AsyncTaskExecutor executor) {
        return getProxiedObject(bean, cglibProxy, executor,
                () -> new EnhanceableAsyncTaskExecutor(executor));
    }

    private Object createThreadPoolExecutorProxy(Object bean, boolean cglibProxy, ThreadPoolExecutor executor) {
        return getProxiedObject(bean, cglibProxy, executor,
                () -> new EnhanceableThreadPoolExecutor(executor));
    }

    private Object createExecutorServiceProxy(Object bean, boolean cglibProxy, ExecutorService executor) {
        return getProxiedObject(bean, cglibProxy, executor,
                () -> new EnhanceableExecutorService(executor));
    }

    private Object getProxiedObject(Object bean, boolean cglibProxy, Executor executor, Supplier<Executor> supplier) {
        ProxyFactoryBean factory = new ProxyFactoryBean();
        factory.setProxyTargetClass(cglibProxy);
        factory.addAdvice(
                new ExecutorMethodInterceptor<>(executor) {
                    @SuppressWarnings("unchecked")
                    @Override
                    <T extends Executor> T executor(T executor) {
                        return (T) supplier.get();
                    }
                });
        factory.setTarget(bean);
        try {
            return getObject(factory);
        } catch (Exception e) {
            log.debug("Exception occurred while trying to get a proxy for [{}]. Will fallback to a default implementation", bean.getClass());
            return supplier.get();
        }
    }

    private Object getObject(ProxyFactoryBean factory) {
        return factory.getObject();
    }

    private Object createProxy(Object bean, boolean cglibProxy, Advice advice) {
        ProxyFactoryBean factory = new ProxyFactoryBean();
        factory.setProxyTargetClass(cglibProxy);
        factory.addAdvice(advice);
        factory.setTarget(bean);
        return getObject(factory);
    }

}

