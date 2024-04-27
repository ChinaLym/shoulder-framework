# maven archtype 基于 Velocity 生成工程，所以两个井号之间内容会被忽略，所以要输出必须要加反斜线 #

\\# ${appId}

该工程通过 `shoulder-archetype-simple` 创建

- Shoulder 开源地址：[github](https://github.com/ChinaLym/shoulder-framework)

\\#\\# 包目录结构

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

\\#\\# 部署方案建议

\\#\\#\\# 结论

* 不间断运行 / 互联网类软件：通过 spring-boot jar 方式 + docker 部署。

* 传统型软件产品：通过 tomcat 部署。

\\#\\#\\# 依据

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


