<h1 align="center"><a href="https://github.com/ChinaLym" target="_blank">Shoulder Framework（一个肩膀）</a></h1>

![LOGO](doc/img/logo.jpg)

> 如果说我比别人看得更远些,那是因为我站在了巨人的肩上. ——牛顿

[![AUR](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg)](https://github.com/ChinaLym/Shoulder-Framework)
[![](https://img.shields.io/badge/Author-lym-blue.svg)](https://github.com/ChinaLym)
[![](https://img.shields.io/badge/version-0.1-brightgreen.svg)](https://github.com/ChinaLym/Shoulder-Framework)

![](https://img.shields.io/badge/Spring%20Boot%20Version-2.3.0-blue.svg)
![](https://img.shields.io/badge/Spring%20Cloud%20Version-Hotox.SR5-blue.svg)

[![GitHub stars](https://img.shields.io/github/stars/ChinaLym/Shoulder-Framework.svg?style=social&label=Stars)](https://github.com/ChinaLym/Shoulder-Framework/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/ChinaLym/Shoulder-Framework.svg?style=social&label=Fork)](https://github.com/ChinaLym/Shoulder-Framework/network/members)


- 地址: [github](https://github.com/ChinaLym/Shoulder-Framework)、[gitee](https://gitee.com/ChinaLym/shoulder-framework)

一款 `Java WEB` / `微服务` 开发框架，在 `Spring Boot`、`Spring Cloud` 基础上实现了一些`可扩展`的常用功能。

Shoulder 不求使用最广，而是致力于成为使用体验最好的开发框架，您任何的使用需求、建议、想法都可以留下来与我们沟通，Shoulder 将与您一起思考攻克疑难，助天下的开发者更好更安心得通过技术赋能业务！同时，利用业务疑难解决方案的积累反哺技术发展！

---

### 与 `Spring Boot`

可以把 `Shoulder` 看作为 `Spring Boot` 的一个 `插件`。即在 `Spring Boot` 基础上实现了常用能力的集合，将 **[软件优雅设计与开发最佳实践](https://spec.itlym.cn)** 落地。

- 许多系统内部有一些统一规范，为降低实现该目的难度，`Shoulder` 实现了这项工作中重复的部分，提供了支持扩展、二次开发的能力。
- 非常适合里将 `Shoulder` 作为公司里的基础脚手架，或是在`毕设`、`外包`、等项目中快速获得一些常用功能，加速开发！

### 功能

- 统一配置项
    - 各模块命名统一，支持多级配置
    - 统一系统级配置入口，无需 `session.store.cluster=true crypto.store.cluster=true token.store.cluster=true`
      ，仅需统一指定 `application.cluster=true`
    - 一处配置，处处默认：如字符集、语言、日期格式等
    - 一键支持集群：只需配置 `cluster=true`，自动切换至集群模式，无状态化
    - 允许模块配置优先（优先级：`显式模块级配置 > 显式全局级配置 > 模块默认值 > 环境变量 > 系统默认值 > 未设置`）

- 错误码(契约精神)
    - 错误可通过错误码溯源，提供表意、传递、追踪机制
    - 与日志记录、链路追踪、接口契约、消息传递打通，有全局自动化兜底处理机制

- 日志、异常、
    - 基于业界标准的日志标准（base on `Slf4j`），无兼容问题，最小化配置、开箱即用，优化内部并发机制，并结合shoulder技术栈提供增强能力
    - `日志`、`异常`、`错误码` 打通、改造了 `lombok` 源码，提供简化开发注解，极大减少代码量
    - 统一错误码格式（也支持配置）、规范代码中的错误
    - 提供错误码实践方案，让 `错误码规范` 不在头疼
    - 提供全局`异常`、`错误码`处理


- 操作/审计日志
    - 基于注解的使用：一个注解简单上手
    - 可扩展的输出格式：便于统一日志规范、提供默认推荐格式
    - 可扩展的目标源：无论是打印日志、保存至数据库、发送至Kafka，ELK等
    - 可扩展的记录流程：灵活编排记录步骤、动态填充
    - 日志策略上下文：自定义日志上下文传递方案，借鉴 Spring 的事务传播机制
    - 自动跨线程支持：异步业务场景，也不需要担心如何将一些变量跨线程传递
    - 异步与缓存：支持高并发场景异步与批量记录，提高系统的吞吐量

- 国际化与多语言翻译
    - 丝滑的翻译封装：在 Spring MessageSource 之上，添加了动态获取当前语言的方法
    - 完善的多语言支持：支持多种多语言文件存放规则，除spring/jdk默认的、以及基于语言标识目录名

- 完备的 session 管理
    - 包含存储、分析

- *完善的*加密方案与实现
    - 不像其他第三方 jar ，仅提供只提供 `AES\RSA\ECC\SHA\MD5` 等公开算法的实现。*shoulder* 还在这之上提供了`安全` `可靠` `可生产落地` 的方案与实现，如 `多级密钥管理`
      、`加密算法平滑升级`
    - 安全本地存储加解密：LocalCrypto，仅本应用/服务可以解密
    - 可配置的非对称密钥端点：无论是 `RSA`、`ECC` 还是国产化的 `SM2`，通过配置即刻实现切换；设置为集群时，自动将密钥存储转移至redis
    - 数字摘要算法（哈希算法）、抗抵赖的签名算法
    - 简化使用的统一接口：不仅仅 byte[] ，也可以 String、Input/OutputStream 的加密
    - 大数据并行加解密：充分利用计算资源，提升加解密性能
    - 便于快速上手的门面工具类：只需要知道几个静态方法即可使用
    - 基于 `ECC`（可替换） 的密钥协商实现，提供了完整的密钥协商逻辑并在 spring boot 中自动激活
    - 安全传输方案（基于 `注解` ，无感知密钥协商，极简使用）

- 通用业务代码封装
    - 数据库封装，如枚举与字段转换
    - 基本业务封装，增删改查系列

- 校验框架
    - 基于 `JSR` 规范，实现更多常用校验规则
    - 与异常、错误码、多语言打通，简化开发难度
    - 包含文件、熟悉、DTO

- 安全系列
    - 常见 `WEB` 攻击的防御器：CSRF、XSS、SQL Inject
    - Oauth2 授权
        - 认证服务器、资源服务器等（扩展 `Spring Security` ）
    - 充分灵活的认证框架（扩展 `Spring Security` 迁移自 [learn-spring-security](https://gitee.com/ChinaLym/learn-spring-security)）
    - 灵活可配置的验证码框架：只需一行配置，即可在任意接口增加验证码校验，极大提高代码复用度
    - 快速实现认证中心：`OAuth2`、`JWT Token` 发放、鉴权、认证、JWK端点、自定义的认证方式、支持 `SSO` 单点登录
    - 灵活的认证方式切换：一行配置在 Session、集群 Session、Token、JWT Token

- 依赖管理 & 开箱即用
  - 与 `Spring Boot` 类似，无需再思考引入哪个版本，会不会冲突，`Shoulder` 管理了常用依赖的版本号，如 `spring-boot`、`spring-cloud`、`spring-cloud-alibaba`
  - 自动依赖，当你引入 `shoulder-starter-web` 不需要再引入 `spring-boot-starter-web` 也不需要担心还要依赖什么
  - 最小化配置，提供了默认的配置项，自带建议配置。
  - 配置项支持 `IDE` 的自动提示

- 更多
  - **Java中性能最高**、灵活配置的**分布式、全局递增、唯一标识生成器（单节点持续高压场景为 twitter 雪花算法**200w倍+**、JDK UUID的 **120倍！**、百度开源算法的近 **60 倍！**）。
    巧妙处理时钟回拨，突发峰值、持续高压，并支持配置与扩展。
  - 可扩展的生产级分布式锁：优雅处理持锁宕机、并发加锁、可重入；默认支持数据库（巧妙处理事务可见性）、内存（提供模拟器）、Redis、zookeeper...
  - 提供使用demo、部分单元测试：提供优秀的应用案例，懒人可以直接复制代码即可。
  - 可扩展的延迟任务
  - 灵活的多环境配置
  - 分布式的任务调度
  - 标准可扩展的链路追踪（`Open Tracing`）
  - 与 Spring Boot 天衣无缝的自动集成能力
  - 能力保证可扩展、可监控、可回滚（单独屏蔽下线）
  - 认证、注册、权限管理
  - **等你探索...**
---

# 快速开始

通过简单的 **[示例工程](https://github.com/ChinaLym/shoulder-framework-demo/tree/master/demo1)**（[github](https://github.com/ChinaLym/shoulder-framework-demo/tree/master/demo1)  [gitee](https://gitee.com/ChinaLym/shoulder-framework-demo/tree/master/demo1)），快速感受 `Shoulder` 带来的优雅编码体验。

### 自动创建

`shoulder` 提供了 maven [archetype](https://github.com/ChinaLym/Shoulder-Framework/tree/master/shoulder-archetype-simple) ，可通过该工程快速创建

### 在新的 Maven 项目中使用


可以直接使用以下 `pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <!-- 继承 shoulder 提供的父工程，自动管理版本号，包含了 spring-boot-parent -->
    <parent>
        <groupId>cn.itlym</groupId>
        <artifactId>shoulder-parent</artifactId>
        <version>0.6</version><!-- shoulder-version -->
    </parent>

    <groupId>com.demo</groupId><!-- 你的 groupId -->
    <artifactId>hello-shoulder</artifactId><!-- 你的 artifactId -->
    <version>1.0.0-SNAPSHOT</version><!-- 你的 version -->

    <dependencies>
        <!-- 版本号、web 相关依赖 自动管理。已自动引入对应的 spring-boot-starter-web -->
        <dependency>
            <groupId>cn.itlym</groupId>
            <artifactId>shoulder-starter-web</artifactId>
        </dependency>
    </dependencies>

</project>

```

### 在已有的工程中使用（不继承父类）

已经继承了其他父工程，如（`spring-boot-parent`）且不想修改，只需加入 `Shoulder` 的依赖清单，然后在 `dependency` 中引入想要的模块即可~

```xml
    <!-- shoulder 的依赖管理 -->
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>cn.itlym</groupId>
                <artifactId>shoulder-dependencies</artifactId>
                <version>${shoulder.version}</version><!-- shoulder-version -->
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <dependencies>
        <!--如希望使用 Shoulder 中 web 相关的能力增强，只需引入 web 模块-->
        <dependency>
            <groupId>cn.itlym</groupId>
            <artifactId>shoulder-starter-web</artifactId>
        </dependency>
    </dependencies>
```

---

# 常见问题

### xxx功能的使用/扩展

首选要确定该能力是否是 `Shoulder` 提供的，不然方向错了，就白忙活啦~ （注：可以通过包路径、类路径来判断~）

Shoulder提供的能力可以参见[使用手册]()（TODO wiki）

`Shoulder` 自动引入并管理了 `Spring Boot` 的版本，但未屏蔽或改变 `Spring Boot` 用法，一些功能并不是 Shoulder 提供的，Shoulder仅仅是帮你引入咯~
如 `Spring Boot`、`Spring Cloud` 等优秀第三方库为我们提供了大量能力（致敬），使用这些时可以去看他们官方的一手教程，或到搜索引擎搜索对应关键词。如

- shoulder Web 工程中如何使用添加自己的过滤器、拦截器
    - 过滤器、拦截器等的基本功能是 `Spring Boot` 提供的，应该搜索 `Spring Boot 自定义过滤器` 而不是 ~~`Shoulder 自定义过滤器`~~
- shoulder Web 工程中如何访问静态资源文件
    - web 的基础功能是由`Spring Boot` 提供哒，所以这么搜索会更合适哟~ `Spring Boot 如何访问静态资源文件`

### 使用中遇到问题、报错

当认为是 Shoulder 框架的 Bug 时可以在 `issure` 中描述你遇到的`问题`、`如何复现`、你尝试过的`排查与解决方式`，甚至可行的缺陷修复方案。

欢迎提交代码~ 以下提交都是可以的~

- 优化代码格式、完善注释、标准化`JavaDoc`
- 修复 BUG
- 添加功能

# 工程目录 & 模块划分

> 与 spring、spring-boot 的包模块划分、包命名策略相似（简化使用者学习成本）

最外层的 shoulder-build 管理了 shoulder 的构建，包含了三个部分

- **shoulder-dependencies** 负责依赖版本管理

- **shoulder-parent** 是 shoulder 所有功能模块的直接父类，负责管理他们的公共依赖、插件配置等，**使用者也可以直接继承该模块**。

- **shoulder-build** 包含 shoulder 的代码，其下面又按照用途分为两个模块。

    - **shoulder-base**: 基础定义与功能模块，真正实现功能代码，但使用者一般不会直接引入。
        - **shoulder-xxx**: xxx模块代码（开发时可以把一个模块当作一个工程）。
        - ...

    - **shoulder-starters**: 带 `Spring Boot` 自动配置的开箱即用模块，并提供 `shoulder` 功能的默认实现，简化使用者上手难度。
        - **shoulder-starter-xxx**: xxx模块的自动配置，供使用者直接引入。
        - ...

---

# 设计 & 路线

> **shoulder** 复用/优化当前业界成熟的解决方案（站在巨人的肩膀上），将 `开源社区` 中优秀的实现带给使用者，给使用者一个肩膀！
为减少使用者的上手成本，在用法上借鉴了 `Spring Boot`，使用 `Shoulder` 就像使用 `Spring Boot` 的 `starter` 一样简单（开箱即用）！

- 实现上参照了 **[软件优雅设计与开发最佳实践](https://spec.itlym.cn)** 中提到的内容，但没与它完全绑定，仅将该实践方案作为默认实现，实际中可替换、可二次开发。
- 可制定自己团队/项目的规范，在 **Shoulder** 基础上增加自己个性化部分，即可快速实现自定义规范并获得常用的功能实现。

## 技术选择

喜欢引入新的优秀技术，但不盲目追捧，当且仅当有完整的实践方案

- 业界标准或绝对主流
- 技术先进、可用于生产环境，且容易从原有的主流技术迁移

##  **[软件优雅设计与开发最佳实践](https://spec.itlym.cn)**  是什么

一千开发手里有一千种实践之路，这一千条路中，`平坦的`（开发维护成本低）、`坎坷的`（开发维护成本高） 差别很大，使用业界有成熟的解决方案往往会**事半功倍**！
为保证 `代码性能` ，降低 `维护成本`、`开发成本`。**Shoulder** 还提供了`技术选型指导`和一定的`开发规范`，带你轻松踏上最佳实践之路！[点击这里可以查看 Shoulder Framework 推荐的开发规约](https://doc.itlym.cn)

## Road Map

`Shoulder` 希望做一个整套的可复用的平台（`PaaS`），使用者只需要做做自己的业务即可。整体格局如下

- `Shoulder iPaaS` 基础中间件环境 Shoulder 提供依赖中间件的`Docker`镜像或部署教程（如 数据库、消息队列、服务注册中心、任务调度中心、搜索引擎、报警与监控系统等）。
- `Shoulder Specific` 软件系开发设计注意事项、[落地方案和规范](https://spec.itlym.cn)
- **Shoulder Framework**  即本开源项目，提供共性能力封装，减少代码冗余，降低系统开发维护成本。
- `Shoulder Platform` 共性业务平台，提供 `用户平台`、`支付平台`、`通知中心`、`业务网关`、`数据字典`、`全局ID生产器` 等基础、通用业务能力平台
- `Shoulder Platform SDK` 以 sdk 形式方便业务层对接使用。

## 项目代码地址

| 项目 | 开源地址 | 说明 |
|---|---|---|
| Shoulder Framework | [github](https://github.com/ChinaLym/Shoulder-Framework)、[gitee](https://gitee.com/ChinaLym/shoulder-framework) | 开发框架，在 Spring Boot 基础之上，结合[软件优雅设计与开发最佳实践](https://spec.itlym.cn)，增加常用的功能，任何基于`Spring Boot`/`Spring Cloud`的项目都可以使用。 |
| Shoulder Platform | [github](https://github.com/ChinaLym/Shoulder-Platform)、[gitee](https://gitee.com/ChinaLym/shoulder-platform) | SaaS 开发平台，提供了基础通用能力，与具体业务无关 |
| shoulder-framework-demo | [github](https://github.com/ChinaLym/shoulder-framework-demo)、[gitee](https://gitee.com/ChinaLym/shoulder-framework-demo) | 以简单的例子介绍 `Shoulder Framework` 的使用 |
| shoulder-plugins | [github](https://gitee.com/ChinaLym/shoulder-plugins)、[gitee](https://gitee.com/ChinaLym/shoulder-plugins) | shoulder 提供的的减少开发工作量的`maven`插件（非必须，如遵循[软件优雅设计与开发最佳实践-国际化开发](https://doc.itlym.cn/specs/base/i18n.html)时推荐希望使用自动生成多语言翻译资源文件的插件减少开发工作量） |
| shoulder-lombok | [github](https://github.com/ChinaLym/shoulder-lombok)、[gitee](https://gitee.com/ChinaLym/shoulder-lombok) | 在`lombok`之上，增加 `@SLog` 注解，用于简化[软件优雅设计与开发最佳实践-错误码与日志](https://spec.itlym.cn/specs/base/errorCode.html) -shoulder 实现的日志框架的使用（非必须） |
| shoulder-lombok-idea-plugin | [github](https://github.com/ChinaLym/lombok-intellij-plugin)、[gitee](https://gitee.com/ChinaLym/lombok-intellij-plugin) | 在 `lombok-idea-plugin`之上，在 IDEA 中增加`@SLog`的编码提示，以更好的使用 `shoulder-lombok`（非必须，使用 shoulder-lombok 时推荐） |
| Shoulder iPaaS | [github](https://github.com/ChinaLym/shoulder-ipaas)、[gitee](https://gitee.com/ChinaLym/shoulder-iPaaS) | iPaaS 平台，介绍了常见中间件、监控系统、私有基础平台如何部署 |

## 发行版本号说明

采用 [主版本号.次版本号.修订号](https://semver.org/lang/zh-CN)（[MAJOR.MINOR.PATCH](https://semver.org)） 的形式

## 建议与反馈

感谢小伙伴们的 **[Star](https://gitee.com/ChinaLym/shoulder-framework/star)** / **Fork**，欢迎在 issue 交流，留下你的建议、期待的新功能等~

欢迎 `fork` 并提交合并请求一起改善该框架 [合作开发流程与项目介绍](CONTRIBUTING.MD)

