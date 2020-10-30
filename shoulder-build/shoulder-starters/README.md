# shoulder-starters （快速启动器）

内部各模块类似 `spring-boot-starter`，推荐 `spring-boot` 项目直接使用，减少配置成本。

自动装配模块，提供了 `base` + 所需依赖合适版本，并专门为 `spring-boot` 的环境中提供自动装配。

- shoulder-starter-xxx 如果存在 spring-boot-starter-xxx，则一定包含 spring-boot-xxx，
    如：shoulder-starter-web 包含 spring-boot-starter-web。引入 shoulder-starter-web 后则不需要引入 spring-boot 对应的包。
