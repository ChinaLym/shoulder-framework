# 学习 **[shoulder-framework](https://gitee.com/ChinaLym/shoulder-framework)** - Spring Security Oauth2 授权、OIDC 认证

## 模块功能

基于 token 的认证实现

-

阅读 [shoulder-security](https://gitee.com/ChinaLym/shoulder-framework/tree/master/shoulder-build/shoulder-starter/shoulder-starter-auth-token)

- 作为 认证服务器发 token
- 作为资源服务器用 token 保护资源

主要关注点在于 `TokenSecurityConfig`，即使用者在配置 spring security 时，把shoulder框架中提供的自己需要的能力开启即可。

---

## Spring Security 配置注意点

- 请求授权之前需要先通过 spring security 认证才行，否则无法尝试授权

- 认证流程通过 tokenSuccessHandler 改造，认证成功后，直接发放 oauth token（jwt/redis/db）

- 再次请求时，服务作为客户端，将头部认证信息取出尝试认证

- 认证失败，默认 BearerTokenAuthenticationEntryPoint 处理，在这里查看具体错误信息

- spring security 异常处理过滤器：ExceptionTranslationFilter

- `JwkSetUriJwtDecoderBuilder.RestOperationsResourceRetriever.retrieveResource` 因为实际报错是 nimbus 中抛出的，故没有打印日志，bug 难以排查

---

Shoulder 中默认提供了两个 TokenEndpoint

- IntrospectEndpoint
    - 校验 token 是否合法
    - 适合 opaqueToken
- JwkSetEndpoint
    - 提供服务器公钥信息
    - 适合 JWT with JWK

---

[Spring Security使用JWT时的几个坑](https://www.jianshu.com/p/af955c2df0be)