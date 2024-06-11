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

---

## SpringBoot整合swagger3.X

启动后访问：http://127.0.0.1:8080/swagger-ui/index.html (懒加载模式，第一次访问时会解析所有文档，生成 apidoc.json会比较慢，后续访问会因缓存快很多)

## 基于 OpenApi

https://springdoc.org/

```pom
   <dependency>
      <groupId>org.springdoc</groupId>
      <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
   </dependency>
```


```java

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 接口文档配置：
 * <p><a href="http://127.0.0.1:8080/swagger-ui.html">接口文档-ui页面</a>
 * <p><a href="http://127.0.0.1:8080/v3/api-docs.yaml">接口文档-json形式地址</a>
 * <p><a href="http://127.0.0.1:8080/v3/api-docs.yaml">接口文档-yaml形式地址</a>
 *
 * @author lym
 */
@Configuration
public class SwaggerConfig {
  @Bean
  public OpenAPI openApi() {
    return new OpenAPI()
            .components(new Components()
                    // 可选：安全说明
                    .addSecuritySchemes("Auth-AppName", new SecurityScheme()
                            .type(SecurityScheme.Type.APIKEY)
                            .in(SecurityScheme.In.HEADER)
                            .name("Auth-appId"))
                    // 可选：安全说明
                    .addSecuritySchemes("Auth-Token", new SecurityScheme()
                            .type(SecurityScheme.Type.APIKEY)
                            .in(SecurityScheme.In.HEADER)
                            .name("Auth-Token")))
            .info(new Info().title("SHOULDER-DEMO")
                    .description("SHOULDER-DEMO-接口文档")
                    .version("v1.0"));
  }
}
```

可读性优化：Controller上添加
```java
@Tag(name = "字典管理")
```

可读性优化：RequestMapping 接口上添加
```java
@Operation(summary = "字典删除", security = {@SecurityRequirement(name = "Auth-appId"), @SecurityRequirement(name = "Auth-Token")})
```

开启 API DOC:

```propertis
springdoc.api-docs.enabled=true
springdoc.swagger-ui.enabled=true
```

```yaml
springdoc:
  api-docs:
    enabled: true
  swagger-ui:
    enabled: true
    path: /swagger-ui.html
```

生产环境可考虑关闭 swagger

#### 使用 knife4j 增强 ui

TODO

#### 若引入 SpringSecurity，集成swagger要放行swaggerAPI

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

### swagger 原理

- 定义某些注解（默认激活 jax-rs、swagger(OpenAPI) 定义的注解）
- 程序启动时根据扫描规则扫描所有类，并识别这些注解，在内存中生成一套结构化的数据
- 访问 swagger-ui 时，返回生成好的这些数据

`swagger - Spring Boot` 在步骤二中扩展了扫描 `Spring MVC` 定义的注解，从而可以快速看到文档，而若使用默认注解，相当于补充注释
`swagger-jaxrs2` 扩展了 `JAX-RS`(Java Restful Server标准注解) 中定义的注解


因此，不推荐在生产环境中激活文档，因为这会影响启动速度、并占用一定的内存。

---

### Shoulder 提供的支持

由于 api 本身就是定制的东西，shoulder 只提供技术选型，不应该再封装一遍如何配置，因此仅在应用未注册时，提供默认配置


[Swagger3 注解使用（Open API 3）](https://blog.csdn.net/qq_35425070/article/details/105347336)
[swagger2 -> swagger3 官方教程](https://github.com/swagger-api/swagger-core/wiki/Swagger-2.X---Annotations#quick-annotation-overview)
[SpringBoot整合swagger3.X](https://blog.csdn.net/weixin_42201180/article/details/111588194)

# 相关名词概念解释

谈到API文档，那就绕不开大名鼎鼎的Swagger，但是您是否还听说过：OpenAPI，Springfox，Springdoc？您第一次看到这些脑瓜子是不是嗡嗡的？

## OpenAPI【组织 & 规范】

是一个组织（OpenAPI Initiative），
他们制定了一个如何描述HTTP API的规范（OpenAPI Specification）。
既然是规范，那么谁想实现都可以，只要符合规范即可。

## Swagger【UI实现】

它是SmartBear这个公司的一个开源项目，里面提供了一系列工具，包括著名的 **swagger-ui**。
swagger是早于OpenApi的，某一天swagger将自己的API设计贡献给了OpenApi，然后由OpenApi标准化了。

## [knife4j](https://doc.xiaominfo.com/docs/quick-start)【UI实现】

它是国内一个开源项目，替换swagger ui页面，页面更漂亮[（点这里查看效果）](https://doc.xiaominfo.com/docs/introduction/ui)。

- 并伴有部分额外功能
    - 更漂亮
    - 登录
    - 多语言：更好的支持中文
    - 聚合网关（如系统中存在多个服务，可以将他们的接口聚合在一起，在网关暴露）

## Springfox

是Spring生态的一个开源库，是Swagger与OpenApi规范的具体实现。
我们使用它就可以在spring中生成API文档。以前基本上是行业标准。
目前最新版本可以支持 Swagger2, Swagger3 以及 OpenAPI3 三种格式。
但是其从 2020年7月14号就不再更新了，**不支持springboot3**，所以业界都在不断的转向我们今天要谈论的另一个库Springdoc，*
*新项目就不要用了**。

## Springdoc

算是后起之秀，带着继任Springfox的使命而来。
支持OpenApi规范，支持Springboot3，**我们的新项目都应该使用这个**。


---

参考：

- [springdoc 官网](https://springdoc.org/)
- [Knife4j 官网](https://doc.xiaominfo.com/docs/quick-start)
- [网友介绍](https://zhuanlan.zhihu.com/p/638887405)
