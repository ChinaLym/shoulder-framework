/**
 * 参考：
 * <p>baidu uid：https://github.com/baidu/uid-generator
 * <p>美团leaf：https://github.com/Meituan-Dianping/Leaf
 * <p>滴滴 tinyId：https://github.com/didi/tinyid
 * <p>
 *    shoulder 使用了乐观锁，若出现获取报错（并发获取失败）
 *    分析看，一般在刚启动恰好来了一波业务请求【刚启动，未加载】
 *    或者 sequenceCache 过期刚好来一波请求【过期时间太长，业务量少，没用完过期了导致清空】
 *    应对方式：启动后加载（调用一次获取） && 设置更短的过期时间，更小的步长 或 定期调用刷新。
 */
package org.shoulder.data.sequence;
