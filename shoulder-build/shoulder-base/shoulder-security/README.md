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

----
 
## 方案

### `TOKEN` 续签问题

通过放redis来解决

- 发放 Token（accessToken） 时，同时将 Token 放置于 Redis 中，key、value均为发出的`Token`，有效期为双倍的Token有效期
- 当请求来临时，校验 Token 中的过期时间，如果未过期，则直接通过（原流程）
- 若requestToken过期，则查 redis 是否存在该 token 对应的 cacheToken
    - 如果 `cacheToken` 存在，则说明刚过期不久，用户仍然处于活跃状态，校验该 `cacheToken` 有效期
        - 如果 `cacheToken` 未过期，则将其当作本次请求的 token 处理请求
        - 如果 `cacheToken` 过期，则创建新的 Token 并放置于 Redis 中，以原来的 Token 为 Key（key 不变），新的 Token 为 Value，过期时间仍然是两倍Token有效期
    - 如果 `cacheToken` 不存在，则说明用户已经不是活跃状态了，通知客户端触发重新认证流程（这时，客户端通过`refreshToken`或重新认证都是可以的）



---

- 启动后报错：`org.springframework.security.web.authentication.rememberme.CookieTheftException: Invalid remember-me token (Series/token) mismatch. Implies previous cookie theft attack.`
    - 一般是极端时间内发送了两个请求导致的:第一个请求将通过，并为随后的请求生成新的哈希，但第二个请求仍带着旧的令牌来访问，因此导致异常。
    - 解决：静态资源不要拦截，如 css、jpg、js 等，保证请求一个页面时仅发生一次过滤即可。其他：该情况经常在启动后第一次访问，连续刷新页面时发生。
    

---

# 流程

- **认证**：AbstractAuthenticationProcessingFilter#doFilter
    - 是否进行认证 RequestMatcher
    - 根据子类的 attemptAuthentication 处理认证
        - UsernamePasswordAuthenticationFilter【以该实现为例】
            - 从请求中获取参数
            - 验证参数是否合法
            - 组装成 AuthenticationToken
            - 为 AuthenticationToken 设置（请求）详情（如 sessionId、remoteAddress等、根据请求 request）
            - 调用 AuthenticationManager#authenticate(AuthenticationToken)【默认是 ProviderManager】
                - 根据 AuthenticationToken.getClass() 找到对应的 AuthenticationProvider（根据 supports 遍历）
                - **进行认证： 调用 authenticate(Authentication) 并返回一个 Authentication result**
                - 复制详情
                - 若发生认证异常（AccountStatusException | InternalAuthenticationServiceException） 调用 prepareException（上下文中发送认证失败通知）并向上抛出
                - 若未能处理认证 / 处理结果为 null，则尝试调用父类的再次尝试处理
                - 认证成功：擦除认证Token中凭证信息（如密码），发送认证成功通知
        - OAuth2ClientAuthenticationProcessingFilter
        - ...
    - 未通过认证：
        - RememberMeServices#loginFail
        - AuthenticationFailureHandler
    - 通过认证
        - SessionAuthenticationStrategy#onAuthentication
        - RememberMeServices
        - AuthenticationSuccessHandler
        
- **鉴权**：

类介绍
- AbstractSecurityInterceptor
    - FilterSecurityInterceptor 适用于 web 中对请求鉴权
    - MethodSecurityInterceptor 基于 Spring AOP，适用于只对 Service 方法鉴权
    - AspectJMethodSecurityInterceptor 适用于对 Service 方法、甚至可以做到领域对象鉴权
    
通常的做法是使用Filter对Web请求进行一个比较粗略的鉴权，辅以使用Spring AOP对Service层的方法进行较细粒度的鉴权。

AbstractSecurityInterceptor 逻辑（`org.springframework.security.web.access.intercept.FilterSecurityInterceptor.invoke`）
1. 先将正在请求调用的受保护对象传递给 `beforeInvocation()` 方法进行权限鉴定（权限鉴定失败就直接抛出异常了）
2. 调用完成后，无论成功还是抛异常，执行 `finallyInvocation()`
3. 未抛异常，则调用 `afterInvocation()`



三、Security 入门总结

1. WebSecurityConfiguration 类是如何根据 Spring Security DSL 创建 SpringSecurityFilterChain 中各个过滤器的只需了解即可。不要太过关注。
2. 重点记忆 创建认证令牌 UsernamePasswordAuthenticationFilter，ExceptionTranslationFilter，FilterSecurityInterceptor 这三个过滤器的作用及源码分析。
3. 重要记忆 处理认证结果 Authentication，AuthenticationManager，ProviderManager，AuthenticationProvider，UserDetailsService，UserDetails这些类的作用及源码分析。
4. 重点记忆 调用鉴权 FilterInvocation，SecurityMetadataSource，AccessDecisionManager 的作用。
5. 将这些类理解的关键是建立起关联，建立起关联的方式就是跟着本节中的案例走下去，一步步看代码如何实现的。


- [OAuth2 oauth_client_details表字段的详细说明](https://blog.csdn.net/wangxuelei036/article/details/109491215)
- spring security 核心类讲解（主要看第四篇。前三篇无干货）
    - 1: spring security简短介绍 https://www.cnblogs.com/wutianqi/p/9174227.html
    - 2: 模块介绍，demo创建 https://www.cnblogs.com/wutianqi/archive/2004/01/13/9177516.html
    - 3: spring security 过滤器的创建原理 https://www.cnblogs.com/wutianqi/p/9185266.html
    - [4: spring security 认证和授权原理（AbstractSecurityInterceptor）](https://www.cnblogs.com/wutianqi/p/9186645.html)

- [Spring Security 鉴权简介](https://www.cnblogs.com/fenglan/p/5913387.html)
