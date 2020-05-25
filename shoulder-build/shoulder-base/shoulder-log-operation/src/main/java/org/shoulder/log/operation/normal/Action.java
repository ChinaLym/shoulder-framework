package org.shoulder.log.operation.normal;

/**
 * 操作日志 action 枚举类接口
 * 【若偏好使用注解：不推荐实现该接口，而是用常量类】
 * @author lym
 */
public interface Action {

    /**
     * 操作名称
     * @return 动作标识 例： add
     */
    String getAction();

    /**
     * 操作详情多语言标识
     * @return 例： add
     */
    String getDetailI18n();

}
