package org.shoulder.web.advice;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.shoulder.core.log.Logger;
import org.shoulder.core.util.JsonUtils;
import org.shoulder.core.util.ServletUtil;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 生产环境接口入参默认，以 Json 形式记录接口出入参数
 * 这种记录方式通常用 filter 记录，在参数序列化、校验之前记录，但需要注意要将流拷贝才能使得可以重复读取
 * 入参出参分开打印不好找？可以通过traceId来确定完整的链路，以及日志系统等。
 *
 * @author lym
 */
public class RestControllerJsonLogAspect extends BaseRestControllerLogAspect {

    public RestControllerJsonLogAspect(boolean useControllerLogger) {
        super(useControllerLogger);
    }

    /**
     * 前置方法较长，单独抽出以保证 JIT 优化
     *
     * @param jp  连接点
     * @param log logger
     */
    @Override
    protected void before(JoinPoint jp, Logger log) {
        MethodSignature methodSignature = (MethodSignature) jp.getSignature();
        Method method = methodSignature.getMethod();
        // 记录请求方法、路径，Controller 信息与代码位置
        HttpServletRequest request = ServletUtil.getRequest();
        StringBuilder requestInfo = new StringBuilder(request.getMethod()).append(" ")
            .append(request.getRequestURI());

        Parameter[] parameters = method.getParameters();
        String[] parameterNames = methodSignature.getParameterNames();

        requestInfo.append(" HEADER: ");
        Enumeration<String> headerNames = request.getHeaderNames();
        Map<String, String> headers = new HashMap<>();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            headers.put(headerName, headerValue);
            /*requestInfo
                .append(headerName)
                .append(": ")
                .append(headerValue)
                .append(" ");*/

        }
        requestInfo.append(JsonUtils.toJson(headers));


        Object[] args = jp.getArgs();
        // 记录 Controller 入参
        if (parameters.length > 0) {
            requestInfo.append(" PARAMS: ");
        }
        Map<String, String> argsMap = new HashMap<>(parameters.length);
        for (int i = 0; i < parameters.length; i++) {
            //Class<?> argType = parameters[i].getType();
            String argName = parameterNames[i];
            String argValue = JsonUtils.toJson(args[i]);
            // 过长的参数可能导致缓慢
            argsMap.put(argName, argValue);
            /*requestInfo
                .append(argType.getSimpleName())
                .append(" ")
                .append(argName)
                .append(": ")
                .append(argValue);*/
        }
        requestInfo.append(JsonUtils.toJson(argsMap));

        log.debug(requestInfo.toString());

    }

    @Override
    protected void after(ProceedingJoinPoint jp, Logger log, Object returnObject) {
        String requestUrl = ServletUtil.getRequest().getRequestURI();
        String returnStr = returnObject != null ? JsonUtils.toJson(returnObject) : "null";
        log.debug("{} Result: {}", requestUrl, returnStr);
    }

}
