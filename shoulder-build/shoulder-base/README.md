# shoulder-base （基本数据定义包）

Shoulder 基础模块，框架能力代码，非常薄的代码层，无 spring-boot 等依赖，大部分依赖可选或需要外部提供，不会自动引入。

使用者的`pom.xml`直接引入后，不会对应用运行产生任何影响，需要手动引入相关的依赖才可以通过编译，Spring 中使用时，需自行配置注入Bean使用，使用太过于灵活，**不建议**普通使用者直接引入这里的模块。

若在 `Spring Boot` 中使用，推荐使用 `shoulder-starter-xxx`，`starter` 中包含了自动识别环境、自动装配、依赖管理，不会有依赖版本冲突、依赖缺失、功能不生效等问题。

---

# Shoulder 开发注意事项：

这里的包除了核心能力依赖（`<scope>provided</scope>`）外，可选能力依赖的包必须使用 `<optional>true<optional/>` ，供高级开发者/非 `spring-boot`用户 提供最小的依赖。
