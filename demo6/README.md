# **[shoulder-framework](https://gitee.com/ChinaLym/shoulder-framework)** 的示例工程

## 模块功能

基于 token 的认证实现

- 阅读 [shoulder-security](https://gitee.com/ChinaLym/shoulder-framework/tree/master/shoulder-build/shoulder-starter/shoulder-starter-auth-token)


作为 认证服务器发 token
作为资源服务器用 token 保护资源
作为客户端，用 token 认证


注意，请求授权之前需要先通过 spring security 认证才行，否则无法尝试授权

认证流程通过 tokenSuccessHandler 改造，认证成功后，直接发放 oauth token（jwt/redis/db）

再次请求时，服务作为客户端，将头部认证信息取出尝试认证



TokenEndpoint

认证失败，默认 BearerTokenAuthenticationEntryPoint 处理，在这里查看具体错误信息

spring security 异常处理过滤器：ExceptionTranslationFilter
这里有几个接口 
`JwkSetUriJwtDecoderBuilder.RestOperationsResourceRetriever.retrieveResource` 没有日志，bug 难以排查
https://www.jianshu.com/p/af955c2df0be