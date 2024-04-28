<h1 align="center"><img src="doc/img/logo.png" height="40" width="40" /><a href="https://github.com/ChinaLym/shoulder-framework" target="_blank">Shoulder Framework</a></h1>

![LOGO](doc/img/shoulder-banner.jpg)

> 如果说我比别人看得更远些,那是因为我站在了巨人的肩上. ——牛顿

[![AUR](https://img.shields.io/badge/license-Apache%20License%202.0-yellow.svg)](https://github.com/ChinaLym/shoulder-framework/blob/master/LICENSE) [![](https://img.shields.io/badge/Author-lym-yellow.svg)](https://github.com/ChinaLym)

[![](https://img.shields.io/badge/Release%20Version-0.8-blue.svg)](https://github.com/ChinaLym/shoulder-framework/releases/tag/v0.8) [![](https://img.shields.io/badge/Spring%20Boot%20Version-3.2.x-blue.svg)](https://spring.io/projects/spring-boot) [![](https://img.shields.io/badge/Spring%20Cloud%20Version-2023.0.x-blue.svg)](https://spring.io/projects/spring-cloud/)

# 📖介绍 [English language](README.md)

`Shoulder Framework` 是一个基于 `Spring Boot` 的 `Java WEB` 开发框架，内置精巧功能，降低开发成本，提升WEB开发幸福感。

- 示例工程： [GitHub](https://github.com/ChinaLym/shoulder-framework-demo)、[Gitee](https://gitee.com/ChinaLym/shoulder-framework-demo)

### 对比 `Spring Boot`

`Shoulder Framework` 是 `Spring Boot` 的插件，融合了 **[软件优雅设计与开发最佳实践](https://spec.itlym.cn)** 和增强功能，在以下场景会比 `Spring Boot` 更好用！

- `毕业设计`、`外包项目` 等场景：快速获得一些常用功能（AOP自动日志、AOP异常处理、低SQL、WEB安全、内嵌式DB/Redis、多语言等），加速开发！
- `微服务底座框架`：许多公司/组织内部往往有些统一编码规范，`Shoulder`
  实现了这项工作中重复的部分（jar包设计、编译、分发、模块化、提供默认实现等），基于`Shoulder`二次开放将极大程度上降低了实现难度；
- `内部框架开发`: 基于 `Shoulder` 二次开发公司/组织里的基础jar：`Shoulder`对于规范处的实现类似 `Spring Boot`，均为`可扩展`
  的，可以非常方便的扩展、二次开发。

---

# 🚀 快速开始

## 体验官方 Demo

通过简单的 [示例工程](https://github.com/ChinaLym/shoulder-framework-demo/tree/master/demo1)，快速体验 `Shoulder`
的顺滑编码感觉。
在（[github](https://github.com/ChinaLym/shoulder-framework-demo/tree/master/demo1) / [gitee](https://gitee.com/ChinaLym/shoulder-framework-demo/tree/master/demo1)
）上体验。


## 在已有的 `Spring-Boot` 工程中使用

添加你需要的模块即可使用，如：希望使用`shoulder-web 的动态字典能力` ，则引入对应starter即可~

```xml
        <!--如希望使用 Shoulder 中 web 相关的能力增强，只需引入 web 模块-->
        <dependency>
            <groupId>cn.itlym</groupId>
            <artifactId>shoulder-starter-web</artifactId>
            <version>0.8</version>
        </dependency>
```

## 通过 maven-archetype 创建 Shoulder 项目

`Shoulder` 提供了 maven [archetype](https://github.com/ChinaLym/shoulder-framework/tree/master/shoulder-archetype-simple)，可通过该工程快速创建。请确保您本地已经安装 `JDK17+`、`Maven`

1. 打开终端或命令提示符。
2. 运行以下命令来生成新的 Shoulder 项目：

```shell
mvn archetype:generate \
  -DarchetypeGroupId=cn.itlym \
  -DarchetypeArtifactId=shoulder-archetype-simple \
  -DarchetypeVersion=0.8 \
  -DgroupId=com.yourcompany \
  -DartifactId=appName \
  -Dversion=1.0-SNAPSHOT
```

## 手动创建新 Maven 项目

可以直接使用以下 `pom.xml`，与 Spring Boot 工程唯一区别就是 `pom.xml` 中 `parent` 不同

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <!-- 继承 shoulder 提供的父工程，自动管理版本号，包含了 spring-boot-parent -->
    <parent>
        <groupId>cn.itlym</groupId>
        <artifactId>shoulder-parent</artifactId>
      <version>0.8</version><!-- shoulder-version -->
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

---

# ❓常见问题 & FAQ

优先参见 [FAQ 文档](doc/faq.md)

更多： [功能介绍.md](doc/ability-intro.md)、[工程目录 & 模块划分](doc/module-intro.md)、[设计理念 & 发展路线](ROADMAP.MD)

# ✈ 规划 & 发展路线

`Shoulder` 希望做一个整套的可复用的平台（`PaaS`），使用者只需要做做自己的业务即可。整体格局如下

- `Shoulder iPaaS` 基础中间件环境 Shoulder 提供依赖中间件的`Docker`镜像或部署教程（如 数据库、消息队列、服务注册中心、任务调度中心、搜索引擎、报警与监控系统等）。
- `Shoulder Specific` 软件系开发设计注意事项、[落地方案和规范](https://spec.itlym.cn)
- **Shoulder Framework**  本项目，提供共性能力封装，减少代码冗余，降低系统开发维护成本。
- `Shoulder Platform` 共性业务平台，提供 `用户平台`、`支付平台`、`通知中心`、`业务网关`、`数据字典`、`全局ID生产器` 等基础、通用业务能力平台
- `Shoulder Platform SDK` 以 sdk 形式方便业务层对接使用。

## 相关项目

| 项目                          | 开源地址                                                                                                                      | 说明                                                                                     |
|-----------------------------|---------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------|
| Shoulder Framework          | [github](https://github.com/ChinaLym/shoulder-framework)、[gitee](https://gitee.com/ChinaLym/shoulder-framework)           | 开发框架 / 在 Spring Boot Start 插件，结合[软件优雅设计与开发最佳实践](https://spec.itlym.cn)，增加常用的功能，助力高效开发。 |
| Shoulder Platform           | [github](https://github.com/ChinaLym/shoulder-platform)、[gitee](https://gitee.com/ChinaLym/shoulder-platform)             | `SaaS` 开发平台，提供了基础通用能力，与具体业务无关                                                          |
| shoulder-framework-demo     | [github](https://github.com/ChinaLym/shoulder-framework-demo)、[gitee](https://gitee.com/ChinaLym/shoulder-framework-demo) | 以简单的例子介绍 `Shoulder` 的使用                                                                |
| shoulder-plugins            | [github](https://gitee.com/ChinaLym/shoulder-plugins)、[gitee](https://gitee.com/ChinaLym/shoulder-plugins)                | `Shoulder` 提供的 `maven`插件，构建时扫描代码注释自动生成多语言翻译文件，从而提效                                     |
| shoulder-lombok             | [github](https://github.com/ChinaLym/shoulder-lombok)、[gitee](https://gitee.com/ChinaLym/shoulder-lombok)                 | 同`lombok`，额外支持 `@SLog` 注解，`lombok`风格省略 `Shoulder` 扩展的 `Logger` 代码                      |
| shoulder-lombok-idea-plugin | [github](https://github.com/ChinaLym/lombok-intellij-plugin)、[gitee](https://gitee.com/ChinaLym/lombok-intellij-plugin)   | `IDEA 插件`，同 `lombok-idea-plugin`，额外支持`shoulder-lombok` 中的 `@SLog`                      |
| Shoulder iPaaS              | [github](https://github.com/ChinaLym/shoulder-ipaas)、[gitee](https://gitee.com/ChinaLym/shoulder-iPaaS)                   | `iPaaS` 平台，分享部署常见中间件步骤、如何自建监控系统、私有基础平台                                                 |

# 📒 版本变更记录

当前版本为 `0.8`，更多见 [CHANGELOG.MD](CHANGELOG.MD)

# 💗 贡献代码

欢迎各类型代码提交，不限于`优化代码格式`、`优化注释/JavaDoc`、`修复 BUG`、`新增功能`
，更多请参考 [如何贡献代码](CONTRIBUTING.MD)

# 📩 反馈 or 联系我

感谢小伙伴们的 **[🌟Star](https://gitee.com/ChinaLym/shoulder-framework/star)** 、 **🍴Fork** 、 **🏁PR**，欢迎使用 `issue`
或 [cn_lym@foxmail.com](mailto:cn_lym@foxmail.com) 交流，如 留下你的建议、期待的新功能等~

`Shoulder` 不求使用最广，而是致力于成为使用体验最好的开发框架，您任何的使用需求、建议、想法都可以留下来与我们沟通，`Shoulder`
将与您一起思考攻克疑难，助天下的开发者更好更安心得使用技术助力业务腾飞！

### 👨‍💼 关于作者

多次参与 Alibaba 核心系统重构与设计，主导过多次 D11 级别大促保障，欢迎技术交流与简历投递～
- 该项目为作者在业余时间独立开发和维护的个人项目，非阿里巴巴官方产品。
