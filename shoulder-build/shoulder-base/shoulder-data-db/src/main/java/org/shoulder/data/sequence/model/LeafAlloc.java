package org.shoulder.data.sequence.model;

import lombok.Getter;
import lombok.Setter;

/**
 * 存储分布式ID生成器中分配器状态的核心数据模型。
 *
 * @author lym
 */
@Getter
@Setter
public class LeafAlloc {

    /**
     * 唯一标识，通常对应业务线或特定场景的标签（tag）
     */
    private String key;

    /**
     * 该分配器当前已分配的最大ID值。每次分配新的ID时，maxId会根据步长进行递增
     */
    private long maxId;

    /**
     * 序列分配器每次分配ID时增加的数量，即ID的增长步长。
     */
    private int step;

    /**
     * 最后一次更新的时间戳，用于追踪和管理分配器的生命周期及状态变更历史
     */
    private String updateTime;

}
