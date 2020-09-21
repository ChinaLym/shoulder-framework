# **[shoulder-framework](https://gitee.com/ChinaLym/shoulder-framework)** 的示例工程

## 工程介绍

- demo1
    - 介绍了 **[Shoulder](https://gitee.com/ChinaLym/shoulder-framework)** 框架基本使用，便于快速入门
    - 通过对比使用 `Shoulder` 与原生的 `Spring Boot` 来展示 Shoulder 提供的能力与快速开发。
    - 包含：日志、错误码、多语言翻译、返回值自动包装、异常自动拦截、数据存储加解密、前后端传输加解密
    
- demo2
    - 演示对数据的封装
    
- demo3
    - 演示安全认证相关
        - 用户名、密码登录
        - 手机短信登录
        - 验证码
        
- demo4
    - spring boot 自动监控
    - 轻松实现监控自己的线程池状态，同步到监控与报警中心 Prometheus，Grafana 展示
    
- demo5
    - 应用间传输加解密【绝密！不可破解、不可伪造、不可抵赖！】
    - 完整的客户端与服务端实现，只需要在 `Spring` 的基础上知道一个 `@Sensitive` 注解即可实现整套功能


## 下载、部署、运行

- `git clone https://gitee.com/ChinaLym/shoulder-framework-demo` 下载本项目
- 进入对应的工程，如 `demo1` ，将工程导入 `IDEA`/`ECLIPSE` 将其在本地运行（默认8080端口）
- 根据其中的 `READE.ME` 或代码注释进行测试，跟着 demo 了解 shoulder 的能力

## 推荐的学习目标

- 学会使用 Spring Boot/Shoulder
- 可以通过框架提供的配置项来更改框架提供的功能
- 理解实现原理
- 可以根据 `Spring` 或 `Shoulder` 框架预留的扩展点或接口来实现自己的功能
- 分享自己的想法和设计，为 `Shoulder`/`Spring Boot` 提交代码

注：可以根据自己的开发经验，选择性跳过一些基础的介绍~

## 参与贡献

1.  `Fork` 本仓库
2.  新建 `feat_xxx` 分支，如：演示对数据库使用的封装可以为 `feat_db`
3.  提交代码
4.  新建 Pull Request

合并之后就可以看到你新建的工程了~

## 推荐的学习顺序

Shoulder 是基于 Spring Boot 的，Shoulder 希望通过本项目帮助各位同学更好的学习 `Spring Boot` 

### Spring Boot 基础知识介绍

为了更好地学习 Spring Boot 的使用，可以从以下开始。

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.3.2.RELEASE/maven-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/2.3.2.RELEASE/maven-plugin/reference/html/#build-image)
* [Spring Web](https://docs.spring.io/spring-boot/docs/2.3.2.RELEASE/reference/htmlsingle/#boot-features-developing-web-applications)
* [Spring Boot DevTools](https://docs.spring.io/spring-boot/docs/2.3.2.RELEASE/reference/htmlsingle/#using-boot-devtools)

### Spring Boot 指南

Shoulder 框架是基于 Spring Boot 之上的，这里有 Spring 如何创建 Restful Web Service 的指南。

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)

* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)

* [Building REST services with Spring](https://spring.io/guides/tutorials/bookmarks/)

