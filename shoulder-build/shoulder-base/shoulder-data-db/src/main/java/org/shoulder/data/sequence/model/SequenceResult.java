package org.shoulder.data.sequence.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 标准 sequence 结果
 *
 * @author lym
 */
@Data

public class SequenceResult implements Serializable {

    @Serial private static final long serialVersionUID = 7267206837567860910L;
    /**
     * 结果
     */
    private String sequenceValue;

    /**
     * 这个sequence从DB的申请日期，往往一批连续sequence该值相同。
     * 该值目前主要用于协助排查错误
     */
    private Date sequenceCreatedDate;

    /**
     * 数据源分库分表标记，方便追踪
     * 该值目前主要用于协助排查错误
     */
    // private String datasource;

}
