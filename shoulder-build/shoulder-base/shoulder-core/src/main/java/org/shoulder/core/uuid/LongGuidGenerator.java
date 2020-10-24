package org.shoulder.core.uuid;


import java.util.Map;

/**
 * 返回值类型为 Long 全局唯一标识符 guid 生成器
 * <p>
 * 实现策略举例：snowflake 系列各种优化和改进
 *
 * @author lym
 */
public interface LongGuidGenerator {

    /**
     * 生成一个ID
     *
     * @return guid
     */
    long nextId();

    /**
     * 批量生成 ID
     *
     * @param num 批量生成个数。若实现原子性批量生成方法，注意控制单次生成个数过多（可能引起阻塞其他线程等）
     * @return guid
     */
    default long[] nextIds(int num) {
        if (num <= 0) {
            throw new IllegalArgumentException("num must > 0!");
        }
        long[] results = new long[num];
        for (int i = 0; i < num; i++) {
            results[i] = nextId();
        }
        return results;
    }


    /**
     * 解析 id，把各个部分
     * <p>
     * {
     * "timestamp": "1567733700834(2019-09-06 09:35:00.834)",
     * "sequenceId": "3448",
     * "workerId": "39"
     * }
     *
     * @param guid guid
     * @return 解析结果
     */
    Map<String, String> decode(long guid);

}
