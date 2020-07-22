# shoulder-rest-server

对外暴露restful http 接口时引入该jar。

能力：
- 依赖管理（自动引入相关 jar）
    - 入参校验
- 全局异常处理
- 全局返回值包装
- 可扩展的全局安全过滤器
    - xss
- 可扩展的防表单重复提交拦截器

除`全局异常处理`和`全局返回值包装`是扩展的 `spring-web`，其余均为 `Java 标准规范`，不依赖额外jar
