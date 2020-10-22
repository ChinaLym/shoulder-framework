package org.shoulder.core.uuid;


/**
 * 返回值类型为 String 的全局唯一标识符 guid 生成器
 * <p>
 * 实现策略举例：直接使用 jdk的UUID；jdkUUID去掉'-'；jdk UUID转62进制；等等...
 *
 * @author lym
 */
public interface StringGuidGenerator {

    /**
     * 生成一个ID
     *
     * @return guid
     */
    String nextId();

    /**
     * 批量生成 ID
     *
     * @param num 批量生成个数，一般不推荐单次生成个数过多，如不要超过 1000 个
     * @return guid
     */
    default String[] nextIds(int num) {
        if (num <= 0) {
            throw new IllegalArgumentException("num must > 0!");
        }
        String[] ids = new String[num];
        for (int i = 0; i < num; i++) {
            ids[i] = nextId();
        }
        return ids;
    }
}
