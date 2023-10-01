# BUG

demo2:batchService没注入？引入 csv 依赖
DEMO3\DEMO4: 编译问题？本地缓存？
DEMO4: org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration.springSecurityFilterChain
demo5: 加解密

# 实现

## 校验部分

校验与错误码整理
考虑前缀区分大类，调用方出错（如参数）/ 系统内部错误（如取缓存时反序列失败） / 第三方服务失败（如调用其他服务，结果其他服务挂了，返回500）

## 数据库增强

- mybatis-plus 日志输出美化（Optional）
- JPA 类型转换（AttributeConverter，Optional）
- 通用简单业务模板代码
    - 应用内部广播变更通知

#### ~~租户支持~~
租户这个概念，不同的业务场景有不同的决策逻辑，直接用租户承载未必是最合适的，而是贴近业务场景，将业务最真实的隔离条件依据作为隔离标更好，如地区，国家，公司主体等

#### api 文档
- open-api v2/v3 ?
- 接口上报到网关？【platform 用client SDK支持】
    - 自动推送
    - 插件生成网关可解析的格式
    - 推到系统中心/字典组件
- api 带版本
- @API 注解区分哪些是对外（第三方）暴露的接口，哪些是系统内部的接口

#### 枚举与字典集成【Preview】
扫描特定枚举类，生成字典信息到统一目录，字典服务

## spring-cache
- RedisSerializer：使用 ConversionService 代替 StringRedisSerializer 支持枚举等，获得更好的编码体验【Optional】


### spring security

当前用户信息 AppContext | Spring Security 提供的上下文
第三方登录等，暂未封装
【OK】 **支持 token 认证**
获取浏览器信息 UserAgentUtils

# 提供技术选型推荐

【OK】 csv(xstream)、excel pdf(itextpdf) 导入导出 JsonPath

定时任务。

sessionKey枚举 appInfo ip/domain 运行模式，如 docker / jar / war / side-car
- 不放 session，而是静态工具，降低内存占用

# 国际化（Optional）

com.github.fge:msg-simple / json-patch / jackson-coreutils / btf


# 编译时增强（CI部分）

静态检查：Error-prone,Google

# 标准

OpenAPI 3
OpenTracing？OpenTelemetry？（目前 OpenTelemetry 在发展中，主流还是Opentracing，但看好未来几年的OpenTelemetry）
MicroMeter

# 自动生成配置相关

### 代码
数据库相关
启动程序一定要 try Catch 打印进程开始、异常、结束


### 日志

详细分文件 按照来源比。如 shoulder / spring / other 等，服务内部也可以分 common core 按照角色分。如 rpcClient rpcServer msgConsumer msgProducer
configClient db heartbeat monitor trace 定时任务 上下文启动、配置 监控状态 按照级别分文件 按照时间分文件 √ 按照大小分文件 √

增加 traceId spanId parentTraceId

### 上下游检测

注册中心、（配置中心）、数据库、消息队列、下游系统 若未启动，为了防止提供服务时，下游服务不存在，最多延迟 10s启动（每500ms检测一次）

### 脚本

服务启动需要的 jvm 参数 启动命令以及扩展点 - healthCheck 健康检查 - nginx 脚本 - start 启动脚本 - 状态/关闭等 - 扩展点。如安装前、安装后，升级前，升级后，卸载前，卸载后


### nginx 代理
路由配置
若设置自动代理时，最好将 static 静态资源目录放到 start，而不是 web 等模块


# 为 OpenAPI 提供统一的工具类
认证、签名、验证、发起调用 ssl、token缓存 aksk等

## Http

RestTemplate/WebFlux 增加 tracer（postProcessor + Interceptor）发送前记录 clientSend 发送后记录 clientReceive、appId、spanType、aimAppId、requestUrl、method、host、port、requestPackageSize
在请求头中添加
Feign 增加 Tracer【包大小、线程名、响应code】

Mvc 增加 tracer
tracer 增加配置，如服务器（zipkinServer）保存的追踪日志
增加方法级trace拦截方式
- 基于类
- 根据注解记录方法，这个注解最好是可扩展的（因为使用者可能已经自定义一套或者使用其他的了）
    - 框架默认注解 *
    - 扩展注解
- 自定义

# web

@Async 的方法前后记录运行时间等

日志相关类做 applicationContext 级别隔离

ketamahash和murmurhash

sla（sli/slo）

灰度

常见敏感信息工具，如 idCard phoneNum userName homeAddress url

事件机制

# 监控
健康检查 actuator 是否启动完毕、版本号

# 调度与定时任务
~~提供统一入口，并允许使用者自由切换底层实现（无论是 spring/xxljob/）~~
- 代价：新的API，新的使用方式，且不无法很好的获取底层的遍历

# 本工程维护
- ~~shoulder-maven-plugin 未开源 / 发布maven仓库，导致 shoulder-framework 其他人不能打包 / 发布~~
  - 已经将 shoulder-maven-plugin 开源发布 1.0 版本，且去除 shoulder-lombok 这类深度集成，现在任何人都可以编译本项目
- key 修改 支持 从pom读application.properties数据【低优先】
  - maven-resource-plugin 打包时候修改application.properties代码，填充值；因pom.xml内容相对固定，且application.properties可更换, 故有很多种方式来更好的维护，该项自行选择，不做框架的默认
