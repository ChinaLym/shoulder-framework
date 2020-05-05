# shoulder-security
安全相关基础定义
依赖并跟随 spring security

- 认证（登录） Oauth2.0、OIDC（Open ID Connect）、SAML
    - 模式：采取主流、功能最完善的 授权码模式

- 授权

Oauth2.0

响应的 JSON 推荐为
```json
{
"code":"num",
"data":"object",
"msg":"string"
}
```
本框架仅使用 msg 字段，即默认的响应为
```json
{
"msg":"xxx"
}
```