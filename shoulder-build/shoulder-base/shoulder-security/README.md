# shoulder-security

认证、授权能力
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

---

- 启动后报错：`org.springframework.security.web.authentication.rememberme.CookieTheftException: Invalid remember-me token (Series/token) mismatch. Implies previous cookie theft attack.`
    - 一般是极端时间内发送了两个请求导致的:第一个请求将通过，并为随后的请求生成新的哈希，但第二个请求仍带着旧的令牌来访问，因此导致异常。
    - 解决：静态资源不要拦截，如 css、jpg、js 等，保证请求一个页面时仅发生一次过滤即可。其他：该情况经常在启动后第一次访问，连续刷新页面时发生。
    

