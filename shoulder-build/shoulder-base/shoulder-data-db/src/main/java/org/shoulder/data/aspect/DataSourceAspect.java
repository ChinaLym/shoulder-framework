package org.shoulder.data.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.shoulder.core.util.StringUtils;
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

    /**
     * annotation 加了注解的方法
     * within 加了注解的类内所有方法
     */
    @Pointcut("@annotation(org.shoulder.data.annotation.DataSource) || @within(org.shoulder.data.annotation.DataSource)")
    public void dataSourcePointCut() {

    }

    @Around("dataSourcePointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        String dataSourceBeanName = getDataSourceBeanNam(point);

        boolean enhance = StringUtils.isNotEmpty(dataSourceBeanName);
        if (enhance) {
            DataSourceContextHolder.setDataSourceType(dataSourceBeanName);
        }

        try {
            return point.proceed();
        } finally {
            if (enhance) {
                DataSourceContextHolder.clean();
            }
        }
    }

    /**
     * 获取需要切换的数据源的 beanName
     * 加在方法上优先级大于类上
     *
     * @return dataSource Bean 名称，返回空值则代表不变更
     */
    public String getDataSourceBeanNam(ProceedingJoinPoint point) {
        String dataSourceBeanName = null;
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        if (method != null) {
            DataSource methodDataSource = method.getAnnotation(DataSource.class);
            dataSourceBeanName = getValueFromAnnotation(methodDataSource);
        }
        if (StringUtils.isEmpty(dataSourceBeanName)) {
            Class<?> targetClass = point.getTarget().getClass();
            DataSource classDataSource = targetClass.getAnnotation(DataSource.class);
            dataSourceBeanName = getValueFromAnnotation(classDataSource);
        }
        return dataSourceBeanName;
    }

    private String getValueFromAnnotation(DataSource dataSource) {
        return dataSource == null ? null : dataSource.value();
    }

}
