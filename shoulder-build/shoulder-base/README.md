# shoulder-base （基本数据定义包）

Shoulder 基础模块，框架能力代码，无 spring-boot 等重依赖，大部分依赖可选或需要外部提供，不会自动引入，使用时需要注意添加依赖。

引入不使用不会对你的应用产生任何影响，需要手动引入相关的依赖才可以通过编译，Spring 中使用时，需自行配置注入Bean使用。

若在 `Spring Boot` 中使用，推荐使用 `shoulder-starter-xxx`，包含了自动识别环境、自动装配、依赖管理。

---

这里的包除了核心能力依赖（`<scope>provided</scope>`）外，可选能力依赖的包必须使用 `<optional>true<optional/>` ，供高级开发者/非 `spring-boot`用户 提供最小的依赖。
