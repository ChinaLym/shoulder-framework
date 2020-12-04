# 更新记录

## 0.6 （pre）

- 接口文档选型
    - 注解同时支持 swagger3（主）/swagger2
    - 注释支持 smartDoc
    - 可视化界面默认使用 xxx

## 0.5 （测试中 && 待发布）

- 批量业务抽象支持
    - csv / excel 解析
    - 批量校验、查询进度、导入、查询进度、查询导入历史、导入详情
    - 导出

- 全局id生成器
    - 提供db方式
    - 性能提升
    - 增加透支消费上限配置、智能阻塞
    - 超高压力下识别缓存buffer过度消费（只在单节点亿级/s以上的压测中才可能出现）
    
- 日志框架
    - 为 debug、info、warn 级别增加类似 errorWitErrorCode 的方法，支持更低级别打印错误码
    - core 中提供 LogHelper 用于生成方法栈目标栈的跳转链接（从 http 的 HttpLogHelper 迁移）
  
- web 
    - 全局异常拦截：现在会对 JSR303 校验框架抛出的异常做详细日志记录以及返回值封装
    - 日志记录：支持记录 MultiPartFile 类型参数信息
    
- 校验框架
    - 参数相关错误码移动至 validate
    - 提供默认翻译项（通过插件生成）
    - @MimeType 改为 @FileType 并增加更多校验能力
        - 文件类型检查从 mime 类型，改为后缀名 allowList -> allowSuffix
        - 增加对文件头的检查
        - 增加对文件大小的检查
        - 增加对文件名称格式限制检查（支持正则，包含允许字符检查、禁止字符检查）
        - 增加对批量文件上传校验支持
    
- 验证码框架
    - 支持一个 url 需要校验多种验证码
        
- 优化基础包中的非必选强依赖
    - 如 spring，降低最小使用依赖成本
    
- ~~默认使用 `Undertow` 而非 `Tomcat`~~
    - 取消各个模块对 `tomcat` 的依赖
    - 注意 `undertow` 不支持jsp
    - 废弃该需求，不实现：默认 `tomcat`、若希望使用 `undertow` 使用者自行排除即可

- 分布式锁
    - 提供锁的概念、接口定义
    - 默认实现
        - JDK 适配（非分布式）
        - 基于内存（伪分布式，默认）
        - 基于数据库（依赖了数据库则默认使用该方式）
        - 基于 Redis

- 安全与认证
    - 修复密钥交换流程错误bug
    - 提供 Token 认证方式默认实现和装配
    - 完善 session 认证中条件装配
    - 添加基于 Session 认证 Demo
    - 添加基于 Token 认证 Demo

- 操作日志框架
    - 支持加了该注解的方法 A 中调用 加了该注解的方法 B 时
    - 支持自定义业务传播行为（日志上下文创建策略），接口：OperationContextStrategy
    - 调整日志上下文的包名、创建器类名 `OperationLogBuilder` -> `OperationLogFactory`

## 0.4 （stable）
- 提供 starters
    - mysql
    
- 全局唯一标识生成器（分布式全局id）
    - 高性能可配置的全局递增唯一 id 生成器（单节点持续高压场景为 twitter 雪花算法**两百万倍+**、JDK UUID的百倍+！、百度开源算法的 **60 倍！**）。
    - 优雅处理时钟回拨，支持突发峰值、持续高压，支持扩展
    - todo 多实例小场景、分布式默认装配
    
- `operation-log` 模块

- 完善安全认证实现
    - 重构安全认证模块
    - 支持自签 `Oauth2 JWT Token 授权`
    - session 支持集群模式
    - security 的全局异常处理器
    - successHandler 同时支持 session、token（`TokenAuthenticationSuccessHandler`）
    
- 接口响应自动包装支持配置排除路径

- 框架异常整理
    - 供使用者直接使用的工具
    - 抛出带错误码的 RuntimeException
- 翻译支持spring原生用法、增加识别jar内多语言资源文件夹
- 增加 `maven archetype`，便于快速创建工程


## 0.3
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
