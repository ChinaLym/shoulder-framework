# shoulder-security

认证、授权能力
依赖并跟随 `Spring Security`

- 认证（登录） Oauth2.0、OIDC（Open ID Connect）、SAML（对接旧系统）
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

## 方案对比

Session
- 非常简单，上手迅速
- 天然支持Web端，开箱即用
- 分布式场景需要接入其他中间件（有现成方案），不支持前后端分离
- 适合：仅限与浏览器直接交互的WEB端应用


Token（JWT）：
- 简单灵活，易定制与扩展
- 但需要自行实现基于JWT协议的单点登录服务，若使用以下两种则只需适配
- 有续签问题
- 适合：无状态的分布式API服务、移动端应用。单点登录、授权。WEB应用也可使用但没必要。

| 特点\方案 | CAS | Oauth2 |
| --- | --- | --- |
| 优点 | 文档清晰，简单<br>功能完善、权限、认证部分功能丰富<br>可以结合JWT | 功能完善、成熟；<br>有现成方案，可搭建稳定易分离的认证/资源服务器<br>可结合JWT |
| 缺点 | 默认支持web端，对于前后分离的服务支持需要自行改造 | 相对略复杂 |
| 适用场景 | 单点登录 | 授权、第三方登录、单点登录 |

---

- 启动后报错：`org.springframework.security.web.authentication.rememberme.CookieTheftException: Invalid remember-me token (Series/token) mismatch. Implies previous cookie theft attack.`
    - 一般是极端时间内发送了两个请求导致的:第一个请求将通过，并为随后的请求生成新的哈希，但第二个请求仍带着旧的令牌来访问，因此导致异常。
    - 解决：静态资源不要拦截，如 css、jpg、js 等，保证请求一个页面时仅发生一次过滤即可。其他：该情况经常在启动后第一次访问，连续刷新页面时发生。
    
