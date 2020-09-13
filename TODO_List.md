# 实现

## 工具类
多语言翻译工具类回滚至java原生用法，兼容shoulder推荐的方式。因为原生的方式一些 IDE 对其有特殊支持，自定义的层级方式在读取 jar 内资源时，需要额外处理。
shoulder-core 中未使用，但常用的工具类考虑抽至新的 util 包

## 校验部分

校验与错误码整理

## 数据库增强

- mybatis-plus 日志输出美化（）
- JPA 类型转换
- 通用简单业务代码
    - 简单增删改接口
    - 批量增删改
    - 异步导入与导出（基于csv/excel）

## WEB
- 考虑使用自定义的 ObjectMapper 替代，MappingJackson2HttpMessageConverter 中的，获取更好的接口兼容性？

将 @Async 的线程池开发出来，以配置形式？

- 接口日志美化？
- api 带版本
- @API 注解区分哪些是对外（第三方）暴露的接口，哪些是系统内部的接口


操作日志推荐加在 ControllerImpl、ListenerImpl、ScheduleImpl ，避免业务嵌套，否则还需要考虑业务传播与覆盖，参考Spring 的事务传播
```
- 同时记录父子业务
    - 为子业务创建一个新的业务上下文，分别记录嵌套业务
    - 在当前业务基础上创建一个子业务上下文，分别记录父子业务
- 忽略子业务
    - 仅记录最外层业务，不需要关心和记录一般子业务
    - 如果存在父级业务，则忽略本次业务
- 忽略父业务（不推荐）
    - 仅记录最内层业务，忽略外层开启的业务
```

#### 租户支持


#### api 文档
- open-api 2/3 ?
- 接口上报到网关？
    - 自动推送
    - 插件生成网关可解析的格式
    - 推到系统中心/字典组件
    
#### 枚举与字典集成
扫描特定枚举类，生成字典信息到统一目录，字典服务
    
## spring-cache
使用 ConversionService 代替 StringRedisSerializer 获得更好的编码体验

## 安全

文件上传过滤，统一

### spring security

当前用户信息 DTO
第三方登录等

## 待定
~~编写id 生成器~~ 引入第三方

# 提供技术选型推荐

csv、excel 


# 使用说明补充

test & 使用说明




# shoulder-platform

开放默认EndPoint
    /redirect/**
    /current/userinfo

SDK

sessionKey枚举

