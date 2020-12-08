package org.shoulder.log.operation.annotation;


import org.shoulder.log.operation.format.covertor.DefaultOperationLogParamValueConverter;
import org.shoulder.log.operation.format.covertor.OperationLogParamValueConverter;

import java.lang.annotation.*;

/**
 * 操作日志参数注解
 * 将该注解加在带 {@link OperationLog} 的方法上，
 * <p>
 * 如果参数值为 null 则默认不在日志输出该参数，如需输出，参见 OperationLogProperties
 * <p>
 * 若将参数中的大对象标记为记录，且使用默认的参数解析方式（展平，类似json），容易造成解析后的字符串较长
 *
 * @author lym
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLogParam {


    /**
     * 参数是否支持多语言，默认为不支持多语言
     */
    boolean supportI18n() default false;

    /**
     * 参数名称，默认取变量名，
     * 如  public void test(@OperationLogParam String foo)，则 name 默认为 'foo'
     */
    String name() default "";

    // --------------------------------- value 记录转换 --------------------------------

    /*
     * value 默认为参数的 toString() 值
     * 如 public void test(@OperationLogParam User user)，则 value 默认为 user.toString()
     *
     * 如果参数类型为 Iterable 及其子类，或者是 Object[], User[] 这类则以逗号拼接其每一项
     * 如 public void test(@OperationLogParam Set<Object> foo)，则 value 默认为 'foo[0].toString(),...,foo[n].toString()'
     * ---------------------------------------------------------------------------------
     * 除默认外，还提供了两种更强大的值转换方式。
     *      1：value() : 以 SPEL 解析其值，作为 value。
     *      2：converter()： 以使用者自定义的 OperationLogParamValueConverter 作为 value 解析方式，功能更强大。
     */

    /**
     * 以该值，作为 value。支持 SPEL
     * <p>
     * 举例：参数为 user，需要记录是否为 admin 则使用 function(@OperationLogParam(value = "#user.role") User user)
     */
    String value() default "";

    /**
     * 值转换器的实现类
     * <p>
     * 注意！值转换器的实现类需要注入在 spring 容器内。若 value() 有值则以 value 为准。
     */
    Class<? extends OperationLogParamValueConverter> converter() default DefaultOperationLogParamValueConverter.class;

}
