package org.shoulder.web.advice;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;
import org.shoulder.core.util.JsonUtils;
import org.shoulder.core.util.ServletUtil;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Enumeration;

/**
 * 生产环境接口入参默认，以 Json 形式记录接口出入参数
 * 这种记录方式通常用 filter 记录，在参数序列化、校验之前记录，但需要注意要将流拷贝才能使得可以重复读取
 * 入参出参分开打印不好找？可以通过traceId来确定完整的链路，以及日志系统等。
 *
 * @author lym
 */
@Aspect
public class BaseRestControllerLogAspect {

    /**
     * 要记录日志的位置：Controller 和 RestController
     * within 不支持继承，不能增强带有某个特定注解的子类的方法
     * 其中 @target 可以，但 spring boot 中 StandardEngine[Tomcat].StandardHost[localhost].TomcatEmbeddedContext[] failed to start
     */
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
        MethodSignature methodSignature = (MethodSignature) jp.getSignature();
        Method method = methodSignature.getMethod();
        Logger log = LoggerFactory.getLogger(method.getDeclaringClass());
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
     *  @param jp 连接点
     * @param log logger
     */
    protected void before(JoinPoint jp, Logger log) {

    }

    protected void after(ProceedingJoinPoint jp, Logger log, Object returnObject) {

    }

}
