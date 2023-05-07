# shoulder-autoconfiguration （自动装配包）

类似 `spring-boot-autoconfiguration`

包含了对框架核心功能的自动装配，框架设计参考 `spring-cloud`，除了核心之外的自动装配代码放置于 `shoulder-starter-xxx` 中

## 应用基本信息

读取配置中对应用的信息装配到 ApplicationInfo 中

## 系统日志

默认使用 logback，输出格式采用 [Shoulder Log Specification](http://spec.itlym.cn/specs/base/log.html) 中约定的格式，使用者无需再配置 `logback.xml`



###实现原理：

 [Spring Boot 日志功能定制](https://docs.spring.io/spring-boot/docs/2.2.2.RELEASE/reference/html/spring-boot-features.html#boot-features-custom-log-configuration)

### 使用

框架提供了默认配置，使用者可以自定义 `spring日志配置`，如logging.level（使用者定义的优先）

默认配置中可以根据 `spring.profiles.active`（debug,test,prod） 来激活不同配置

- 激活test、prod 时日志只打印进日志文件
- 激活debug或其他选项或不配置时日志只默认在控制台输出。

#### 修改日志配置
Spring 提供的配置格式
```properties
logging.level.<包名>=<日志级别>
```

----------------

## 开发框架注意

Properties 中嵌套其他 properties，最好初始 new 一个，避免用户使用不当产生 NPE 注入 controller 时候，返回值必须为 带注解的实现类

如果上下文中存在 WebMvcConfigurationSupport Bean，则不会加载 WebMvcConfigurer，这时最好打印日志，告诉用户，由于自己装配了这个类，导致框架某些能力失效。
