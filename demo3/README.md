# 开始学习 Shoulder - Spring Security 安全与认证 ！

下载本项目

```
git clone https://gitee.com/ChinaLym/shoulder-framework-demo
```

在自己的数据库，如mysql中建立 `demo_shoulder` 数据库
- 导入建表语句 `tb_user.ddl` 即可

打开 `demo3` 工程，修改 `application.yml` 中数据库配置信息：url、用户名、密码等

将其在本地运行（默认8080端口）

打开 [http://localhost:8080/](http://localhost:8080) 

---

## 功能介绍

建议根据以下的顺序了解 `Shoulder` 的使用，Shoulder-Security 主要包含了 Spring-Security 的基本装配，若需要自定义可以参照框架的方式替换。

**demo3** 工程包含安全与认证的教程
- 学习 Spring-Security 认证逻辑
- 学习如何扩展 Spring-Security 的认证
- 使用灵活的验证码框架 Shoulder-Security-Code
- 学习单点登录SSO、Oauth2协议、JWT、OpenID
- 学习使用 github、QQ 等第三方登录方式认证
- 学习搭建认证服务器、资源服务器


---
### shoulder-starter-auth-session

当后端服务器是面向浏览器的 web 程序时候（如供用户通过浏览器/手机浏览器访问）

引入 `shoulder-starter-auth-session`

```xml
<dependency>
    <groupId>cn.itlym</groupId>
    <artifactId>shoulder-starter-auth-session</artifactId>
</dependency>
```          

对浏览器的额外支持如下：
- 基本的认证页面（登录页面）
- Session 的无效、过期策略
- 认证成功、失败、退出登录的处理
- 默认的待认证请求处理
- 对验证码进行默认配置

且实现了自动完成 spring security 相关的配置、依赖的 bean 注入等，达到开箱即用的效果

以上部分均支持使用者自行替换，如自定义各种页面（如登录、注册、退出等）、各类请求url、请求页面参数、会话过期时间等


### 添加验证码使用、扩展教程

引入

```xml
<dependency>
    <groupId>cn.itlym</groupId>
    <artifactId>shoulder-starter-security-code</artifactId>
</dependency>
```

配置需要检查验证码的请求，默认提供了两种：图片验证码、短信验证码

假如登录(/login)、修改密码请求(/changePwd)需要校验图片验证码，只需配置

`shoulder.security.validate-code.image.urls=/login,/changePwd`

假如手机号登录(`/login/phone`)、修改身份证号(`/changeIdCard`)需要校验短信验证码，只需配置

`shoulder.security.validate-code.sms.urls=/login/phone,/changeIdCard`

当新的业务需要校验验证码时，只需要配一下即可。

- 更多配置:
    - 图片验证码相关配置，如希望修改图片尺寸、字符个数等
        - `org.shoulder.security.code.img.config.ImageCodeProperties` 
    - 短信验证码相关配置，如希望修改短信长度等
        - `org.shoulder.security.code.sms.config.SmsCodeProperties`

扩展说明：
> shoulder 默认提供的两种验证码框架都是基于 `shoulder-security-code` 进行二次开发的，实际业务中可能有更多验证码的设计方式，
使用者可以基于 `shoulder-security-code` 可以快速地定制一套自己的、可扩展性强的验证码框架~


---

### shoulder-starter-auth-token

引入 `shoulder-starter-auth-token` 后，只需要注入自己的 `ClientDetailsService` 即可，shoulder 会自动识别，并根据此来进行认证、授权

与 session 相似的，仍然可以使用 `http://localhost:8080/authentication/form` 来完成认证（登录）

不一样的是：session为生成sessionId，请求时会根据sessionId取用户信息；而token是生成token，请求时，请求头中必须修携带token字段

