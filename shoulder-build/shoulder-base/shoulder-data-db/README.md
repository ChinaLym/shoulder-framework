# shoulder-data
数据库相关支撑
数据库-枚举值转换

## JPA or MyBatis ?

若您对这两个技术都熟悉，建议选型参照以下建议：

优先 JPA
- 越简单、小型项目越选用JPA，如不到十万用户、几千并发
- 搞 DDD 的，底层数据库可能切换的
- 连接查询少的，去 Join、拆微服务做的好的（互联网应用）

优先 Mybatis
- 团队技术限制
- 连表查询较多、复杂查询多、SQL 经常需要专人优化（传统复杂业务应用）


## Let MyBatis Easier

Mybatis 与 JPA 相比最大的缺点就是简单 SQL 还需要自己写，其实可以使用业界已有的方案

* `MyBatis Generator`
    ** 只是根据数据库生产 Java、Mybatis 代码以及常用方法，不做任何封装。
* `MyBatis Plus`
    ** 自己封装了大量的实现，定义了一些扩展点。
* `tkMyBatis`
    ** 优先采用业界已有的，命名与 JPA 贴近，使用方式与 JPA 相近。

## Sequence

基于 DB 的序列生成器，实现参考了美团的 leaf、[滴滴的 tinyId](https://github.com/didi/tinyid) 等同类技术实现。

滴滴 tiny 特点：

功能：有极度轻量的客户端（几乎纯java原生）、token 实现、无监控但有监控的扩展设计，客户端可获取当前运行状态

评价：
- 模型、逻辑清晰，但需要手动初始化 db （代码中无 insert），
- 序列步幅长度、min、max、loadThreshold 等都在数据库中初始化时配置。
- 锁：几乎都在初始化中，初始化后，内存号段用完时，同步锁粒度与同类产品更大。
  - 但考虑到tinyId中有到阈值后异步预加载nextBuffer，故 synchronize中大概率只有内存操作，也就不会对性能造成太大影响。
  - 反而减少了大量的代码判断逻辑和循环，易于维护。
- 滴滴做了很聪明的抉择：但在绝大多数场景下，刚刚提到的内容对于运行时无弊端，反而减少了大量判断逻辑，代码理解更简单可维护性更好。
- 在绝大多数场景，甚至可以不改代码仅需修改配置直接适用（有基本的鉴权）。
- 企业大规模适用时，建议自行开发 id 分配器的申请逻辑（insert代码）。
- 缓存id无过期
- 提供了静态工具类

---
美团 leaf 特点：

功能：提供获取 sequence 的 HTTP 接口、提供轻量状态监控、双 buffer 缓冲

评价：
- demo 级别源码，极度轻量
- 锁：除了初始化有锁，初始化后，内存号段用完时优先空循环等待
  - 与 tinyId 相比锁力度较细，进入同步前优先调用 waitAndSleep（空循环 1w次 or sleep 10ms）+ volatile 代替 synchronized
  - 逻辑更复杂，在用光 && 异步加载未及时完成 && 并发获取id 情况下性能可能更优（线程未切换），也可能会更差（等待10ms），同时 CPU 占用将更高。
  - 不必太多关注这块性能和实现
- 缓存id默认每60s过期一次
---

Shoulder 的 sequence

功能：

评价：
- 与美团 leaf 类似，但有更细粒度的锁，理论并发更好【最追求极限性能】、且判断更完善，定义更清晰，手动指定事物可见性，对不同数据库兼容性更好。
- 判断是否达到阈值，仅需要由异步线程判断，获取id时无须判断
- 额外新增定时打印状态
- 缓存id可定制过期时间
- 支持多数据库
- 代码相对更多，多在：
  - 完善的缓存/模型设计
  - 一套代码通过不同配置可兼容不同数据库（如同时有 mysql / oracle）
  - 完善的逻辑判断（各种极端情况）
  - 完善的报错与日志打印
  - 通过配置可指定表名、字段名
  - 更完备的sequence定义：max、step、initValue等


---

整体：
- 性能上几乎都一样
  - 锁的粒度都是 sequenceName 级别，几乎集中在初始化，初始化后 && 合理的参数配置下均几乎无锁
  - 加载实现上均有 双 buffer 缓冲 + 异步检测是否需要加载
- 使用上几乎一样
  - 几乎都是Generator.nextId()
- id 缓存略有差别
  - tinyId 缓存id不支持过期
  - leaf 默认过期短，修改需要改代码
  - shoulder 默认过期长，支持自定义

差异：
- tinyId 提供了静态工具类，代码更简单
- leaf 提供了非常简单的监控接口
- Shoulder 的其他功能更完善：日志、监控、定制数据库名称（便于符合不同组织定义的数据库命名规范）



#### 其他工具
* ideal 的 easycode 插件
* mybatiscodehelper pro 插件


CREATE TABLE `labels` (
`id` int(11) NOT NULL AUTO_INCREMENT,
`label_id` int(11) NOT NULL,
`gmt_create` datetime NOT NULL,
`gmt_modified` datetime NOT NULL,
`parent_label_id` int(11),
`sub_label_ids` varchar(255) COMMENT '子节点标签，逗号分割',
`label_order` int(11),
`tenant_id` varchar(255),
`label_entry` varchar(255) COMMENT '操作入口，如运营、类目等等',
PRIMARY KEY (`id`),
UNIQUE KEY `uk_label_id` (`label_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

#### 比较

* `MBG`(`MyBatis Generator`) 是简单的生成代码，没有封装，生成后使用的仍然是原生 Mybatis，且无任何依赖。
* `MyBatis Plus` 与 `tkMyBatis` 都在原生 Mybatis 基础上做了自己的封装，不同的是`MyBatis
Plus`自己定义了一套规范，功能比较齐全，近几年社区越来越活跃，依赖较重，使用时需要了解他的设计。`tkMyBatis`更成熟，且优先采用已有成熟开源并集成，依赖较轻。

#### 结论

希望最轻依赖，使用 `MyBatis Generator`、代码生成器、IDE 插件、Maven Plugin 等形式的。

希望较轻依赖，又要有一定封装，推荐使用 `tkMyBatis`

希望较完善的解决，使用 `MyBatis Plus`（可能导致重度依赖）。

希望完善的解决方案：`JPA`

若希望可以高度灵活定制某个 Service，推荐使用生成器方式，而非继承等封装。




---
## Spring 事务

1. 简述
        在声明式的事务处理中，要配置一个切面，其中就用到了propagation，表示打算对这些方法怎么使用事务，是用还是不用，其中propagation有七种配置，REQUIRED、SUPPORTS、MANDATORY、REQUIRES_NEW、NOT_SUPPORTED、NEVER、NESTED。默认是REQUIRED。

2. Spring中七种Propagation类的事务属性详解：

 REQUIRED：支持当前事务，如果当前没有事务，就新建一个事务。这是最常见的选择。

 SUPPORTS：支持当前事务，如果当前没有事务，就以非事务方式执行。

MANDATORY：支持当前事务，如果当前没有事务，就抛出异常。

REQUIRES_NEW：新建事务，如果当前存在事务，把当前事务挂起。

NOT_SUPPORTED：以非事务方式执行操作，如果当前存在事务，就把当前事务挂起。

NEVER：以非事务方式执行，如果当前存在事务，则抛出异常。

NESTED：支持当前事务，如果当前事务存在，则执行一个嵌套事务，如果当前没有事务，就新建一个事务。

### 四大原则

ACID

### 基本算法

#### 2PC

两阶段提交 注意：发出提交，必须提交成功

自己设计表，保证隔离性 无法保证一致性，因为操作多个资源不是一个原子操作

####

todo
monitor
- SequenceDao getAll
- SegmentBufferView all sequenceName and doubleBuffer status.
