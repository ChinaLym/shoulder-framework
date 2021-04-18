package org.shoulder.core.log;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * 调用日志拦截器
 *
 * @author lym
 */
public class InvokeLogInterceptor implements MethodInterceptor {

    private final String loggerName;

    public InvokeLogInterceptor(String loggerName) {
        this.loggerName = loggerName;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        Logger logger = LoggerFactory.getLogger(this.loggerName);
        String invokeIndex = method.getDeclaringClass().getSimpleName() + "." + method.getName();
        Object[] args = invocation.getArguments();
        logger.info(invokeIndex, " invoke params:", Arrays.toString(args));
        Object invocationResult = invocation.proceed();
        logger.info(invokeIndex, " invoke result:", invocationResult);
        return invocationResult;
    }

}