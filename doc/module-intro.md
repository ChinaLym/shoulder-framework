# 源码目录指南 & 模块划分

> 标准的 Spring Boot Starter 分模块方式，且模块/目录命名策略也与 Spring Boot相似。

最外层的 shoulder-build 管理了 shoulder 的构建，包含了三个部分

- **shoulder-dependencies** 负责依赖版本管理

- **shoulder-parent** 是 shoulder 所有功能模块的直接父类，负责管理他们的公共依赖、插件配置等，**方便其他模块引入依赖无需关心版本冲突等**。

- **shoulder-build** 包含 shoulder 的代码，其下面又按照用途分为两个主要模块。

    - **shoulder-base**: 基础定义与功能模块，真正实现功能代码，Spring Boot 无关，可在无Spring Boot环境使用。
        - **shoulder-xxx**: xxx模块代码（开发时可以把一个模块当作一个工程）。
        - ...

    - **shoulder-starters**: 带 `Spring Boot` 自动配置的开箱即用模块，并提供 `Shoulder` 功能的默认实现，降低在`Spring Boot`里的使用成本。
      - **shoulder-starter-xxx**: xxx模块的自动配置，供使用者直接引入。
      - ...
