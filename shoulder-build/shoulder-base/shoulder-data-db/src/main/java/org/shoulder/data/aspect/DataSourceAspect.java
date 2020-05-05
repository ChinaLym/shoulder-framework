package org.shoulder.data.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.shoulder.data.annotation.DataSource;
import org.shoulder.data.context.DataSourceContextHolder;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 数据源处理切面
 *
 * @author lym
 */
@Aspect
@Order(1)
@Component
public class DataSourceAspect {

    @Pointcut("@annotation(org.shoulder.data.annotation.DataSource) || @within(org.shoulder.data.annotation.DataSource)")
    public void dataSourcePointCut() {

    }

    @Around("dataSourcePointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        DataSource dataSource = getDataSource(point);

        if (dataSource != null) {
            DataSourceContextHolder.setDataSourceType(dataSource.value().name());
        }

        try {
            return point.proceed();
        } finally {
            DataSourceContextHolder.clean();
        }
    }

    /**
     * 获取需要切换的数据源
     * todo 明确注解加在方法和类的优先级
     */
    public DataSource getDataSource(ProceedingJoinPoint point) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Class<?> targetClass = point.getTarget().getClass();
        DataSource targetDataSource = targetClass.getAnnotation(DataSource.class);
        if (targetDataSource != null) {
            return targetDataSource;
        } else {
            Method method = signature.getMethod();
            return method.getAnnotation(DataSource.class);
        }
    }
}
