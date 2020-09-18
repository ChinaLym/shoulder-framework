# 开始学习 Shoulder - 安全与认证 ！

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
### shoulder-starter-security-browser

实现这些能力仅仅是引入了 `shoulder-starter-security-browser` 这个 jar

当后端服务器是面向浏览器的 web 程序时候（如浏览器直接访问tomcat），引入这个包

对浏览器的额外支持如下：
- 基本的认证页面（登录页面）
- Session 的无效、过期策略
- 认证成功、失败、退出登录的处理
- 默认的待认证请求处理
- 对验证码进行默认配置

且实现了自动完成 spring security 相关的配置、依赖的 bean 注入等，达到开箱即用的效果

以上部分均支持使用者自行替换，如自定义各种页面（如登录、注册、退出等）、各类请求url、请求页面参数、会话过期时间等


TODO 添加验证码使用、扩展教程