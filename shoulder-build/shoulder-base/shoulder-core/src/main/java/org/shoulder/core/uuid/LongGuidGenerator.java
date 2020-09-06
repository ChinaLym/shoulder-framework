package org.shoulder.core.uuid;


/**
 * 返回值类型为 Long
 * <p>
 * 实现策略举例：snowflake 系列各种优化和改进
 *
 * @author lym
 */
public interface LongGuidGenerator extends GuidGenerator<Long> {

    /**
     * 生成一个ID
     *
     * @return guid
     */
    @Override
    Long nextId();

    /**
     * 批量生成 ID
     *
     * @param num 批量生成个数，一般不推荐单次生成个数过多，如不要超过 1000 个
     * @return guid
     */
    @Override
    Long[] nextIds(int num);
}
