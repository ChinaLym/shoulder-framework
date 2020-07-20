package org.shoulder.log.operation.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.shoulder.log.operation.entity.OperationLogEntity;

import java.util.List;

/**
 *  可以获取操作详情的对象
 *
 * @author lym
 */
public interface OperationDetailAble {

    /** 忽略这个值 */
    List<String> LOG_DETAIL_IGNORE = null;

    /**
     * 获取操作详情填充参数。
     *
     * @return 对应 {@link OperationLogEntity#detailItems} 字段
     */
    @JsonIgnore
    List<String> getDetailItems();

    /**
     * 获取操作详情填充参数。
     *
     * @return 对应 {@link OperationLogEntity#detail} 字段
     */
    @JsonIgnore
    default String getDetail(){
        return null;
    }

}
