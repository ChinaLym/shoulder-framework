# shoulder-web

对外暴露restful http 接口时引入该jar。

能力：
- 依赖管理（自动引入相关 jar）
    - 入参校验
- Controller 快速开发模板
- 入参注解解析（基于HandlerMethodArgumentResolver）
    - 注意同时最多只有一个解析器会生效 HandlerMethodArgumentResolverComposite.getArgumentResolver
- 优雅退出 https://blog.csdn.net/qq_17231297/article/details/117376991
    - 退出前处理完当前的请求
- 全局异常处理
- 全局返回值包装
- 增强的类型转换器 ConversionService
- 全自动字典枚举，动态枚举，JSR 校验支持，核心类型转换，方便的扩展SPI，后台管理页面，开箱即用的查询接口（支持国际化多语言）
- 可扩展的全局安全过滤器
    - xss
- 近地域路由，如根据ip，在请求头中添加特殊标记，暗示期望访问的服务器（域名+页面跳转）
- 可扩展的防表单重复提交拦截器
- 便于调试的 api 访问报告（默认调试时为美观方式、生产环境关闭）

除`全局异常处理`和`全局返回值包装`是扩展的 `spring-web`，其余均为 `Java 标准规范`，不依赖额外jar

过滤器执行顺序一般为：

1. 安全过滤器（csrf/xss/httpMethod/统一字符集等）
2. 认证过滤器（单点登录、Oauth2、Session/Token校验等）
3. 功能/业务过滤器（权限过滤、菜单权限、前端路由等）

拦截器同理

----

## web 安全防护注意事项

// TODO 放到 spec 中 refer

- 黑板名单
- 带参数 scheme: http / https / shoulder... 文件上传 SSRF
    - https://blog.csdn.net/qq_30135181/article/details/52734225
      模板引擎xss 紧急开关 检查；双检查 encode cors allowHost redirect 白名单 ctoken（csrfToken）
    - 在 cookie 当前 host 下种 ctoken，这样其他网站发出同样请求则无效 uriWithFormated
- xss
  -

XXE：

- 未处理，不推荐使用 XML 作为响应 redirect
- CRLF注入（包含 '0' '\r' '\n';最好过滤掉所有控制字符（ASCII码十六进制 0x00~0x1F）；）
- 空地址
- 不合法的URI
- 不被允许的地址
- 不被允许的协议头
- 重定向地址的 host、authority 不同
- 命中白名单
- 允许跳转
- app关闭了重定向检测
- 利用域内跳转进行重定向检测绕过
-

方式:黑白名单，优先级列表

## Shoulder 提供过滤器

> 使用 spring security、shiro 安全框架时，推荐使用安全框架提供的，shoulder这里提供最轻实现

- XssFilter

其他安全过滤器：

csrf
http 慢请求
http method

## Shoulder 提供拦截器

- 业务安全：SessionTokenRepeatSubmitInterceptor（BaseRejectRepeatSubmitInterceptor）
    - 防重复提交
- 框架功能：HttpLocaleInterceptor
    - 自动翻译功能使用优化：从请求中获取语言标识，作为 AppContext 上下文的语言标识

## Spring 内置功能性过滤器一览

- CharacterEncodingFilter
    - 用于设置请求接收和响应的字符集
    - 会自动配置：HttpEncodingAutoConfiguration

- CorsFilter
    - 支持跨域
    - 不会自动配置，需要用户手动注入

- OrderedHiddenHttpMethodFilter（HiddenHttpMethodFilter）
    - 普通浏览器form表单只支持 `GET` 与 `POST` 请求，而 `DELETE`、`PUT` 等 method 并不支持。spring 在接收请求时，
      使用 ` <input type="hidden" name="_method" value="put" />` 的value当做http的method，常用与兼容 RestFul 风格请求
    - 自动装配，`WebMvcAutoConfiguration` 可通过 `spring.mvc.hiddenmethod.filter.enabled=false` 关闭

- OrderedFormContentFilter（FormContentFilter）
    - 普通浏览器form表单只支持 `GET` 与 `POST` 请求，而 `DELETE`、`PUT` 等 method 并不支持。spring 在接收请求时，
      使用 ` <input type="hidden" name="_method" value="put" />` 的value当做http的method，常用与兼容 RestFul 风格请求
    - 自动装配，`WebMvcAutoConfiguration` 可通过 `spring.mvc.hiddenmethod.filter.enabled=false` 关闭

- ForwardedHeaderFilter（[5.1后不再推荐](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/filter/reactive/ForwardedHeaderFilter.html)）
    - 常用于作为请求代理时使用，如 spring cloud gateway 中
    - 未自动装配

- RelativeRedirectFilter
    - 通过RelativeRedirectResponseWrapper包装原Response对象,重写默认的sendRedirect重定向方法改为相对路径重定向
    - 未自动装配

- ShallowEtagHeaderFilter
    - 用于节省带宽，通过支持ETag，给客户端返回 `304`，让浏览器使用缓存。
    - 未自动装配

----

## 参考

- [Spring Filter过滤器工作原理](https://blog.csdn.net/sadlay/article/details/86570411)
- [spring security 标准Filter及其在filter chain的顺序](https://blog.csdn.net/ZYC88888/article/details/86534515)
- [Spring web过滤器-各种filter讲解](https://blog.csdn.net/wei55255555/article/details/80611314)（内容较多）
- 前端相关
    - [apollo 的类 graphql](http://apollographql.com/)
    - [antDesign 文档](https://ant.design/docs/spec/introduce-cn)
    - [知乎问题修改规范](https://www.zhihu.com/question/20414919)
