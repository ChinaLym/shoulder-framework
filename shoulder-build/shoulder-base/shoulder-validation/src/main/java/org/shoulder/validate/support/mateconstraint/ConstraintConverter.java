package org.shoulder.validate.support.mateconstraint;

import org.shoulder.validate.support.dto.ConstraintInfoDTO;

import java.lang.annotation.Annotation;

/**
 * JSR -> ConstraintInfoDTO 注解转换器
 *
 * @author lym
 */
public interface ConstraintConverter {

    /**
     * 支持的类型
     *
     * @param annotationClass 类型
     * @return 是否支持
     */
    boolean support(Class<? extends Annotation> annotationClass);

    /**
     * 转换
     *
     * @param annotation 注解
     * @return 约束信息
     * @throws Exception 异常信息
     */
    ConstraintInfoDTO converter(Annotation annotation) throws Exception;
}
