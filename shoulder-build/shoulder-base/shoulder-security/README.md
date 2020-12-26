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

- **认证与颁发凭证**：`AbstractAuthenticationProcessingFilter#doFilter`
    - 是否进行认证 `RequestMatcher`
    - 根据子类的 `attemptAuthentication` 处理认证
        - `UsernamePasswordAuthenticationFilter`【以该实现为例】
            - 从请求中获取参数
            - 验证参数是否合法
            - 组装成 `AuthenticationToken`
            - 为 `AuthenticationToken` 设置（请求）详情（如 sessionId、remoteAddress等、根据请求 request）
            - 调用 `AuthenticationManager#authenticate(AuthenticationToken)`【默认是 ProviderManager】
                - 根据 `AuthenticationToken.getClass()` 找到对应的 `AuthenticationProvider`（根据 supports 遍历）
                - **进行认证： 调用 `authenticate(Authentication)` 并返回一个 Authentication result**
                - 复制详情
                - 若发生认证异常（AccountStatusException | InternalAuthenticationServiceException） 调用 prepareException（上下文中发送认证失败通知）并向上抛出
                - 若未能处理认证 / 处理结果为 null，则尝试调用父类的再次尝试处理
                - 认证成功：擦除认证Token中凭证信息（如密码），发送认证成功通知
        - `OAuth2ClientAuthenticationProcessingFilter`
        - ...
    - 未通过认证：
        - `RememberMeServices#loginFail`
        - `AuthenticationFailureHandler`
    - 通过认证
        - `SessionAuthenticationStrategy#onAuthentication`
        - `RememberMeServices`
        - `AuthenticationSuccessHandler`
       
- **认证颁发的凭证** AbstractPreAuthenticatedProcessingFilter
    - 处理经过预先认证的身份验证请求的过滤器的基类，其中认证主体已经由外部系统进行了身份验证。 目的只是从传入请求中提取主体上的必要信息，而不是对它们进行身份验证。

 
- **鉴权**：

类介绍
- `AbstractSecurityInterceptor` 【鉴权核心类，不仅仅用于 web】
    - `FilterSecurityInterceptor` 适用于 web 中对请求鉴权
    - `MethodSecurityInterceptor` 基于 Spring AOP，适用于只对 Service 方法鉴权
    - `AspectJMethodSecurityInterceptor` 适用于对 Service 方法、甚至可以做到领域对象鉴权
    
通常的做法是使用Filter对Web请求进行一个比较粗略的鉴权，辅以使用Spring AOP对Service层的方法进行较细粒度的鉴权。

**AbstractSecurityInterceptor** 逻辑（`org.springframework.security.web.access.intercept.FilterSecurityInterceptor.invoke`）

1. 【主要流程】 先将正在请求调用的受保护对象传递给 `beforeInvocation()` 方法进行权限鉴定（权限鉴定失败就直接抛出异常了），返回 InterceptorStatusToken token
2. 调用完成后，无论成功还是抛异常，执行 `finallyInvocation()`，主要用于刷新认证上下文 SecurityContextHolder.setContext(token.getSecurityContext());
3. 【主要流程】 未抛异常，则调用 `afterInvocation()`

- beforeInvocation(Object object)
    - 通过 getSecureObjectClass 安全检查入参是否为期望的，不是则抛异常（推测因为这块比较抽象，故做此检查）
    - obtainSecurityMetadataSource 获取 SecurityMetadataSource（鉴权依据数据源，一般是 `ExpressionBasedFilterInvocationSecurityMetadataSource`）
        - 根据 SecurityMetadataSource 根据入参 （FilterInvocation） 获取请求 request
        - 遍历 requestMap ，返回第一个 RequestMatcher.matches(request) 的 Collection<ConfigAttribute>，这里包含很多规则，如 WebExpressionConfigAttribute（WebSecurityAdapter里配的）
    - 若获取到的规则为空
        - rejectPublicInvocations == true （默认 true）则抛 IllegalArgumentException 异常
        - 否则发送 PublicInvocationEvent 通知，并返回 null。
        - beforeInvocation 方法会结束 
    - 若获取到的规则不为空：
        - 断言 SecurityContextHolder.getContext().getAuthentication() 不为空（已经认证过）
            - 若为空，一般表示非法调用，发送 AuthenticationCredentialsNotFoundEvent ，并抛对应的异常 AuthenticationCredentialsNotFoundException
        - 获取认证凭证，根据 alwaysReauthenticate（默认 false）决定是否再次通过 AuthenticationManager#authenticate 重新认证
        - **通过 `accessDecisionManager` 鉴权** `#decide(authenticated, object, attributes)`
            - accessDecisionManager 的抽象类是 `AbstractAccessDecisionManager`，有三个默认实现
         
        - 抛 AccessDeniedException 异常代表鉴权失败，发送 AuthorizationFailureEvent，并抛出鉴权失败异常 AccessDeniedException
        - 鉴权通过后发送 AuthorizedEvent 通知
        - 调用 runAsManager.buildRunAs(authenticated, object, attributes); 根据返回值来决定是否更换执行者信息，默认为 null 不切换 
- afterInvocation


**accessDecisionManager** 的抽象类是 `AbstractAccessDecisionManager`，他主要实现了一个投票机制，以支持多种权限设计模型（RBAC、ABAC、DAC、MAC...）
- 投票管理器有三个默认实现，分别是：
    - `AffirmativeBased`【默认，常用】 一票通过，只要获得任意一个投票者投赞成票即可
    - `UnanimousBased` 【常用】一票反对，只要获得任意一个投票者投反对票则不通过
    - `ConsensusBased` 少数服从多数，获得多数投票者的赞成票才可
    
- 投票者为 `AccessDecisionVoter`，常见的有
    - `RoleVoter<Object>`基于角色的投票器（适用于 用户-角色-权限）
    - `RoleHierarchyVoter<Object>` 基于角色(额外支持继承)的投票器
    - `AuthenticatedVoter<Object>` 基于角色(支持继承)的投票器（适用于 用户-权限）
    - `Jsr250Voter<Object>` 用于支持 JSR250 的投票器（`javax.annotation.security`）
        - `@DeclareRoles` 声明角色
        - `@DenyAll` 拒绝所有角色
        - `@PermitAll` 按照角色授权
        - `@RolesAllowed` 授权所有惧色
        - `@RunAs` 运行模式
    - `ScopeVoter<Object>` 权限范围投票器
    - `ClientScopeVoter<Object>` 客户端权限范围投票器
    - **重要** `WebExpressionVoter<FilterInvocation>` 权限范围投票器
        - vote 方法：在传入的 Collection<ConfigAttribute> 中找到第一个 WebExpressionConfigAttribute
        - 若未找到（为 null），投中立票并返回
        - 通过 expressionHandler（DefaultWebSecurityExpressionHandler） 创建表达式上下文 EvaluationContext
            - createSecurityExpressionRoot 创建 rootObject
                - 设置 authentication、FilterInvocation
                - 设置 PermissionEvaluator（默认总是返回false `DenyAllPermissionEvaluator`），供表达式门面 SecurityExpressionRoot 使用
                - 设置 AuthenticationTrustResolver（AuthenticationTrustResolverImpl），供表达式门面 SecurityExpressionRoot、AuthenticatedVoter 投票、解析异常时调用
                - 设置 RoleHierarchy，角色继承（默认无: null）。供表达式门面 SecurityExpressionRoot、RoleHierarchyVoter 使用。
                - 设置角色表达式前缀，默认 `"ROLE_"`
            - 创建表达式上下文，
                - 使用抽象类默认实现，返回 `new StandardEvaluationContext()`
            - 设置 BeanResolver(`BeanFactoryResolver`) rootObject(上面创建的)
        - 调用 WebExpressionConfigAttribute 的后置处理器，处理 ctx 并返回使用新的表达式上下文
            - WebExpressionConfigAttribute 使用了装饰器模式，会调用 postProcessor 的相同方法（实际为 `RequestVariablesExtractorEvaluationContextPostProcessor`）但由于其未继承，会调用 AbstractVariableEvaluationContextPostProcessor 的方法
                - 返回new `DelegatingEvaluationContext` 的匿名子类 ，重写了 `lookupVariable` 方法，返回值为 null 时，使用 extractVariables(request)，从请求中获取，即：表达式中可以使用请求中相关变量，但优先级低于原有变量解析器
        - 根据 ExpressionUtils.evaluateAsBoolean(weca.getAuthorizeExpression(), ctx) 返回值决定投赞成票还是反对票
        
    - `AbstractAclVoter<MethodInvocation>` 基于角色(支持继承)的投票器
    - `PreInvocationAuthorizationAdviceVoter<MethodInvocation>` 权限范围投票器
    - ... 可以自行实现更多的投票器，比如基于时间、登录地点


大部分鉴权是通过 `beforeInvocation` 前置鉴权处理的，也有部分是通过 `afterInvocation` 的，如有的业务需要根据方法返回值决定是否有权限

## 扩展一种认证方式

> 新增认证方式（面向认证请求）：短信验证码登录

创建类，继承 AbstractSecurityInterceptor，实现认证逻辑

> 新增校验认证凭证方式（面向资源访问请求）：自签 token 授权访问

- 新建类，实现 `AuthenticationSuccessHandler`，主要用于记录客户端已经认证过了，如把token - 认证凭证放到 Map 中
- 新建类，实现 `AbstractSecurityInterceptor`，用于从 AuthenticationSuccessHandler 保存处根据传来的 token，找到对应的认证凭证。

注意：第二步初学者容易遗忘，遗忘时，往往难以排查，甚至会跟源码跟到`SPEL`反射源码（ReflectiveMethodExecutor.execute）时，会发现是鉴权失败，而不是认证失败（因为 Spring Security默认不认识你的 token，使用了默认的 `anonymousUser`，导致无所需权限）

小结：要增加认证方式，需要认证服务器 **可以认证 `申请认证凭证请求`** 、**签发凭证**（、**认证凭证**），还需要资源服务器能够 **认证（认证服务器签发的）凭证**。


Authentication.Detail
- `PreAuthenticatedAuthenticationTokenDeserializer`
- 


## Spring Security 入门总结

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

- [权限系统设计模型分析（DAC，MAC，RBAC，ABAC）](https://www.jianshu.com/p/ce0944b4a903)
- [Spring Security 鉴权简介](https://www.cnblogs.com/fenglan/p/5913387.html)
- [AccessDecisionManager 介绍](https://www.cnblogs.com/chenhonggao/p/9152751.html)
- [Spring Security 内置 Filter](https://blog.csdn.net/qq_35067322/article/details/102690579)
- [Spring Security 优缺点](https://blog.csdn.net/caomiao2006/article/details/51812401)
