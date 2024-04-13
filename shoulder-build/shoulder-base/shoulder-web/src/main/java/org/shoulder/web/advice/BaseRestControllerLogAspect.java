package org.shoulder.web.advice;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.shoulder.core.log.AppLoggers;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;
import org.shoulder.core.util.ServletUtil;

import java.lang.reflect.Method;

/**
 * 生产环境接口入参默认，以 Json 形式记录接口出入参数
 * 记录日志通常用 filter 方式记录，在参数序列化、校验之前记录（因为filter的内容为实际内容，而其他地方很可能被二次修改）
 * - 但需要注意在使用 filter 记录时，要将流拷贝才能使得可以重复读取
 *
 * @author lym
 */
@Aspect
public abstract class BaseRestControllerLogAspect {

    protected final boolean useControllerLogger;

    public BaseRestControllerLogAspect(boolean useControllerLogger) {
        this.useControllerLogger = useControllerLogger;
    }

    /**
     * 要记录日志的位置：Controller 和 RestController
     * within 不支持继承，不能增强带有某个特定注解的子类的方法
     * 其中 @target 可以，但 spring boot 中 StandardEngine[Tomcat].StandardHost[localhost].TomcatEmbeddedContext[] failed to start
     */
    @SuppressWarnings("unused")
    @Pointcut("@within(org.springframework.web.bind.annotation.RestController)")
    // +" || @within(org.springframework.stereotype.Controller) && @annotation(org.springframework.web.bind.annotation.ResponseBody)")
    public void httpApiMethod() {
    }

    /**
     * 记录出入参
     *
     * @param jp 日志记录切点
     */
    @Around("httpApiMethod()")
    public Object around(ProceedingJoinPoint jp) throws Throwable {
        if (!ServletUtil.inServletContext()) {
            // 非HTTP请求，比如Controller实现了不带 RequestMapping 的public方法、监听AppContextEventListener、或者手动调用带了 @RequestMapping 方法等，
            // 总之不在HTTP上下文，直接执行，跳过切面
            return jp.proceed();
        }
        MethodSignature methodSignature = (MethodSignature) jp.getSignature();
        Method method = methodSignature.getMethod();
        // 根据配置项选择 logger
        Logger log = useControllerLogger ? LoggerFactory.getLogger(method.getDeclaringClass()) : AppLoggers.APP_SERVICE;
        if (!log.isDebugEnabled()) {
            // 直接执行什么都不做
            return jp.proceed();
        }
        long start = System.currentTimeMillis();
        // 前置
        before(jp, log);

        // 执行目标方法
        Object returnObject = null;
        try {
            returnObject = jp.proceed();
            return returnObject;
        } catch (Exception ignore) {
            //AppContext.setError("xxx");
            throw ignore;
        }finally {
            long cost = System.currentTimeMillis() - start;
            // 全局异常处理器会记录详细异常，这里不需要详细记录异常相关
            after(jp, log, returnObject, cost);
        }

    }

    /**
     * 前置
     *
     * @param jp  连接点
     * @param log logger
     */
    protected abstract void before(JoinPoint jp, Logger log);

    /**
     * 后置置
     *
     * @param jp           连接点
     * @param log          logger
     * @param returnObject 返回值
     * @param cost
     */
    protected abstract void after(ProceedingJoinPoint jp, Logger log, Object returnObject, long cost);

}
