# Road Map 发展规划

> **shoulder** 复用/优化当前业界成熟的解决方案（站在巨人的肩膀上），将 `开源社区` 中优秀的实现带给使用者，给使用者一个肩膀！
> 为减少使用者的上手成本，在用法上借鉴了 `Spring Boot`，使用 `Shoulder` 就像使用 `Spring Boot` 的 `starter` 一样简单（开箱即用）！

- 实现上参照了 **[软件优雅设计与开发最佳实践](https://doc.itlym.cn)** 中提到的内容，但没与它完全绑定，仅将该实践方案作为默认实现，实际中可替换、可二次开发。
- 可制定自己团队/项目的规范，在 **Shoulder** 基础上增加自己个性化部分，即可快速实现自定义规范并获得常用的功能实现。

## 诞生背景

14年spring boot 发布，15年逐渐流行，16年先锋队跟上，后续逐渐普及，2021 年，spring boot 已经成为 web 标配，spring cloud 也逐渐成熟稳定

但 spring boot 是面向所有场景的，实际开发中还有许多需要完善，shoulder 在此之外对 web 开发做了一些简化代码编写，利于工程维护，
赋予使用者监控、灰度、限流、安全、错误码、国际化、开发规范等能力，使得开发者可以更加专注于业务！

但 shoulder 不满足于此，shoulder 热爱技术，追求提供更好的开发/服务治理的解决方案。因为现在服务治理的方式都是`胖SDK`（应用依赖一个 `中间件-client.jar`）实现的，
采用这种方案，一旦中间件SDK升级（除非停止维护，否则必然会有新版本出现），那么所有的应用都需要重新编译打包，部署；
异构语言之间SDK无法复用，需要为各种编程语言提供不同的SDK，极大的消耗人力、财力；在部署了几万个应用的系统中，升级框架的版本，可不是一件容易的事情；
而业界主流甚至99.9% 的公司都是这样做的。虽然大家都这么做，但不是最好的选择，云原生 `ServiceMesh`才是下一代架构方案，也是实现 多开发语言融合、异构接入、`FaaS` 的一大利器。

Google 等互联网顶尖公司已经探索 `ServiceMesh` 许久，虽然当前未全面落地，
但 shoulder 认为，技术发展日新月异，ServiceMesh 终将是主流！

将框架能力下沉到基础设施层（如容器 + sideCar），能够将中间件变更与业务应用开发完全隔离，互不影响，既能提升开发效率，又能轻松迭代，何乐而不为呢？

----

## **[软件优雅设计与开发最佳实践](https://doc.itlym.cn)**  是什么

一千开发手里有一千种实践之路，这一千条路中，`平坦的`（开发维护成本低）、`坎坷的`（开发维护成本高） 差别很大，使用业界有成熟的解决方案往往会**事半功倍**！
为保证 `代码性能` ，降低 `维护成本`、`开发成本`。**Shoulder** 还提供了`技术选型指导`和一定的`开发规范`
，带您轻松踏上最佳实践之路！[点击这里可以查看 Shoulder Framework 推荐的开发规约](https://doc.itlym.cn)


## 展望未来

未来提供基础 docker 镜像，只需要依赖很轻的一层用于对接容器 sideCar 约定API 的 jar，中间件/依赖升级通过镜像方式分发。

包含以下：

- 数据层代理：数据库、缓存、搜索引擎，以及他们的分片、读写分离、跨机房、跨zone、跨地区数据访问
- 接口调用：包含 RPC、REST、HTTP 等、流量染色；消息队列
- 分布式事务、弹性任务调度、api网关、应用分组、应用弹性伸缩、业务单元隔离
- 服务治理：服务注册、动态配置、发现、负载均衡、重试、熔断、限流、降级、故障注入、健康检查、实时监控、日志采集、链路追踪、统一应急接入、压测、同城/异地容灾
----

## 计划
- 生产/压测隔离【DONE】
- redis6已经发布，有客户端缓存相关涉及，lettuce6.x已经支持，但spring boot 中暂未支持，预计 spring boot 2.5+ 支持【DONE】
- 启动速度（CRaC，spring boot 3.2 2023.11才支持）、启动预热 https://docs.spring.io/spring-framework/reference/integration/checkpoint-restore.html
- 默认容器由 alpaquita 替换 alpine，以获得更好的内存管理支持（musl-perf，JDK16后兼容）、支持 Graavl VN
- redis Lettuce 6.3 修复了redis宕机会导致15分钟不可用（客户端无主动探活，问题链接未释放），netty-transport-native-epoll:4.1.65.Final:linux-x86_64，在 EPOLL 可用时；https://xie.infoq.cn/article/8d5ecb7b1bc9c84a35de43674【DONE】
- JDK 21 https://www.oracle.com/java/technologies/downloads/#jdk21-windows 【DONE】
- 可监控的 ThreadPoolTaskScheduler
- 可编辑 restTemplate 调用日志打印内容，如大的 body 不打印

## 后续规划

* ext-ai
  ** 提供AI 工程里额外能力，如意图识别，输出内容管控、会话历史、会话分析等

* ext-config
  ** 提供动态配置管理功能

* ext-moments
  ** 提供社交功能模块

** 动态、文章、评论、点赞、朋友圈、通知等

* 【新功能】
  ** db: mybatis-plus 一些常用配置支持 mybatis-plus.configuration.log-impl，方便查看 sql
  ** batch: 静态模型注解形式使用

* 【新人友好】配置优化（枚举化/默认值）
  ** BatchInitializationSettings
  ** LocalCryptoProperties
  ** DictionaryProperties 默认开
  ** OplogProperties 默认开
  ** security-code（captcha）验证码改为开箱即用，同时判断时参考 spring.web session 配置

---

## 技术选型

喜欢引入新的优秀技术，但不盲目追捧，当且仅当有完整的实践方案

- 业界标准或绝对主流
- 技术先进、可用于生产环境，且容易从原有的主流技术迁移

## 发行版本号说明

采用 [主版本号.次版本号.修订号](https://semver.org/lang/zh-CN)（[MAJOR.MINOR.PATCH](https://semver.org)） 的形式

