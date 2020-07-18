package org.shoulder.log.operation.normal;

/**
 * 操作日志 i18nKey 枚举类接口
 * 【若偏好使用注解：不推荐实现该接口，而是用常量类】
 * @author lym
 */
public interface OpLogDetailKey {

    /**
     * 操作详情多语言标识
     * @return 例： add
     */
    String getDetailKey();

    /**
     * 操作标识
     * @return 操作标识
     */
    String getOperation();
}
