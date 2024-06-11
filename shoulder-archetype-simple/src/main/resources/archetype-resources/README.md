## maven archtype 基于 Velocity 生成工程，所以两个井号之间内容会被忽略，所以要输出必须要加反斜线
# ${appId}

# 📖介绍
该工程由 [Shoulder](https://github.com/ChinaLym/shoulder-framework) 的 `shoulder-archetype-simple` 创建 ，包目录结构如下

[Shoulder 文档地址](https://shoulder.itlym.cn/shoulder.html)

```
groupId.artifactId
    ├─config                 配置类
    ├─constant               常量类
    ├─controller             Controller
    ├─dto                    DTO
    │  ├─param                  入参
    │  └─result                 出参
    ├─enums                  枚举类
    ├─exception              异常
    ├─repository             存储层
    │  └─impl                   存储层实现
    ├─service                业务层
    │  └─impl                   业务层实现
    └─util                   工具类
```

---

# 🚀 快速开始

* 不间断运行 / 互联网类软件：通过 spring-boot jar 方式 + docker 部署。

* 传统型软件产品：通过 tomcat 部署。

# ❓常见问题 & FAQ

### 问题举例：Spring Boot 项目打包成 Tomcat 或 Fat Jar 方式运行有什么区别？

* Tomcat
    * 部署能极大的减少内存占用
    * 统一调参
    * 基础 Jar包共用、节约硬盘空间
    * 减少成果物大小；可以非常方便地替换的某个 `class`文件、 `jar`模块，而无需将整个应用打包重新部署，热更新方便
    * 成果物定制方便
    * 简化运维
    * 可以利用maven提供的机制进行热部署

* Fat Jar
    * 隔离性差，不会因为单个应用影响其他所有应用
    * 移植性好，打包后可直接运行，不需要绑定tomcat
    * 可以选择其他服务器，如`Undertow`、更底层的 `Netty`

# 📒 版本变更记录

当前版本为 `${version}`，更多见 [CHANGELOG.MD](CHANGELOG.MD)

# 💗 贡献代码

欢迎各类型代码提交，不限于`优化代码格式`、`优化注释/JavaDoc`、`修复 BUG`、`新增功能`
，更多请参考 [如何贡献代码](CONTRIBUTING.MD)

# 📩 反馈 or 联系我

感谢小伙伴们的 **[🌟Star](https://gitee.com/ChinaLym/shoulder-framework/star)** 、 **🍴Fork** 、 **🏁PR**，欢迎使用 `issue`
或 [Email](mailto:yourEmail@yourEmail.com) 交流，如 留下您的建议、期待的新功能等~
