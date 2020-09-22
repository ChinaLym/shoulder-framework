# shoulder-base （基本数据定义包）
Shoulder 框架基础的数据结构定义，但引入不使用不会对你的应用产生任何影响，需要关注配置注入Bean，以及手动引入相关的依赖才可以使用。

在 `Spring Boot` 中使用，推荐使用 `shoulder-starter-xxx`，包含了自动识别环境、自动装配、依赖管理。

这里的包除了核心能力依赖外，可选能力依赖的包必须使用 `<optional>true<optional/>` 以高级使用者提供最小的依赖。
