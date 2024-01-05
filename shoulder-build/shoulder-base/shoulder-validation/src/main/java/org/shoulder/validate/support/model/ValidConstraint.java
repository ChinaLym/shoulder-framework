package org.shoulder.validate.support.model;

import jakarta.validation.groups.Default;

import java.lang.annotation.Annotation;

/**
 * 验证约束
 *
 * @author lym
 */
public class ValidConstraint {

    /**
     * 在哪
     */
    private final Class<?> target;

    /**
     * 所有注解
     */
    private final Annotation[] methodAnnotations;

    /**
     * 校验分组
     */
    private final Class<?>[] groups;

    public ValidConstraint(Class<?> target, Class<?>[] groups, Annotation[] methodAnnotations) {
        this.target = target;
        this.groups = groups == null || groups.length == 0 ? groups : new Class<?>[]{Default.class};
        this.methodAnnotations = methodAnnotations;
    }

    public Class<?> getTarget() {
        return target;
    }

    public Class<?>[] getGroups() {
        return groups;
    }

    public Annotation[] getMethodAnnotations() {
        return methodAnnotations;
    }
}
