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

    /**
     * 帮助 应用 记录请求摘要，所以打印在 app 目录下
     */
    protected static final Logger logger = AppLoggers.APP_SERVICE_DIGEST;

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
        Logger log = useControllerLogger ? LoggerFactory.getLogger(method.getDeclaringClass()) : logger;
        if (!log.isDebugEnabled()) {
            // 直接执行什么都不做
            return jp.proceed();
        }
        // 前置
        before(jp, log);

        // 执行目标方法
        Object returnObject = jp.proceed();

        // 异常后则不记录返回值，由全局异常处理器记录
        after(jp, log, returnObject);

        return returnObject;
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
     */
    protected abstract void after(ProceedingJoinPoint jp, Logger log, Object returnObject);

}
