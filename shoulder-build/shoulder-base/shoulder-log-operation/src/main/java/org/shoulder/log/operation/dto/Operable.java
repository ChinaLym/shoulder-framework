package org.shoulder.log.operation.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

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

    /**
     * 对应 {@link org.shoulder.log.operation.entity.OperationLogEntity#detailItems}，
     * 批量日志中 易变的部分包括 被操作对象 和 detailItems 且操作详情通常是根据操作对象生成的，为了使用者方便，特集成在这里。
     *
     * @implSpec 但并不是所有时候都能通过可操作对象生成 故默认为不能生成
     *
     * */
    @Override
    @JsonIgnore
    default List<String> getDetailItems(){
        return LOG_DETAIL_IGNORE;
    }

}
