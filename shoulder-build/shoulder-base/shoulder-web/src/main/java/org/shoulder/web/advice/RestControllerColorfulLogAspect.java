package org.shoulder.web.advice;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.shoulder.core.log.Logger;
import org.shoulder.core.util.ColorString;
import org.shoulder.core.util.ColorStringBuilder;
import org.shoulder.core.util.JsonUtils;
import org.shoulder.core.util.ServletUtil;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Enumeration;

/**
 * 彩色形式记录接口出入参数
 *
 * Spring 的 RequestMappingHandlerMapping RequestResponseBodyMethodProcessor 也会记录 debug 日志
 * 但其记录日志的目的是为了便于排查spring框架的错误，而不是方便使用者查看，shoulder 的 logback.xml 默认屏蔽他们的 debug 日志
 * shoulder 这里的记录 Logger 是取的对应 Controller 的 Logger，且信息更多，如请求方、请求方法、请求路径、请求头、请求参数
 *
 * @author lym
 * @see CommonsRequestLoggingFilter spring 中提供的日志过滤器
 * @see RequestResponseBodyMethodProcessor
 * @see RequestMappingHandlerMapping
 * @see RequestResponseBodyMethodProcessor
 */
@Aspect
public class RestControllerColorfulLogAspect extends BaseRestControllerLogAspect {

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

    public RestControllerColorfulLogAspect(boolean useControllerLogger) {
        super(useControllerLogger);
    }

    /**
     * 记录入参
     *
     * @param jp 日志记录切点
     */
    @Override
    public void before(JoinPoint jp, Logger log) {
        MethodSignature methodSignature = (MethodSignature) jp.getSignature();
        Method method = methodSignature.getMethod();
        if (!log.isDebugEnabled()) {
            return;
        }

        String codeLocation = genCodeLocationLink(method);
        codeLocationLocal.set(codeLocation);
        // 记录请求方法、路径，Controller 信息与代码位置
        HttpServletRequest request = ServletUtil.getRequest();
        ColorStringBuilder requestInfo = new ColorStringBuilder()
            .newLine()
            .cyan("========================================== ")
            .yellow("Shoulder HTTP Report", ColorString.Style.BOLD, true)
            .cyan(" ==========================================")
            .newLine();

        // 请求地址
        requestInfo
            .green("Request   : ")
            .append("[")
            .lBlue(request.getMethod().toUpperCase())
            .append("] ")
            .lBlue(request.getRequestURL().toString())
            .newLine();
        // 处理的 Controller
        requestInfo
            .green("Controller: ")
            .append(codeLocation)
            .newLine();

        Parameter[] parameters = method.getParameters();
        String[] parameterNames = methodSignature.getParameterNames();
        Object[] args = jp.getArgs();

        requestInfo
            .green("From      : ")
            .append(request.getRemoteAddr())
            .newLine();

        requestInfo
            .green("Headers   :");

        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            requestInfo
                .newLine().tab()
                .lGreen(headerName)
                .append(": ")
                .cyan(headerValue);
        }

        // 记录 Controller 入参
        if (parameters.length > 0) {
            requestInfo.newLine()
                .green("Params    :");
        }
        for (int i = 0; i < parameters.length; i++) {
            Class<?> argType = parameters[i].getType();
            String argName = parameterNames[i];
            String argValue = JsonUtils.toJson(args[i]);

            requestInfo
                .newLine().tab()
                .lBlue(argType.getSimpleName())
                .append(" ")
                .cyan(argName)
                .append(": ")
                .lMagenta(argValue);
        }

        requestInfo.newLine().cyan(". . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .");

        log.debug(requestInfo.toString());

        requestTimeLocal.set(System.currentTimeMillis());
    }


    @Override
    public void after(ProceedingJoinPoint jp, Logger log, Object returnObject) {
        String codeLocation = codeLocationLocal.get();
        // 是否处理 ModelAndView
        long cost = System.currentTimeMillis() - requestTimeLocal.get();
        String returnStr = returnObject != null ? JsonUtils.toJson(returnObject) : "null";
        // todo 彩色打印
        ColorStringBuilder requestInfo = new ColorStringBuilder();
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
        final String split = "$";
        if (classFileName.contains(split)) {
            int indexOf = classFileName.contains(".") ? classFileName.lastIndexOf(".") + 1 : 0;
            return classFileName.substring(indexOf, classFileName.indexOf("$"));
        } else {
            return clazz.getSimpleName();
        }
    }

    /**
     * 清理线程变量
     */
    private void cleanLocal() {
        requestTimeLocal.remove();
        codeLocationLocal.remove();
    }

}
