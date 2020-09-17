package org.shoulder.web.advice;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
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
import org.springframework.web.filter.CommonsRequestLoggingFilter;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Enumeration;

/**
 * 彩色形式记录接口出入参数
 * <p>
 * todo remoteAddress 允许可选的打印信息，如太多请求头不想全部打印
 * <p>
 * Spring 的 RequestResponseBodyMethodProcessor
 * RequestMappingHandlerMapping
 * RequestResponseBodyMethodProcessor 也会记录 debug 日志
 * 但其记录日志的目的是为了便于排查spring框架的错误，而不是方便使用者查看，shoulder 的 logback.xml 默认屏蔽他们的 debug 日志
 * shoulder 这里的记录 Logger 是取的对应 Controller 的 Logger，且信息更多，如请求头、请求参数
 * <p>
 *
 * @author lym
 * @see CommonsRequestLoggingFilter spring 中提供的日志过滤器
 */
@Aspect
public class RestControllerColorfulLogAspect {

    /**
     * 换行符
     */
    private static final String NEW_LINE_SEPARATOR = System.getProperty("line.separator");
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
            jp.proceed();
        }

        // 前置记录
        before(jp);
        long requestTime = System.currentTimeMillis();

        // 执行目标方法
        Object returnObject = jp.proceed();

        // 异常后则不记录返回值，由全局异常处理器记录
        if (!log.isDebugEnabled()) {
            return returnObject;
        }
        long cost = System.currentTimeMillis() - requestTime;
        String requestUrl = ServletUtil.getRequest().getRequestURI();
        String returnStr = returnObject != null ? JsonUtils.toJson(returnObject) : "null";
        log.debug("{} [cost {}ms], Result: {}", requestUrl, cost, returnStr);
        return returnObject;
    }



    /**
     * 记录入参
     *
     * @param jp 日志记录切点
     */
    public void before(JoinPoint jp) {
        MethodSignature methodSignature = (MethodSignature) jp.getSignature();
        Method method = methodSignature.getMethod();
        Logger log = LoggerFactory.getLogger(method.getDeclaringClass());
        loggerLocal.set(log);
        if (!log.isDebugEnabled()) {
            return;
        }

        String codeLocation = genCodeLocationLink(method);
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

        requestInfo.append("-- Headers --");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
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
     * @param returnObject 返回值（返回的数据）
     */
    public void afterReturn(Object returnObject) {
        Logger log = loggerLocal.get();
        String codeLocation = codeLocationLocal.get();
        // 是否存在并发问题：如突然动态改变日志级别？这时获得的 log 为 null 应什么都不做
        if (log == null || !log.isDebugEnabled()) {
            return;
        }
        // 是否处理 ModelAndView
        long cost = System.currentTimeMillis() - requestTimeLocal.get();
        String returnStr = returnObject != null ? JsonUtils.toJson(returnObject) : "null";
        log.debug(NEW_LINE_SEPARATOR + "{} [cost {}ms], Result: {}", codeLocation, cost, returnStr);
        cleanLocal();
    }

    /**
     * IDE 控制台日志跳转代码原理：https://www.jetbrains.com/help/idea/setting-log-options.html
     * 输出这种格式则可以识别 <fully-qualified-class-name>.<method-name>(<file-name>:<line-number>)
     * 但测试发现 IDEA 只要 .(xxx.java:行号) 格式即可
     *
     * @param method 方法
     * @return IDE 支持的跳转格式
     */
    private String genCodeLocationLink(Method method) {
        Class<?> clazz = method.getDeclaringClass();

        int lineNum;
        try {
            CtClass ctClass = ClassPool.getDefault().get(clazz.getName());
            CtMethod ctMethod = ctClass.getDeclaredMethod(method.getName());
            lineNum = ctMethod.getMethodInfo().getLineNumber(0);
        } catch (NotFoundException e) {
            // 未找到，无法跳到具体行数，使用第一行，以便于跳到目标类
            lineNum = 1;
        }
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
     * 记录发生异常
     * 在这里记录异常会导致和异常处理器重复，故忽略异常情况
     *
     * @param exception 异常
     */
    //@AfterThrowing(value = "httpApiMethod()", throwing = "exception")
    public void exception(Exception exception) {
        long cost = System.currentTimeMillis() - requestTimeLocal.get();
        loggerLocal.get().error("[cost " + cost + "ms] and EXCEPTION.", exception);
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
