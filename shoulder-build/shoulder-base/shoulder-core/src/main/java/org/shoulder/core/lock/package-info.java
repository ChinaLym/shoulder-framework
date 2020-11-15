/**
 * 未释放-代码未释放：静态代码检查
 * 未释放-持锁宕机：分段加锁
 * 提早释放-业务过久：看门狗续命
 * 意外释放-他人释放：通过令牌操作锁
 * 获取阻塞过久-数据库事务超时：手动提交事务
 * 中间件主从宕机：单节点 / 同步复制中间件，如数据库 / 从节点有才认为加锁成功
 */
@NonNullApi
package org.shoulder.core.lock;

import org.springframework.lang.NonNullApi;
