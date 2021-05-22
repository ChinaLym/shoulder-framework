package org.shoulder.ext.config.domain;

import java.lang.annotation.*;

/**
 * 加在配置类字段上，必须至少有一个字段 indexKey=true，否则视为编码错误，阻塞启动，最晚在测试阶段发现
 * <p>
 * 潜在的风险：主键字段顺序变更将影响 bizId 计算结果？反射获取后按名称排序，避免字段顺序修改导致问题
 * 名称改变影响 bizId 计算结果？允许设置 name 字段
 *
 * @author lym
 * @see ConfigType
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConfigField {

    /**
     * 字符串数组
     */
    String DESC_STR_ARR = "List<String>";
    String DESC_STR_MAP = "Map<String, String>";


    /**
     * 是否为索引 key 默认 false
     *
     * @return 是否索引key
     */
    boolean indexKey() default false;

    /**
     * 字段名称 默认使用字段名
     *
     * @return 用于作为key
     */
    String name() default "";

    /**
     * 展示名称 默认使用字段名
     *
     * @return 展示名称
     */
    String chineseName() default "";

    /**
     * 默认值，新增数据时，输入框中默认填充
     *
     * @return 默认
     */
    String defaultValue() default "";

    /**
     * 描述信息，用于前端输出修改时提示该文本业务含义，有什么影响，有什么格式限制
     *
     * @return 默认
     */
    String description() default "";

    // 当且仅当 xxx 租户才支持该字段
    //String support

}