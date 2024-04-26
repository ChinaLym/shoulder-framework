# 源码目录指南 & 模块划分

> 与 spring、spring-boot 的包模块划分、包命名策略相似（简化使用者学习成本）

最外层的 shoulder-build 管理了 shoulder 的构建，包含了三个部分

- **shoulder-dependencies** 负责依赖版本管理

- **shoulder-parent** 是 shoulder 所有功能模块的直接父类，负责管理他们的公共依赖、插件配置等，**使用者也可以直接继承该模块**。

- **shoulder-build** 包含 shoulder 的代码，其下面又按照用途分为两个模块。

    - **shoulder-base**: 基础定义与功能模块，真正实现功能代码，但使用者一般不会直接引入。
        - **shoulder-xxx**: xxx模块代码（开发时可以把一个模块当作一个工程）。
        - ...

    - **shoulder-starters**: 带 `Spring Boot` 自动配置的开箱即用模块，并提供 `Shoulder` 功能的默认实现，简化使用者上手难度。
      - **shoulder-starter-xxx**: xxx模块的自动配置，供使用者直接引入。
      - ...
