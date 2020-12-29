package org.shoulder.autoconfigure.core.current.enhancer;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.shoulder.core.concurrent.enhance.EnhanceableExecutor;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Executor;

/**
 * Interceptor for executor methods.
 *
 * @param <T> - executor type
 * @author lym
 */
@SuppressWarnings("unchecked")
class ExecutorMethodInterceptor<T extends Executor> implements MethodInterceptor {

    private final T delegate;

    ExecutorMethodInterceptor(T delegate) {
        this.delegate = delegate;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        T executor = executor(this.delegate);
        Method methodOnTracedBean = getMethod(invocation, executor);
        if (methodOnTracedBean != null) {
            try {
                return methodOnTracedBean.invoke(executor, invocation.getArguments());
            } catch (InvocationTargetException ex) {
                // gh-1092: throw the target exception (if present)
                Throwable cause = ex.getCause();
                throw (cause != null) ? cause : ex;
            }
        }
        return invocation.proceed();
    }

    private Method getMethod(MethodInvocation invocation, Object object) {
        Method method = invocation.getMethod();
        return ReflectionUtils.findMethod(object.getClass(), method.getName(),
            method.getParameterTypes());
    }

    <E extends Executor> E executor(E executor) {
        return (E) new EnhanceableExecutor(executor);
    }

}
