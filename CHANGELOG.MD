# 更新记录

## 0.4 （doing）
- 提供 starters
    - mysql
- 全局唯一标识生成器（分布式全局id）
    - 高性能可配置的全局 id 生成器（持续高压场景为 twitter 雪花算法万倍性能），优雅处理时钟回拨，支持突发峰值、持续高压
    - todo 多实例小场景、分布式默认装配
- `operation-log` 模块
- 完善安全认证实现
    - 重构安全认证模块
    - 支持自签 `Oauth2 JWT Token 认证`
    - session 支持集群模式
    - security 的全局异常处理器
    - successHandler 同时支持 session、token（`TokenAuthenticationSuccessHandler`）
- 接口响应自动包装支持配置排除路径
- 接口文档选型
    - 注解同时支持 swagger3（主）/swagger2
    - 注释支持 smartDoc
    - 可视化界面默认使用 xxx
- ~~默认使用 `Undertow` 而非 `Tomcat`~~
    - 取消各个模块对 tomcat 的依赖
    - 注意 `undertow` 不支持jsp
    - 实际未实现，默认 tomcat、若希望使用 undertow 使用者自行排除即可
- 框架异常整理
    - 供使用者直接使用的工具
    - 抛出带错误码的 RuntimeException
- 翻译支持spring原生用法、增加识别jar内多语言资源文件夹
- 增加 `maven archetype`，便于快速创建工程


## 0.3 （stable）
- 数据库功能增强：分页、自动补充创建者、修改者、创建时间、修改时间
- 更简单的服务间安全传输(基于`ECDH`，jdk15中才加的功能，shoulder已经在jdk8中实现，且为Spring Boot 提供了开箱即用的能力)
- 增加 `ColorStringBuilder`，方便构建彩色输出
- 优化新增自动日志与美化（自动区分本地和生产环境）
    - `HTTP` 接口自动记录日志支持单行格式
    - 自动记录 `RestTemplate` 接口调用，默认支持彩色和单行两种
    - 重构日志自动记录类关系，便于二次扩展，自定义日志规则（如某些用户记录、特定请求记录等）与格式
- 完善安全认证实现，`browser`可用
- 完善验证码框架，可用
- 增加监控模块（线程池监控、错误码、异常监控），基于 `micrometer`，可使用 `Prometheus` 等对接
- 升级依赖 Spring Boot-> 2.3.4, Cloud-> H.S8，修复 RFD 漏洞
- 废弃 trace、minio、aopx 三个模块，移动至 shoulder-platform 中，Spring 高级特性使用保留原生用法

## ~~0.2（2020年9月10日）~~
- 数据库功能支撑
    - 连接池选型为 beeCP，近似 spring boot 默认连接池两倍性能
    - 引入 `mybatis-plus` 增强 mybatis
    - 数据源动态切换
- 更舒服的控制台日志（针对开发阶段、基于色彩学，DEBUG=淡灰色；INFO=控制台默认色；Warn=蓝色；异常/Error=粗体、红色；行号：蓝色；线程名称/线程id/时间=跟随日志级别）
- JSON 工具支持自动扩展
- 新增http接口自动记录日志 Controller日志
    - 开发阶段更好的调试体验
    - 支持IDE点击打印日志，自动跳转代码位置
- 可监控、动态调整的线程池
    - 可实现负载告警、动态扩容、资源释放、执行统计
- 将 spring-web 日志级别提高为INFO，防止无用日志过多
- 添加监控对接技术方案

- 已知问题
    - 版本号不正确（snapshot）
    - 签名异常


## 0.1 （2020年8月5日）

首次发布，将大多数基本功能测试并发布
