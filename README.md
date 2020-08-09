![LOGO](doc/img/logo.jpg)

> 如果说我比别人看得更远些,那是因为我站在了巨人的肩上. ——牛顿

![](https://img.shields.io/badge/build-passing-green.svg)
![](https://img.shields.io/badge/modules-27-yellow.svg)
![](https://img.shields.io/badge/license-Apche%202.0-orange.svg)
![](https://img.shields.io/badge/Version-0.1-blue.svg)

![](https://img.shields.io/badge/Spring%20Boot%20Version-2.3.0-blue.svg)
![](https://img.shields.io/badge/Spring%20Cloud%20Version-Hotox.SR5-blue.svg)

- 地址： [github](https://github.com/ChinaLym/Shoulder-Framework)、[gitee](https://gitee.com/ChinaLym/shoulder-framework)

# shoulder （肩膀）

一款 `Java WEB` / `微服务` 开发框架，在 `Spring Boot`、`Spring Cloud` 基础上实现了一些`可扩展`的常用功能。

### **Shoulder** 与 **Spring Boot**

可简单地把 `Shoulder` 看作为 `Spring Boot` 的一个 `插件`。即在 `Spring Boot` 基础上实现了常用能力的集合，将 **[软件优雅设计与开发最佳实践](http://spec.itlym.cn)** 落地。

- 许多系统内部有一些统一规范，为降低实现该目的难度，`Shoulder` 实现了这项工作中重复的部分，提供了支持扩展、二次开发的能力。
- 非常适合里将 `Shoulder` 作为公司里的基础脚手架，或是在`毕设`、`外包`、等项目中快速获得一些常用功能，加速开发！

### 设计依据

**shoulder** 复用/优化当前业界成熟的解决方案（站在巨人的肩膀上），将 `开源社区` 中优秀的实现带给使用者，给使用者一个肩膀！
为减少使用者的上手成本，在用法上借鉴了 `Spring Boot`，使用 `Shoulder` 就像使用 `Spring Boot` 的 `starter` 一样简单（开箱即用）！

- 实现上参照了 **[软件优雅设计与开发最佳实践](http://spec.itlym.cn)** 中提到的内容，但没与它完全绑定，仅将该实践方案作为默认实现，实际中可替换、可二次开发。
- 可制定自己团队/项目的规范，在 **Shoulder** 基础上增加自己个性化部分，即可快速实现自定义规范并获得常用的功能实现。

### 提供的能力

- 统一系统级配置项（可配置）
    - 字符集、语言、日期格式等
- 日志、异常、错误码
    - `日志`、`异常`、`错误码` 打通、改造了 `lombok` 源码，提供简化开发注解，极大减少代码量
    - 采用 `Sl4j` 规范，无兼容问题，最小化配置、开箱即用
    - 统一错误码格式（也支持配置）、规范代码中的错误
    - 提供错误码实践方案，让 `错误码规范` 不在头疼
    - 提供全局`异常`、`错误码`处理
- 操作/审计日志
    - 可扩展的输出格式
    - 可扩展的目标源
    - 可扩展的记录流程
    - 基于注解的使用
- 国际化与多语言翻译
    - 丝滑的翻译封装
    - 完善的多语言支持
- *完善的*加密方案与实现
    - 不像其他第三方 jar ，仅提供只提供 `AES\RSA\ECC\SHA\MD5` 等公开算法的实现。*shoulder* 还在这之上提供了`安全` `可靠` `可生产落地` 的方案与实现，如 `多级密钥管理`、`加密算法平滑升级`
    - 安全存储加解密
    - 可配置的非对称密钥端点
    - 安全认证方案
    - 数字摘要算法（哈希算法）、抗抵赖的签名算法
    - 简化使用的统一接口
    - 便于快速上手的门面工具类
    - 基于 `ECC` 的密钥协商实现
    - 安全传输方案（基于 `注解` 极简使用）
- 通用业务代码封装
    - 数据库封装，如枚举与字段转换
    - 基本业务封装，增删改查系列
- 校验框架
    - 基于 `JSR` 规范，实现更多常用校验规则
    - 与异常、错误码、多语言打通，简化开发难度
- 安全系列
    - 常见web攻击的防御器
    - Oauth 授权
        - 认证服务器、资源服务器等（扩展 `Spring Security` ）
    - 充分灵活的认证框架（扩展 `Spring Security` 迁移自 [learn-spring-security](https://gitee.com/ChinaLym/learn-spring-security)）
- 依赖管理 & 开箱即用
    - 与 `Spring Boot` 类似，无需再思考引入哪个版本，会不会冲突，`Shoulder` 管理了常用依赖的版本号，如 `spring-boot`、`spring-cloud`、`spring-cloud-alibaba`
    - 自动依赖，当你引入 `shoulder-starter-web` 不需要再引入 `spring-boot-starter-web` 也不需要担心还要依赖什么
    - 最小化配置，提供了默认的配置项，自带建议配置。
    - 配置项支持 `IDE` 的自动提示
- 更多
    - ...
    
# 快速开始

推荐通过 `Shoulder` 提供的示例工程，快速感受 `Shoulder` 带来的优雅编码体验。
- Demo 地址: [github](https://github.com/ChinaLym/shoulder-framework-demo/tree/master/demo1)  [gitee](https://gitee.com/ChinaLym/shoulder-framework-demo/tree/master/demo1)

### 新建 Maven 工程

可以直接使用以下 `pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <!-- 继承 shoulder 提供的父工程，自动管理版本号，包含了 spring-boot-parent -->
    <parent>
        <groupId>cn.itlym</groupId>
        <artifactId>shoulder-framework</artifactId>
        <version>0.1</version><!-- shoulder-version -->
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
注：`Shoulder` 自动管理了 `Spring Boot` 版本号，提供了功能扩展，但并未屏蔽或改变 `Spring Boot` 给我们的功能与用法哟~

### 已有工程使用

已有工程大多数已经继承了 Spring Boot 提供的父工程，如果不想改动，只需要引入 shoulder 的 bom，然后引入想要的模块即可即可~

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
```
如希望使用 Shoulder 中 web 相关的能力增强，只需引入 web 模块
```xml
        <dependency>
            <groupId>cn.itlym</groupId>
            <artifactId>shoulder-starter-web</artifactId>
        </dependency> 
```

# 常见问题

### xxx功能的使用/扩展

首选要确定该能力是否是 `Shoulder` 提供的，不然方向错了，就白忙活啦（注：可以通过包路径、类路径来判断~）。

Shoulder提供的能力可以参见[使用手册]()（TODO wiki）
一些功能并不是 Shoulder 提供的，Shoulder仅仅是帮你引入咯~ 如 `Spring Boot`、`Spring Cloud` 等优秀第三方库为我们提供了大量能力（致敬），使用这些时可以去看他们官方的一手教程，或搜索对应关键词。如

- shoulder Web 工程中如何使用添加自己的过滤器、拦截器
    - 过滤器、拦截器等的基本功能是 `Spring Boot` 提供的，可以搜索 `Spring Boot 过滤器` 而非 ~~`Shoulder 过滤器`~~

### 使用中遇到问题、报错

认为是 Shoulder 框架的 Bug 时可以在 `Issure` 中留下你遇到的问题，如何复现，尝试排查、解决的手段，甚至缺陷修复方案。

欢迎提交代码~ 为 Shoulder 添加一个你设计/实现/扩展的能力、修改 `BUG`、修改代码格式、完善注释都可以的~ 

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

## Shoulder 技术选型偏好：

喜欢引入新的优秀技术，但不盲目追捧，当且仅当有完整的实践方案

- 优先为业界标准
- 其次为技术先进且已用于互联网厂商的生产环境，且容易从原有的主流技术迁移
- 其次绝对主流
- 依赖的第三方库中核心部分的版本选型偏好：生产可用（安全）的较新的 `Release` 版本

##  **[软件优雅设计与开发最佳实践](http://spec.itlym.cn)**  是什么

一千开发手里有一千种实践之路，这一千条路中，`平坦的`（开发维护成本低）、`坎坷的`（开发维护成本高） 差别很大，使用业界有成熟的解决方案往往会**事半功倍**！
为保证 `代码性能` ，降低 `维护成本`、`开发成本`。**Shoulder** 还提供了`技术选型指导`和一定的`开发规范`，带你轻松踏上最佳实践之路！[点击这里可以查看 Shoulder Framework 推荐的开发规约](http://doc.itlym.cn)

## Shoulder 平台与计划

`Shoulder` 希望做一个整套的可复用的平台（`PaaS`），使用者只需要做做自己的业务即可。整体格局如下

- `Shoulder iPaaS` 基础中间件环境 Shoulder 提供依赖中间件的`Docker`镜像或部署教程（如 数据库、消息队列、服务注册中心、任务调度中心、搜索引擎、报警与监控系统等）。
- `Shoulder Specific` 软件系开发设计注意事项、[落地方案和规范](http://spec.itlym.cn)
- **Shoulder Framework**  即本开源项目，提供共性能力封装，减少代码冗余，降低系统开发维护成本。
- `Shoulder Platform` 共性业务平台，提供 `用户平台`、`支付平台`、`通知中心`、`业务网关`、`数据字典`、`全局ID生产器` 等基础、通用业务能力平台
- `Shoulder Platform SDK` 以 sdk 形式方便业务层对接使用。 

## 发行版本号说明

采用 [主版本号.次版本号.修订号](https://semver.org/lang/zh-CN)（[MAJOR.MINOR.PATCH](https://semver.org)） 的形式

## 建议与反馈

感谢小伙伴们的 **[Star](https://gitee.com/ChinaLym/shoulder-framework/star)** / **Fork**，欢迎在 issue 交流，留下你的建议、期待的新功能等~

欢迎 fork 并提交合并请求一起改善该框架 [合作开发流程与项目介绍](CONTRIBUTING.MD)

