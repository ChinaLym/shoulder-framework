package org.shoulder.autoconfigure.web.advice;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;
import org.shoulder.core.util.JsonUtils;
import org.shoulder.core.util.ServletUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Enumeration;

/**
 * 自动记录接口出入参数和异常
 * 入参出参分开打印，可以通过traceId来确定完整的链路，不在这里记录异常
 * <p>
 * Spring 的 RequestResponseBodyMethodProcessor
 * RequestMappingHandlerMapping
 * RequestResponseBodyMethodProcessor 也会记录 debug 日志
 * 但其记录日志的目的是为了便于排查spring框架的错误，而不是方便使用者查看，shoulder 的 logback.xml 默认屏蔽他们的 debug 日志
 * shoulder 这里的记录 Logger 是取的对应 Controller 的 Logger，且信息更多，如请求头、请求参数
 * <p>
 * todo 0.3 为开发、生产提供不同的打印格式？（生产环境可能需要日志采集、统一日志格式）
 * 允许可选的打印信息，如太多请求头不想全部打印、
 *
 * @author lym
 */
@Aspect
@Configuration(
    proxyBeanMethods = false
)
@ConditionalOnWebApplication
@ConditionalOnProperty(name = "shoulder.web.logHttpRequest", havingValue = "true", matchIfMissing = true)
public class RestControllerLogAspect {

    /**
     * 记录请求消耗时间
     */
    private ThreadLocal<Long> requestTimeLocal = new ThreadLocal<>();

    /**
     * 代码位置
     */
    private ThreadLocal<String> codeLocationLocal = new ThreadLocal<>();

    /**
     * 日志记录器
     */
    private ThreadLocal<Logger> loggerLocal = new ThreadLocal<>();

    /**
     * 换行符
     */
    private static final String NEW_LINE_SEPARATOR = System.getProperty("line.separator");

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
     * 记录入参
     *
     * @param jp 日志记录切点
     */
    @Before("httpApiMethod()")
    public void before(JoinPoint jp) {
        MethodSignature methodSignature = (MethodSignature) jp.getSignature();
        Method method = methodSignature.getMethod();
        Logger log = LoggerFactory.getLogger(method.getDeclaringClass());
        loggerLocal.set(log);
        if (!log.isDebugEnabled()) {
            return;
        }

        String codeLocation;
        try {
            codeLocation = genCodeLocationLink(method);
        } catch (NotFoundException e) {
            codeLocation = jp.getSignature().toShortString();
        }
        codeLocationLocal.set(codeLocation);
        // 记录请求方法、路径，Controller 信息与代码位置
        HttpServletRequest request = ServletUtil.getRequest();
        StringBuilder requestInfo = new StringBuilder(NEW_LINE_SEPARATOR)
            .append("Shoulder API Report: ")
            .append("[")
            .append(request.getMethod()).append("] ")
            .append(request.getRequestURL().toString())
            .append(" —— ")
            .append(codeLocation)
            .append(NEW_LINE_SEPARATOR);

        Parameter[] parameters = method.getParameters();
        String[] parameterNames = methodSignature.getParameterNames();
        Object[] args = jp.getArgs();

        // todo 记录请求头信息
        requestInfo.append("-- Headers --");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            // 是否也记录过长的参数？
            requestInfo
                .append(NEW_LINE_SEPARATOR).append("\t")
                .append(headerName)
                .append(": ")
                .append(headerValue);
        }

        // 记录 Controller 入参
        if (parameters.length > 0) {
            requestInfo.append(NEW_LINE_SEPARATOR).append("-- Params --");
        }
        for (int i = 0; i < parameters.length; i++) {
            Class<?> argType = parameters[i].getType();
            String argName = parameterNames[i];
            String argValue = JsonUtils.toJson(args[i]);

            // 是否也记录过长的参数？
            requestInfo
                .append(NEW_LINE_SEPARATOR).append("\t")
                .append(argType.getSimpleName())
                .append(" ")
                .append(argName)
                .append(": ")
                .append(argValue);
        }

        log.debug(requestInfo.toString());

        requestTimeLocal.set(System.currentTimeMillis());
    }


    /**
     * 记录出参
     *
     * @param joinPoint    切入点
     * @param returnObject 返回值（返回的数据）
     */
    @AfterReturning(value = "httpApiMethod()", returning = "returnObject")
    public void afterReturn(JoinPoint joinPoint, Object returnObject) {
        Logger log = loggerLocal.get();
        String codeLocation = codeLocationLocal.get();
        // 是否存在并发问题：如突然动态改变日志级别？这时获得的 log 为 null 应什么都不做
        if (log == null || !log.isDebugEnabled()) {
            return;
        }
        // 是否处理 ModelAndView
        long cost = System.currentTimeMillis() - requestTimeLocal.get();
        String returnStr = returnObject != null ? JsonUtils.toJson(returnObject) : "null";
        log.debug("{} cost [{}]ms, Result: {}", codeLocation, cost, returnStr);
        cleanLocal();
    }

    /**
     * IDE 控制台日志跳转代码原理：https://www.jetbrains.com/help/idea/setting-log-options.html
     * 输出这种格式则可以识别 <fully-qualified-class-name>.<method-name>(<file-name>:<line-number>)
     * 但测试发现 IDEA 只要 .(xxx.java:行号) 格式即可
     *
     * @param method 方法
     * @return IDE 支持的跳转格式
     * @throws NotFoundException 代码位置找不到
     */
    private String genCodeLocationLink(Method method) throws NotFoundException {
        Class<?> clazz = method.getDeclaringClass();

        CtClass ctClass = ClassPool.getDefault().get(clazz.getName());
        CtMethod ctMethod = ctClass.getDeclaredMethod(method.getName());
        int lineNum = ctMethod.getMethodInfo().getLineNumber(0);

        return clazz.getSimpleName() + "." + method.getName() +
            "(" + getClassFileName(clazz) + ".java:" + lineNum + ")";
    }


    private static String getClassFileName(Class clazz) {
        String classFileName = clazz.getName();
        if (classFileName.contains("$")) {
            int indexOf = classFileName.contains(".") ? classFileName.lastIndexOf(".") + 1 : 0;
            return classFileName.substring(indexOf, classFileName.indexOf("$"));
        } else {
            return clazz.getSimpleName();
        }
    }


    /**
     * 记录发生异常，异常不应该这里记录
     *
     * @param exception 异常
     */
    //@AfterThrowing(value = "httpApiMethod()", throwing = "exception")
    public void exception(Exception exception) {
        long cost = System.currentTimeMillis() - requestTimeLocal.get();
        // 是否在这里记录异常？会否重复记录？
        loggerLocal.get().error("cost [" + cost + "]ms and EXCEPTION.", exception);
        cleanLocal();
    }

    /**
     * 清理线程变量
     */
    private void cleanLocal() {
        requestTimeLocal.remove();
        loggerLocal.remove();
        codeLocationLocal.remove();
    }

}
