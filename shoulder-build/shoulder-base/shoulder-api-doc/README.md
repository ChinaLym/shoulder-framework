# 接口文档模块

## 为什么使用 Swagger

`Swagger` 是基于注解的，低侵入式的 api 文档标记规范。它的组织者是跨语言的 api 规范（`OpenAPI`）
的主要发起者，是编程语言接口文档规范的主要制定者，影响广泛，吸引了一些开发者简化`注解解析`、为其制作`UI界面`，选择规范和主流技术可以为使用者减少大量的宝贵时间，并提供相对优质的体验。

为了支持使用 Swagger 的用户，Shoulder 默认也引入，就像 Spring Boot 同时支持 Jackson 和 Jackson2，两种工具的注解、实现都用了，实际只生效一种。

其他可选：
- [SmartDoc](https://gitee.com/smart-doc-team/smart-doc/wikis/Home)
    - 基于 JavaDoc 的 Api 解析工具，Java原生无入侵
    - 但既然完全无入侵，那解析必然是有限的，在无强文档格式需求场景更简单

## 整体方案
- Java 代码生成接口文档/在线预览/测试(`Swagger` + 任意 `Swagger-UI`)
- 接口展示/测试平台：`YApi`、`swagger-admin`、``、``
- 需求分析/效果图 `Axure`
- 流程图，时序图 `Visio`、`ProcessOn`
- wiki：Confluence

---

### `Swagger2` VS `Swagger3（OpenApi3）`
- 目前大多数开发者尤其国内开发者更熟悉 `Swagger2`
- 主流第三方接口辅助软件（UI、文档、mock服务器）解析支持也是 swagger2居多（swagger2基本都支持，swagger3仅极少数软件支持）
- 如果希望使用 swagger3 则可以使用 `springdoc-openapi` 相当于 `springfox-doc`


### Swaager 文档：`Markdown` 替代 `javadoc`（需要IDE 插件）
 ```xml
<build>
  <plugins>
    <plugin>
      <artifactId>maven-javadoc-plugin</artifactId>
      <version>2.9</version>
      <configuration>
        <doclet>ch.raffael.doclets.pegdown.PegdownDoclet</doclet>
        <docletArtifact>
          <groupId>ch.raffael.pegdown-doclet</groupId>
          <artifactId>pegdown-doclet</artifactId>
          <version>1.1</version>
        </docletArtifact>
        <useStandardDocletOptions>true</useStandardDocletOptions>
      </configuration>
    </plugin>
  </plugins>
</build>
```

---

## SpringBoot整合swagger3.X

启动后访问：http://127.0.0.1:8080/swagger-ui/index.html


使用 knife4j 增强 ui

生成环境关闭swagger
```yaml
# swagger3.0 settings
springfox:
  documentation:
    swagger-ui:
      enabled: true # true放开api文档，false关闭api文档

```

SpringSecurity集成swagger要放行swaggerAPI
```java
@Override
public void configure(WebSecurity web) throws Exception {
    web.ignoring().antMatchers("/swagger-ui/index.html",
            "/swagger-ui/**",
            "/doc.html",
            "/webjars/**",
            // swagger api json
            "/v3/api-docs",
            //用来获取支持的动作
            "/swagger-resources/configuration/ui",
            //用来获取api-docs的URI
            "/swagger-resources",
            //安全选项
            "/swagger-resources/configuration/security",
            "/swagger-resources/**");
}
```

openapi (swagger3)
```java
@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openApi() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("Auth-AppName", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("Auth-AppName"))
                        .addSecuritySchemes("Auth-Token", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("Auth-Token")))
                .info(new Info().title("xxx系统")
                        .description("xxx系统-接口文档")
                        .version("v1.0"));
    }
}
```
举例：Controller上添加
```java
@Tag(name = "字典管理")
```
举例：接口上添加
```java
@Operation(summary = "字典删除", security = {@SecurityRequirement(name = "Auth-appId"), @SecurityRequirement(name = "Auth-Token")})
```
enable API DOC:
springdoc.api-docs.enabled=true
springdoc.swagger-ui.enabled=true
---

### swagger 原理

- 定义某些注解（默认激活 jax-rs、swagger(OpenAPI) 定义的注解）
- 程序启动时根据扫描规则扫描所有类，并识别这些注解，在内存中生成一套结构化的数据
- 访问 swagger-ui 时，返回生成好的这些数据

`swagger - Spring Boot` 在步骤二中扩展了扫描 `Spring MVC` 定义的注解，从而可以快速看到文档，而若使用默认注解，相当于补充注释
`swagger-jaxrs2` 扩展了 `JAX-RS`(java restful server标准注解) 中定义的注解


因此，不推荐在生产环境中激活文档，因为这会影响启动速度、并占用一定的内存。

---

### Shoulder 提供的支持

由于 api 本身就是定制的东西，shoulder 只提供技术选型，不应该再封装一遍如何配置，因此仅在应用未注册时，提供默认配置


[Swagger3 注解使用（Open API 3）](https://blog.csdn.net/qq_35425070/article/details/105347336)
[swagger2 -> swagger3 官方教程](https://github.com/swagger-api/swagger-core/wiki/Swagger-2.X---Annotations#quick-annotation-overview)
[SpringBoot整合swagger3.X](https://blog.csdn.net/weixin_42201180/article/details/111588194)
