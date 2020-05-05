# shoulder （肩膀）
如果说我比别人看得更远些,那是因为我站在了巨人的肩上. ——牛顿

![LOGO](doc/img/logo.jpg)

Shoulder 是一款 Java 微服务开发框架，以 Spring 为基础，实现了一些常用功能，提供了业界生产级主流前沿方案与功能完善，且开箱即用。

该框架主要提供了技术指导，和一定的编码规范，如果想知道该框架如何使用，以及希望运行，可以期待 **Shoulder-Platform**(一款使用了 Shoulder 框架的开源平台)

所依赖的技术选型规范：
- 优先为业界标准
- 其次为技术先进且已用于大厂的生产环境，并且容易从原有的主流技术迁移
- 其次绝对主流

如优先采用JSR大于其他框架的定义，数据库采用 TiDB 而非 MySQL（但完全兼容 MySQL的协议，从MySQL迁移过来无需修改任何代码）

版本选择标准:
依赖版本为较新的生产可用的 Release 版本，如小型依赖可直接选用最新发布版，Spring 选用较新发布以保证其他依赖 Spring 的框架（如Spring Cloud Alibaba）可以正常使用。

已经依赖的

Spring 5
Spring Boot 2.2
Spring Cloud H


---

**提供能力**包括但不限于：
加解密方案（AES\RSA\ECC\SHA\MD5）与配套工具以及开箱即用的实现。安全传输方案（ECDH）与配套工具以及HTTP的自动实现。
日志（审计日志、业务日志、追踪日志）方案、简单使用。
异常、错误码。
数据库封装。
认证、授权、验证码框架。
等。。。 


---

### 包模块划分策略

采用与 spring、spring-boot 相似的包模块划分策略

最外层的 shoulder-build 管理了依赖的 spring-boot 的版本

- shoulder-dependencies 是 shoulder 框架的依赖管理包，负责管理依赖的版本

- shoulder-parent 是 shoulder 提供的所有jar的直接父类，它依赖了dependencies，负责管理所有 shoulder 及其公共依赖。
shoulder-parent 中分为 shoulder-base 和 shoulder-starters。他们内部都包含了不同模块。

    - shoulder-base: 基础定义包，里面包含了 shoulder框架 的所有的基础定义包。
        - shoulder-xxx: xxx模块代码（开发时可以把一个模块当作一个工程）。作为 starter 的基础，使用者一般不会直接引入。
        - ...
        
    - shoulder-starters: 某个模块开箱即用的 jar 包。以 shoulder-base 为基础，一般由 shoulder-xxx 以及自动配置的代码组成。
        - shoulder-starter-xxx: xxx模块的自动配置，供使用者直接引入。
        - ...
 
 其他：
 - shoulder-initializer: maven archetype. 用于快速创建 使用shoulder框架的项目。类似 Spring Initializer.

 - shoulder-demos: shoulder 框架功能使用演示


### 规范和约束

本框架定义一些限制性的使用方式，如统一返回值，统一错误码，统一日志格式，统一消息格式等，是为了更好的服务治理而考虑的。

当一个系统不再是单机运行，而是被拆分为多个微服务独立运行时，如果没有一套规范和约束，在服务治理上就会相当困难，因此做了一些必要的约束




