package org.shoulder.core.uuid;


/**
 * 返回值类型为 String
 * <p>
 * 实现策略举例：直接使用 jdk的UUID；jdkUUID去掉'-'；jdk UUID转62进制；等等...
 *
 * @author lym
 */
public interface StringGuidGenerator extends GuidGenerator<String> {

    /**
     * 生成一个ID
     *
     * @return guid
     */
    @Override
    String nextId();

    /**
     * 批量生成 ID
     *
     * @param num 批量生成个数，一般不推荐单次生成个数过多，如不要超过 1000 个
     * @return guid
     */
    @Override
    String[] nextIds(int num);
}
