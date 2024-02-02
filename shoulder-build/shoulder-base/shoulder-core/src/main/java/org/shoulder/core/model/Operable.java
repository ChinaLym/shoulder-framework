package org.shoulder.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 被操作对象
 * 【推荐】被操作对象实现该接口（如：业务类、数据库模型，只给前端提供接口时的入参）
 *
 * @author lym
 */
public interface Operable {

    // ============================ 全部可空，如果无对应，返回null即可 ===============================

    /**
     * 被操作对象唯一标识
     *
     * @return 返回业日志操作实体的唯一标识
     */
    @JsonIgnore
    String getObjectId();

    /**
     * 被操作对象名称
     *
     * @return 返回业日志操作实体的名称
     */
    @JsonIgnore
    String getObjectName();

    /**
     * 被操作对象类型多语言key
     *
     * @return 返回业日志操作实体的对象类型
     */
    @JsonIgnore
    default String getObjectType() {
        // 这里如果过长，数据库保存时可能报错，强烈建议实现该接口
        return "objectType." + getClass().getName();
    }

}
