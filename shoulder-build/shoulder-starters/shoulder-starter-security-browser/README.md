# shoulder-security

封装了 spring-security，支持 web（http） 环境的认证功能以及自动装配，

适用场景：前后端不分离，与浏览器直接交互。如：使用模板引擎等将前端文件放在 `resources`

- 提供了默认的登录页面
- 支持单点登录 SSO
- session 支持在集群模式自动切换为 redis 存储


    
- 通过 Token 认证
    - 场景：前后端分离，后端不直接与浏览器交互；服务间认证；后端要与APP、微信小程序等交互。如：前端使用 nodeJs 部署
    - 自签 Token，常用于单机/集群
    - SSO：分布式
        - 分布式 + 服务集群

- 通过第三方认证
    - SSO，通过
    - OAuth2/OIDC

- 授权
    - Oauth2 单机/集群
    
    
各个模式激活时均默认以单机模式装配，可通过 `cluster=true` 切换至集群模式，默认将存储转移至 redis（可替换为其他）

