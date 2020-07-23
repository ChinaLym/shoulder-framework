package org.shoulder.log.operation.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Collections;
import java.util.List;

/**
 *  可以获取操作详情的对象
 *
 * @author lym
 */
public interface OperationDetailAble {

    /**
     * 获取操作详情填充参数。
     *
     * @return 对应 {@link OperationLogDTO#detailItems} 字段
     */
    @JsonIgnore
    default List<String> getDetailItems(){
        return Collections.emptyList();
    }

    /**
     * 获取操作详情描述。
     *
     * @return 对应 {@link OperationLogDTO#detail} 字段
     */
    @JsonIgnore
    default String getDetail(){
        return null;
    }

}
