package org.shoulder.validate.support.mateconstraint.impl;

import cn.hutool.core.util.ReflectUtil;
import org.shoulder.validate.support.dto.ConstraintInfoDTO;
import org.shoulder.validate.support.mateconstraint.ConstraintConverter;

import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.util.*;

/**
 * 约束提取基础类
 *
 * @author lym
 */
public abstract class BaseConstraintConverter implements ConstraintConverter {

    /**
     * 支持解析所有注解
     */
    protected static final List<Class<? extends Annotation>> SUPPORT_ALL = new ArrayList<>(0);

    /**
     * 支持的注解
     */
    protected List<Class<? extends Annotation>> supportAnnotations = Collections.emptyList();

    /**
     * 支持的方法
     */
    protected List<String> methods = Collections.singletonList("message");

    /**
     * 支持的类型
     *
     * @param annotationClass 类型
     * @return 是否支持
     */
    @Override
    public boolean support(Class<? extends Annotation> annotationClass) {
        if (getSupportAnnotations() == SUPPORT_ALL) {
            return true;
        }
        return annotationClass != null && getSupportAnnotations().contains(annotationClass);
    }

    /**
     * 转换
     *
     * @param annotation 注解
     * @return 约束信息
     * @throws Exception 异常信息
     */
    @Override
    public ConstraintInfoDTO converter(Annotation annotation) throws Exception {
        Class<? extends Annotation> clazz = annotation.getClass();
        List<String> methods = getAnnotationMethods();
        Map<String, Object> attributes = new HashMap<>(methods.size());
        for (String method : methods) {
            attributes.put(method, ReflectUtil.getMethod(clazz, method).invoke(annotation));
        }
        String type = getType(annotation.annotationType());
        return new ConstraintInfoDTO().setType(type).setAttributes(attributes);
    }

    /**
     * 返回支持的 JSR 注解 类型
     *
     * @return 注解
     */
    protected List<Class<? extends Annotation>> getSupportAnnotations() {
        return supportAnnotations;
    }

    /**
     * 返回需要反射的 JSR 注解的 字段名
     *
     * @return 方法
     */
    @Nonnull
    protected List<String> getAnnotationMethods() {
        return methods;
    }

    /**
     * 这个注解对应什么校验类型
     *
     * @param type 注解类型
     * @return 校验类型名称
     */
    protected abstract String getType(Class<? extends Annotation> type);

}
