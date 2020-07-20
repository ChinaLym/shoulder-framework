# shoulder （肩膀）

![](https://img.shields.io/badge/build-passing-green.svg)
![](https://img.shields.io/badge/modules-27-yellow.svg)
![](https://img.shields.io/badge/license-Apche%202.0-orange.svg)

![](https://img.shields.io/badge/Version-0.0.1--SNAPSHOT-red.svg) 

![](https://img.shields.io/badge/Spring%20Boot%20Version-2.3.0-blue.svg)

![](https://img.shields.io/badge/Spring%20Cloud%20Version-Hotox.SR5-blue.svg)

![LOGO](doc/img/logo.jpg)

如果说我比别人看得更远些,那是因为我站在了巨人的肩上. ——牛顿


## 简介

**Shoulder** 是一款 `Java WEB` / `微服务` 开发框架，在 `Spring Boot`、`Spring Cloud` 基础上实现了一些`可扩展`的常用功能(可以再次开发)。

**Shoulder** 设计上参照了 **[软件设计开发规范](http://spec.itlym.cn)** 为了减少使用者的上手成本，**shoulder** 不去造轮子，而是复用/优化当前业界成熟的解决方案（站在巨人的肩膀上），优秀的开源社区给 shoulder 一个肩膀，shoulder 把优秀的实现带给使用者，给使用者一个肩膀！

Shoulder 和 `Spring Boot` 的关系就像 `maven 插件` 和 `maven` 的关系一样，实现了**[软件设计开发规范](http://spec.itlym.cn)** 要求的部分，并在 `spring boot` 原来的基础上实现了一些常用的能力，以为应用提供统一风格，简化多个服务的大系统之间的设计难度，且这些能力支持扩展、二次开发，代码规范，非常适合公司里拿他作为基础脚手架（`Apache2.0` 商业友好），当然，这些便利的能力也适合外包、毕设等项目的快速开发。。

虽然业界基础框架大都已经有了成熟的解决方案，但一千开发手里有一千种实践之路，一千条路里宽阔和坎坷的可能差的很大。因此开发者在使用时如果不按照一定的实践原则，还是会导致代码性能差、难维护等问题。Shoulder 框架不仅提供了Java Web开发中的常用能力，还提供了技术选型指导、一定的开发规范，以使你开发的软件更迅速和易于维护，带你轻松踏上最佳实践之路！[点击这里可以查看 Shoulder Framework 遵循的规范](http://doc.itlym.cn)

想知道该框架的效果，或希望看到一个基于本框架开发的的运行的系统，可以期待 **Shoulder-Platform**(一款使用了 Shoulder 框架的开源平台，`规划中`)

## 技术选型标准：
- 优先为业界标准（如微服务里的 Spring Cloud、Dubbo）
- 其次为技术先进且已用于互联网厂商的生产环境，且容易从原有的主流技术迁移（如 MySQL -> TiDB）
- 其次绝对主流（如优先采用JSR大于其他框架的定义）

## 第三方库依赖选型:
核心依赖（如spring）版本为生产可用的较新的 Release 版本，其他第三方小型依赖可直接选用最新稳定版。

已经依赖的

- Spring 5
- Spring Boot 2.3
- Spring Cloud H S5

## 发行版本号说明
采用 [主版本号.次版本号.修订号](https://semver.org/lang/zh-CN)（[MAJOR.MINOR.PATCH](https://semver.org)） 的形式

---

## 提供能力
包括但不限于：
- 加解密方案（AES\RSA\ECC\SHA\MD5）与配套工具以及开箱即用的实现。安全传输方案（ECDH）与配套工具以及HTTP的自动实现。
- 日志（审计日志、业务日志、追踪日志）方案、简单使用。
- 异常、错误码。
- 数据库封装。
- 认证、授权、验证码框架。
- 等。。。 

## 工程目录与模块划分

采用与 spring、spring-boot 相似的包模块划分、包命名策略

最外层的 shoulder-build 管理了依赖的 spring-boot 的版本

- **shoulder-dependencies** 是 shoulder 框架的依赖管理包，负责管理依赖的版本

- **shoulder-parent** 是 shoulder 提供的所有jar的直接父类，**shoulder-dependencies**，负责管理所有 **shoulder 模块** 和他们的公共依赖、插件等，**使用者也可以直接继承该模块**。
**shoulder-parent** 中分为 **shoulder-base** 和 **shoulder-starters**。他们内部都包含了不同模块。

    - **shoulder-base**: 基础定义包，里面包含了 shoulder框架 的所有的基础定义包。
        - **shoulder-xxx**: xxx模块代码（开发时可以把一个模块当作一个工程）。作为 starter 的基础，使用者一般不会直接引入。
        - ...
        
    - **shoulder-starters**: 某个模块开箱即用的 jar 包。以 **shoulder-base** 为基础，一般由 **shoulder-xxx** 以及自动配置的代码组成。
        - **shoulder-starter-xxx**: xxx模块的自动配置，供使用者直接引入。
        - ...


---


### 规范和约束

本框架定义一些限制性的使用方式，如统一返回值，统一错误码，统一日志格式，统一消息格式等，是为了更好的服务治理而考虑的。

当一个系统不再是单机运行，而是被拆分为多个微服务独立运行时，如果没有一套规范和约束，在服务治理上就会相当困难，因此做了一些必要的约束


## 建议与反馈

感谢小伙伴们的 **[Star](https://gitee.com/ChinaLym/shoulder-framework/star)** / **Fork**，欢迎在 issue 交流，留下你的建议、期待的新功能等~

欢迎 fork 并提交合并请求一起改善该框架 [合作开发流程与项目介绍](CONTRIBUTING.MD)

