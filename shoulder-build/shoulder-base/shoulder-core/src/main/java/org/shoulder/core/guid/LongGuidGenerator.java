package org.shoulder.core.guid;


import java.util.Map;

/**
 * long 类型全局唯一的标识符生成器
 * 实现策略举例：
 * - 类 SnowFlake 系列：快、支持分布式，趋势递增；注意实例标识分配、时钟回拨（手动修改、NTP自动校时）、序列上限
 * - 号段模式：集中生成，不浪费号段，适合不敏感业务的流水号，如日志id。注意横向扩展、高可用
 * - 随机+保留号：如用户id需要随机生成，可以通过分批预生成以及随机排序值、并保留靓号/特殊号（如 8888888）
 * - 敏感业务/订单号：业务线id + 流水号 + crypt(uid) 等个性规则。注意横向扩展、分库分表规则、防扫描，一般不包含全局序列
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
