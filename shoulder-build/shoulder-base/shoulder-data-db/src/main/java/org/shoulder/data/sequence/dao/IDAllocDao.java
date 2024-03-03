package org.shoulder.data.sequence.dao;

import org.shoulder.data.sequence.model.LeafAlloc;

import java.util.List;

/**
 * IDAllocDao 接口定义了与分布式ID生成器（如美团Leaf）的数据库交互所需的方法，用于管理和获取分配器状态。
 *
 * @author lym
 */
public interface IDAllocDao {

    /**
     * 获取所有已注册在系统中的 Leaf 分配器实体，每个实体包含了分配器的 key、最大已分配ID、步长以及更新时间等信息。
     *
     * @return 所有 Leaf 分配器实体的列表
     */
    List<LeafAlloc> getAllLeafAllocs();

    /**
     * 根据指定业务标签（tag）更新其对应分配器的最大已分配ID，并返回更新后的分配器实体。
     * 该方法确保在生成新的ID时，能够安全地递增并获取最新的分配器状态。
     *
     * @param tag 代表特定业务线或场景的唯一标识符
     * @return 更新后对应的 Leaf 分配器实体
     */
    LeafAlloc updateMaxIdAndGetLeafAlloc(String tag);

    /**
     * 使用自定义步长更新指定 LeafAlloc 实体的最大已分配ID，并返回更新后的分配器实体。
     * 此方法允许用户根据需要调整分配器的步长值，以满足特定场景下ID生成的需求。
     *
     * @param leafAlloc 包含自定义步长信息的 Leaf 分配器实体
     * @return 更新后对应的 Leaf 分配器实体
     */
    LeafAlloc updateMaxIdByCustomStepAndGetLeafAlloc(LeafAlloc leafAlloc);

    /**
     * 获取当前系统中所有正在使用的业务标签（tags）列表。
     * 这些标签用于区分不同的业务场景，确保各个业务场景下的ID都是全局唯一的。
     *
     * @return 所有业务标签字符串组成的列表
     */
    List<String> getAllTags();

}
