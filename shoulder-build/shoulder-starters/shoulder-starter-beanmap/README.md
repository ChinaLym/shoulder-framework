# shoulder-starter-beanmap （bean 属性自动映射）

如 bean 的属性复制，DTO，BO 之间转换，该包未作二次封装，仅做`技术选型`和`版本控制`。

## 技术选型

[Dozer](http://dozer.sourceforge.net/documentation/gettingstarted.html)
- [https://www.baeldung.com/dozer](https://www.baeldung.com/dozer)
- 支持自动映射，支持xml、api、注解配置，**性能最差**
- 单次运行很差

---

**性能中等**（较上一层十倍性能提升）

[ModelMapper](http://modelmapper.org/)
- 配置中等。

[Orika](https://orika-mapper.github.io/orika-docs/)
- Orika 的工作原理与 Dozer 相似。但 Orika 使用字节码，性能较高。配置中等。
- [https://www.baeldung.com/orika-mapping](https://www.baeldung.com/orika-mapping)


---

**高性能**（较上一层百倍性能提升）

**[JMapper](https://github.com/jmapper-framework/jmapper-core/wiki)**

- 配置更多（配置更灵活，但更繁琐）
- 单次运行最快

[MapStruct](https://github.com/mapstruct/mapstruct)

- [官网](https://mapstruct.org/)
- [https://www.baeldung.com/mapstruct](https://www.baeldung.com/mapstruct)
- 单次运行较差
- 原理为 JAP （与 `lombok` 类似）

**Spring ConversionService**

- Spring 官方提供接口，可以据此实现灵活的类转换，本质上是手写get/set，不存在反射等消耗性能操作
- 需要手动写每个字段的映射，当然这也规避了字段名变更带来的潜在未赋值bug（现在IDE各类自动生成set代码插件，已经将其代码量较多的弊端较好的解决）
- 需要依赖 Spring 上下文，静态类不太友好，需要手动new ConversionService

其他：Selma（与MapStruct设计类似，故应该有类似的性能，但由于其相对小众，暂不比较）

## 结论：

1. 单从性能上讲，**MapStruct** 无疑是最佳选择；
   - **JMapper** 性能也不错，但其需要的配置也更多，需要手动完成一些配置，且更新少，社区沉寂。
2. **MapStruct** 消除了编译依赖，除了注解不需要引入，第三方类，一定程度更利于维护。
3. 考虑到框架是为了方便使用者，降低使用者上手成本，**MapStruct** 其字段映射同名赋值，约定大于配置 + 基于注解的处理方式（类似于 spring-boot习惯）故上手成本低，也是高优先的选择。
4. 通常情况讲，**MapStruct** 无疑是新/小项目极好的选择。
5. 考虑到业务不稳定、多人维护、代码量大的工程时，**Spring ConversionService** 因其基于Java原生语法，可读性强，字段改名不容易出bug，有更低的维护成本，对易变、中大项目更有价值。


推荐使用 **[MapStruct](https://github.com/mapstruct/mapstruct)** 作为bean转换工具。

快速入门：[https://blog.csdn.net/zhige_me/article/details/80699784](https://blog.csdn.net/zhige_me/article/details/80699784)

参考：[https://www.baeldung.com/java-performance-mapping-frameworks](https://www.baeldung.com/java-performance-mapping-frameworks)
[https://segmentfault.com/a/1190000020663215?utm_source=tag-newest](https://segmentfault.com/a/1190000020663215?utm_source=tag-newest)

---

## 0.7 版本补充：

该模块即将废弃，但文档将保留：
1. 框架内因依赖 Spring，为更好的可读、可维护、可靠性，减少第三方依赖优先使用Spring的 `ConversionService` 手动映射
2. 该模块只是 mapstruct 的依赖，无实际代码，使用者可根据自己的选择自行决策是否引入 mapStruct。
3. shoulder 框架目的是让使用者更容易写代码，间接帮助使用者学习其他优秀的技术解决方案，而不希望即使低二次封装的技术，使用者也只是了解 shoulder，不了解背后的技术
