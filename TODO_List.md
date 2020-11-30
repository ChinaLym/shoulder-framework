# 实现

## 校验部分

校验与错误码整理

## 数据库增强

- mybatis-plus 日志输出美化（）
- JPA 类型转换
- 通用简单业务代码
    - 简单增删改接口
    - 批量增删改
    - 应用内部广播变更通知

- api 带版本
- @API 注解区分哪些是对外（第三方）暴露的接口，哪些是系统内部的接口


#### 租户支持


#### api 文档
- open-api v2/v3 ?
- 接口上报到网关？
    - 自动推送
    - 插件生成网关可解析的格式
    - 推到系统中心/字典组件
    
#### 枚举与字典集成
扫描特定枚举类，生成字典信息到统一目录，字典服务
    
## spring-cache
使用 ConversionService 代替 StringRedisSerializer 支持枚举等，获得更好的编码体验


### spring security

当前用户信息 AppContext | Spring Security 提供的上下文
第三方登录等，暂未封装
【OK】 **支持 token 认证**

# 提供技术选型推荐

【OK】 csv、excel 导入导出

定时任务。 

sessionKey枚举

