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

其他：Selma（与MapStruct设计类似，故应该有类似的性能，但由于其相对小众，暂不比较）

## 结论：

单从性能上讲，MapStruct无疑是最佳选择，**JMapper** 性能也不错，但其需要的配置也更多，需要手动完成一些配置，且更新少，社区沉寂。
又考虑到框架是为了方便使用者，降低使用者上手成本，且本框架作为服务端框架，忽略单次开销，**MapStruct**成了最佳的选择，
且其基于注解的处理方式，消除了编译依赖，故 `Shoulder` 推荐使用 **[MapStruct](https://github.com/mapstruct/mapstruct)** 作为bean转换工具。

快速入门：[https://blog.csdn.net/zhige_me/article/details/80699784](https://blog.csdn.net/zhige_me/article/details/80699784)

参考：[https://www.baeldung.com/java-performance-mapping-frameworks](https://www.baeldung.com/java-performance-mapping-frameworks)
[https://segmentfault.com/a/1190000020663215?utm_source=tag-newest](https://segmentfault.com/a/1190000020663215?utm_source=tag-newest)
