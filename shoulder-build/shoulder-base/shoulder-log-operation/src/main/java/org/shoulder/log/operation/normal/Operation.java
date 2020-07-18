package org.shoulder.log.operation.normal;

/**
 * 操作日志 操作动作标识枚举类接口
 * 【若偏好使用注解：不推荐实现该接口，而是用常量类】
 * @author lym
 */
public interface Operation {

    /**
     * 操作名称
     * @return 动作标识 例： add
     */
    String getOperation();

    /**
     * 操作详情多语言标识
     * @return 例： add
     */
    String getDetailKey();

}
