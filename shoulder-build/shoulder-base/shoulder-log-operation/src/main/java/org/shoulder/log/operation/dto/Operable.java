package org.shoulder.log.operation.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 操作日志： 被操作对象
 * 【推荐】被操作对象实现该接口（如：入参、数据库模型）
 * @author lym
 */
public interface Operable extends OperationDetailAble {

    // ============================ 全部可空，如果无对应，返回null即可 ===============================

    /**
     * 被操作对象唯一标识
     * @return 返回业日志操作实体的唯一标识
     */
    @JsonIgnore
    String getObjectId();

    /**
     * 被操作对象名称
     * @return 返回业日志操作实体的名称
     */
    @JsonIgnore
    String getObjectName();

    /**
     * 被操作对象类型多语言key
     * @return 返回业日志操作实体的对象类型
     */
    @JsonIgnore
    default String getObjectType(){
        return null;
    }

}
