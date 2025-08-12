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
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 自动包装所有线程池，实现自动增强
 *
 * @author lym
 * @see org.springframework.cloud.sleuth.instrument.async.ExecutorInstrumentor 参考了该类的设计
 */
public class EnhanceableExecutorBeanPostProcessor implements BeanPostProcessor {

    private static final Logger log = ShoulderLoggers.SHOULDER_CONFIG;

    private static final Map<Executor, Executor> CACHE = new ConcurrentHashMap<>();

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

        if (bean instanceof ThreadPoolTaskExecutor) {
            // Spring 线程池
            return wrapThreadPoolTaskExecutor(bean);
        } else if (bean instanceof ScheduledExecutorService) {
            // JDK 任务调度
            return wrapScheduledExecutorService(bean);
        } else if (bean instanceof ThreadPoolExecutor) {
            // JDK 线程池
            return wrapThreadPoolExecutor(bean);
        } else if (bean instanceof ExecutorService) {
            // JDK 线程池接口：ExecutorService
            return wrapExecutorService(bean);
        } else if (bean instanceof AsyncTaskExecutor) {
            // Spring 任务调度器 ThreadPoolTaskScheduler、AsyncTaskExecutor
            return wrapAsyncTaskExecutor(bean);
        } else {
            // jdk 的执行器
            return wrapExecutor(bean);
        }
    }

    private Object wrapThreadPoolTaskExecutor(Object bean) {
        ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) bean;
        boolean classFinal = Modifier.isFinal(bean.getClass().getModifiers());
        boolean methodsFinal = anyFinalMethods(executor);
        boolean cglibProxy = !classFinal && !methodsFinal;
        return createThreadPoolTaskExecutorProxy(bean, cglibProxy, executor);
    }

    private Object wrapScheduledExecutorService(Object bean) {
        ScheduledExecutorService executor = (ScheduledExecutorService) bean;
        boolean classFinal = Modifier.isFinal(bean.getClass().getModifiers());
        boolean methodFinal = anyFinalMethods(executor);
        boolean cglibProxy = !classFinal && !methodFinal;
        return createScheduledExecutorServiceProxy(bean, cglibProxy, executor);
    }

//    private Object wrapTaskScheduler(Object bean) {
//        boolean classFinal = Modifier.isFinal(bean.getClass().getModifiers());
//        boolean cglibProxy = !classFinal;
//        TaskScheduler executor = (TaskScheduler) bean;
//        return createAsyncTaskExecutorProxy(bean, cglibProxy, executor);
//    }


    // =========================== 包装 =====================================

    private Object wrapExecutor(Object bean) {
        boolean methodFinal = anyFinalMethods((Executor) bean);
        boolean classFinal = Modifier.isFinal(bean.getClass().getModifiers());
        boolean cglibProxy = !methodFinal && !classFinal;
        Executor executor = (Executor) bean;
        try {
            return createProxy(bean, cglibProxy, new ExecutorMethodInterceptor<>(executor));
        } catch (AopConfigException ex) {
            if (cglibProxy) {
                if (log.isDebugEnabled()) {
                    log.debug(
                            "Exception occurred while trying to create a proxy, falling back to JDK proxy", ex);
                }
                // 由于接收器是 Executor（接口），故直接使用 JDK 代理即可
                return createProxy(bean, false, new ExecutorMethodInterceptor<>(executor));
            }
            throw ex;
        }
    }


    private Object wrapAsyncTaskExecutor(Object bean) {
        AsyncTaskExecutor executor = (AsyncTaskExecutor) bean;
        boolean classFinal = Modifier.isFinal(bean.getClass().getModifiers());
        boolean methodsFinal = anyFinalMethods(executor);
        boolean cglibProxy = !classFinal && !methodsFinal;
        return createAsyncTaskExecutorProxy(bean, cglibProxy, executor);
    }

    private Object wrapThreadPoolExecutor(Object bean) {
        // cglib 、JDK 都不支持这个类，直接用装饰器
        boolean classFinal = Modifier.isFinal(bean.getClass().getModifiers());
        boolean methodsFinal = anyFinalMethods(bean);
        boolean cglibProxy = !classFinal && !methodsFinal;;
        ThreadPoolExecutor executor = (ThreadPoolExecutor) bean;
        return createThreadPoolExecutorProxy(bean, cglibProxy, executor);
    }

    private Object wrapExecutorService(Object bean) {
        ExecutorService executor = (ExecutorService) bean;
        boolean classFinal = Modifier.isFinal(bean.getClass().getModifiers());
        boolean methodFinal = anyFinalMethods(executor);
        boolean cglibProxy = !classFinal && !methodFinal;
        return createExecutorServiceProxy(bean, cglibProxy, executor);
    }

    // =========================== 包装实现（创建代理） =====================================

    Object createThreadPoolTaskExecutorProxy(Object bean, boolean cglibProxy, ThreadPoolTaskExecutor executor) {
        if (!cglibProxy) {
            return EnhanceableThreadPoolTaskExecutor.wrap(executor);
        }
        return getProxiedObject(bean, true, executor,
                () -> EnhanceableThreadPoolTaskExecutor.wrap(executor));
    }

    Object createScheduledExecutorServiceProxy(Object bean, boolean cglibProxy, ScheduledExecutorService executor) {
        return getProxiedObject(bean, cglibProxy, executor,
                () -> EnhanceableScheduledExecutorService.wrap(executor));
    }

    private Object createAsyncTaskExecutorProxy(Object bean, boolean cglibProxy, AsyncTaskExecutor executor) {
        return getProxiedObject(bean, cglibProxy, executor, () -> {
            if (bean instanceof ThreadPoolTaskScheduler) {
                return EnhanceableThreadPoolTaskScheduler.wrap((ThreadPoolTaskScheduler) executor);
            }
            return EnhanceableAsyncTaskExecutor.wrap(executor);
        });
    }

    private Object createThreadPoolExecutorProxy(Object bean, boolean cglibProxy, ThreadPoolExecutor executor) {
        return getProxiedObject(bean, cglibProxy, executor,
                () -> new EnhanceableThreadPoolExecutor(executor));
    }

    private Object createExecutorServiceProxy(Object bean, boolean cglibProxy, ExecutorService executor) {
        return getProxiedObject(bean, cglibProxy, executor, () -> EnhanceableExecutorService.wrap(executor));
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
            log.debug("Exception occurred while trying to get a proxy for [{}]. Will fallback to a default implementation", bean.getClass(), e);
            try {
                if (bean instanceof ThreadPoolTaskScheduler) {
                    return EnhanceableThreadPoolTaskScheduler.wrap((ThreadPoolTaskScheduler) executor);
                } else if (bean instanceof ScheduledThreadPoolExecutor) {
                    ScheduledThreadPoolExecutor
                            scheduledThreadPoolExecutor = (ScheduledThreadPoolExecutor) executor;

                    return EnhanceableScheduledThreadPoolExecutor.wrap(scheduledThreadPoolExecutor.getCorePoolSize(),
                            scheduledThreadPoolExecutor.getThreadFactory(),
                            scheduledThreadPoolExecutor.getRejectedExecutionHandler(),
                            scheduledThreadPoolExecutor);
                }
            } catch (Exception ex2) {
                if (log.isDebugEnabled()) {
                    log.debug("Fallback for special wrappers failed, will try the tracing representation instead", ex2);
                }
            }
            return supplier.get();
        }
    }

    Executor executorFromCache(Executor executor, Function<Executor, Executor> function) {
        return CACHE.computeIfAbsent(executor, function);
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

    private static <T> boolean anyFinalMethods(T object) {
        try {
            for (Method method : ReflectionUtils.getAllDeclaredMethods(object.getClass())) {
                if (method.getDeclaringClass().equals(Object.class)) {
                    continue;
                }
                Method m = ReflectionUtils.findMethod(object.getClass(), method.getName(), method.getParameterTypes());
                if (m != null && Modifier.isPublic(m.getModifiers()) && Modifier.isFinal(m.getModifiers())) {
                    return true;
                }
            }
        } catch (IllegalAccessError er) {
            if (log.isDebugEnabled()) {
                log.debug("Error occurred while trying to access methods", er);
            }
            return false;
        }
        return false;
    }
}

