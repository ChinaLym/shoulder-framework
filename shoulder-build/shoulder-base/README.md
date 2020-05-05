# shoulder-base （基本数据定义包）
shoulder 框架基础的数据结构定义，高级功能需要配置注入Bean，以及手动引入相关的依赖才可以使用。

这里的包除了核心能力依赖外，可选能力依赖的包必须使用 `<optional>true<optional/>` 以为使用者提供最小的依赖。

推荐使用者使用 shoulder-starter-xxx。