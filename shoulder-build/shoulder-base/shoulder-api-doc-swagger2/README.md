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
