# shoulder-cluster

该包提供集群部署的能力，如将框架中原本存储在本地的全局变量放于外部存储中，默认支持 redis

应用隔离级别定义：
- 实例专属，即使是同一个应用，但也只能本实例可见。如 JDK 的数据结构类(List、Map...)，Java 默认。
- 主机专属，只有在同一主机运行的应用才可见，如依赖注册表、指定文件等。
- 应用专属，同一个应用不同实例均可见，如订单应用1可以看到订单应用2的内容，但库存应用不可以看到订单应用中的内容。常用于框架中，框架特别支持。
- 全局可见，不限制可见性，如库存应用在redis放了一个值，订单应用可以看到，中间件默认。

默认的隔离级别为应用专属

## 服务无状态

应将有状态的数据迁移到外部存储，如 `Redis`、`数据库`、`消息队列` 等

为了降低从有状态应用迁移到无状态服务的难度，在有状态开发阶段就应该使用同一的缓存接口或管理器，支持无状态时，只需要把存储的实现由内存改为外部存储即可，甚至只需改一个配置项。

- 使用`统一的缓存接口` 或 `带 SPI 机制的缓存入口`
- 开始使用内存缓存，验证前，先更换为本地带序列化的存储（放入缓存前序列化，拿出时反序列化），保证代码的正确性
- 如果上述条件满足，则只需扩展缓存的实现


## 分布式锁

- 基于 DB
    - 高可靠、强一致性、性能差
    - 适合强一致性场景
    
- 基于 `zookeeper`、`etcd`
    - 高可靠、强一致性、性能中等、引入新的依赖
    - 适用于技术选形中已经存在 `zookeeper`/`etcd` 的场景
    
- 基于 `redis`
    - 高性能、非强一致性、单节点时强一致性
    - 适用于一般业务、或特定幂等业务场景
    
    
提前定义全局锁接口，加全局锁时使用该接口，默认实现为JDK实现，支持集群时，只需替代为外部锁即可。


## 消息通知

在对接时就定义清楚，是 public 还是点对点，public 要不要加分组消费

生产者发生消息时，保证消息的顺序性，并为消息附加唯一标识

处理消息时备注是否幂等，考虑顺序性因素


### 消息不丢失
- 生产者：使用 `confirm` 机制确保消息发送（避免使用事务）
- MQ：使用多副本、镜像机制保障高可用，写入所有副本/镜像才认为消息收到。（同步刷盘慢很多）
- 消费者：取消自动 ack 机制，手动 ack，处理完再 ack。



## 数据一致性

缓存数据一致

写数据库、删缓存

特定场景：读业务永远不读数据库、由定时/异步更新来更新缓存

canal 等数据同步工具，读数据库日志，删缓存


## Redis 集群客户端重连

Jedis 自动支持

当 spring boot 版本低于 2.3 时，需要手动开启 Lettuce 刷新集群拓扑图

* [集群说明](https://github.com/lettuce-io/lettuce-core/wiki/Redis-Cluster#Refreshing%20the%20cluster%20topology%20view)
* [集群配置项](https://github.com/lettuce-io/lettuce-core/wiki/Client-options#Cluster-specific%20options)

## redis Session 报错

spring-data-session 在启动时会向 redis 中执行 config 命令，若是云厂商提供的redis，这一命令通常是禁止的，需要在控制台开启，且程序中注入取消操作的 Bean。

```java

@Configuration
public class RedisSessionInitOpConfig {
	
	@Bean
	public ConfigureRedisAction configureRedisAction() {
		return ConfigureRedisAction.NO_OP;
	}
	
}
```
 


- [redis分布式锁和lua脚本](https://www.cnblogs.com/number7/p/8320259.html)
- [redis 可视化工具-Redily官网](https://www.electronjs.org/releases/stable) [github 发布地址](https://github.com/electron/electron/releases)
- [redis 可视化工具-RedisManager]()
