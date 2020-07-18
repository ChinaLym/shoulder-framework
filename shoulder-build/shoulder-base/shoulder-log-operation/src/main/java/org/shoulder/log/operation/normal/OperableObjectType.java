package org.shoulder.log.operation.normal;

/**
 * 操作日志 ObjectType 枚举类接口
 * 【若偏好使用注解：不推荐实现该接口，而是用常量类】
 * @author lym
 */
public interface OperableObjectType {

    /**
     * 被操作对象类型
     * @return 例： person、card
     */
    String getObjectType();
}
