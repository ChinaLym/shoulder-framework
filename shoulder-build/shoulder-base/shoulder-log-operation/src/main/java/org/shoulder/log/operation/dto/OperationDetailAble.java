package org.shoulder.log.operation.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
     * 获取操作详情填充参数
     *
     * @return 操作日志 detailItems 字段
     *          返回值格式：框架支持 ['a,b,c'] 也支持 ['a','b','c']，甚至['a,b','c']，这几种填充方式是等效的。
     *          特殊：返回 null，日志框架将忽略该值
     */
    @JsonIgnore
    List<String> getDetailItems();
}
