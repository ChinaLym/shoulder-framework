# 接口文档模块

## 整体方案
- Java 代码生成接口文档/在线预览/测试(Swagger + 任意 swagger-ui)
- 接口 mock 平台：YApi
- 需求分析/效果图 Axure 
- 流程图，时序图 visio、processon

wiki：Confluence

---

为什么不用 swagger3（OpenApi3）?
- 目前大多数开发者更熟悉swagger2
- 主流第三方接口辅助软件（UI、文档、mock服务器）解析支持也是 swagger2居多（swagger2基本都支持，swagger3仅极少数软件支持）
- 如果希望使用 swagger3 则可以使用 `springdoc-openapi` 相当于 `springfox-doc`
 
 
Markdown替代javadoc（需要IDE 插件）
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
