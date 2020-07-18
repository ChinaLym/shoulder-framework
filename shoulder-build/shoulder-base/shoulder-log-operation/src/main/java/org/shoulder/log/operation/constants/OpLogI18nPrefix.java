package org.shoulder.log.operation.constants;

/**
 * 统一操作日志多语言key前缀
 * @author lym
 */
public interface OpLogI18nPrefix {

    /**
     * 操作日志多语言通用前缀
     */
    String COMMON = "op.";

    /**
     * 动作标识
     */
    String OPERATION = COMMON + "op.";

    /**
     * 操作详情
     */
    String DETAIL = COMMON + "detail.";

    /**
     * 被操作对象类型
     */
    String OBJECT_TYPE = COMMON + "objType.";
}
