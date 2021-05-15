package org.shoulder.validate.support.model;

import javax.validation.groups.Default;

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
     * 校验分组
     */
    private Class<?>[] groups;

    public ValidConstraint(Class<?> target) {
        this.target = target;
        groups = new Class<?>[]{Default.class};
    }

    public ValidConstraint(Class<?> target, Class<?>[] groups) {
        this.target = target;
        this.groups = groups;
    }

    public Class<?> getTarget() {
        return target;
    }

    public Class<?>[] getGroups() {
        if (groups == null) {
            groups = new Class[0];
        }
        if (groups.length == 0) {
            groups = new Class[]{Default.class};
        }
        return groups;
    }
}
