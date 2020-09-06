package org.shoulder.core.uuid;

/**
 * 全局唯一标识符 guid 生成器
 *
 * @author lym
 */
public interface GuidGenerator<T> {

    /**
     * 生成一个ID
     *
     * @return guid
     */
    T nextId();

    /**
     * 批量生成 ID
     *
     * @param num 批量生成个数，一般不推荐单次生成个数过多，如不要超过 1000 个
     * @return guid
     */
    T[] nextIds(int num);


}
