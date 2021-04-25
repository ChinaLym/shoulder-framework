** 注意：由于 spring security oauth 即将下架，本模块暂不主动维护，请参考替代品**

在文末有两个可考虑备选的项目

# 快速创建授权服务器

提供本系统账号认证 账号密码认证、手机号认证、手机号密码认证【基于Spring Security】 作为Oauth2客户端，支持通过第三方平台登录 如 GitHub、QQ、WeChat、Gitee【基于Spring Security】
作为认证服务器 Oauth2、OIDC、SAML2【基于 Spring Security Oauth2】

## 技术、版本选型

`spring security` 支持 `oAuth2/OpenID` 认证，但不支持自身作为 `授权服务器`。 因此选择 `spring security oauth` 来实现（注意该项目将于2022.5转移至社区维护）。

[新的授权服务器](https://github.com/spring-projects-experimental)

## 说明

默认值（`UserConfig#userDetailsService` 配置的）： 用户名：user 密码：password

默认的端信息（`AuthorizationServerConfiguration#configure(ClientDetailsServiceConfigurer)` 配置的） clientId：demo clientSecret:
secret

#### 一个关键的方法
```
org.springframework.security.oauth2.provider.endpoint.AuthorizationEndpoint.authorize()
```
该方法作为spring security oauth2默认的处理认证请求的方法 `/oauth/authorize`

## 常见错误

### 本地调试时出现 authorization_request_not_found
- 常见原因： 授权服务器和客户端域名相同(如都在以不同端口本地部署，但访问时都输入localhost)。

- 原理：浏览器在请求 auth-client 时，会与 auth-client 建立一个会话，会话标识存储在 cookie 的 sessionId 中。 由于auth-client配置了oauth2登录，所以会告诉浏览器去 auth-server 请求授权。
  浏览器便请求 auth-server ，而 auth-server 的域名也是 localhost，于是浏览器就带着 auth-client 的sessionId去了，
  但auth-server应用找不到这个标识对应的会话，便会为浏览器分配一个新的会话标识，浏览器遵循HTTP协议， 将 localhost 域名下key为 `sessionId` 的 cookie 更新为 auth-server 的，这时就覆盖浏览器访问客户端应用时得到的 sessionId，
  再回到客户端应用时，携带的sessionId将是auth-server生成的，导致认证完成后，客户端应用提示会话失效。

- 解决： 在 host 文件中设置 windows: 系统盘（一般为C）`c:\windows\system32\drivers\etc`
  加入以下

```
127.0.0.1	authServer.com
127.0.0.1	resourceServer.com
```

---

其他开源

[spring-authorization-server](https://github.com/spring-projects-experimental/spring-authorization-server)
[keycloak](https://github.com/keycloak/keycloak)