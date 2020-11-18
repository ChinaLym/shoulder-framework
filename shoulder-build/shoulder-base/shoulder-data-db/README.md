# shoulder-data
数据库相关支撑
数据库-枚举值转换

## JPA or MyBatis ?

如果你两个都熟悉，建议选型参照以下建议：

优先 JPA
- 越简单、小型项目越选用JPA，如不到十万用户、几千并发
- 搞 DDD 的，底层数据库可能切换的
- 连接查询少的，去 Join、拆微服务做的好的（互联网应用）

优先 Mybatis
- 团队技术限制
- 连表查询较多、复杂查询多、SQL 经常需要专人优化（传统复杂业务应用）


## Let MyBatis Easier

Mybatis 与 JPA 相比最大的缺点就是简单 SQL 还需要自己写，其实可以使用业界已有的方案

* `MyBatis Generator`
    ** 只是根据数据库生产 Java、Mybatis 代码以及常用方法，不做任何封装。  
* `MyBatis Plus`  
    ** 自己封装了大量的实现，定义了一些扩展点。
* `tkMyBatis`
    ** 优先采用业界已有的，命名与 JPA 贴近，使用方式与 JPA 相近。

#### 其他工具
* ideal 的 easycode 插件
* mybatiscodehelper pro 插件

#### 比较

* `MBG`(`MyBatis Generator`) 是简单的生成代码，没有封装，生成后使用的仍然是原生 Mybatis，且无任何依赖。
* `MyBatis Plus` 与 `tkMyBatis` 都在原生 Mybatis 基础上做了自己的封装，不同的是`MyBatis 
Plus`自己定义了一套规范，功能比较齐全，近几年社区越来越活跃，依赖较重，使用时需要了解他的设计。`tkMyBatis`更成熟，且优先采用已有成熟开源并集成，依赖较轻。

#### 结论

希望最轻依赖，使用 `MyBatis Generator`、代码生成器、IDE 插件、Maven Plugin 等形式的。

希望较轻依赖，又要有一定封装，推荐使用 `tkMyBatis`

希望较完善的解决，使用 `MyBatis Plus`（可能导致重度依赖）。

希望完善的解决方案：`JPA`

若希望可以高度灵活定制某个 Service，推荐使用生成器方式，而非继承等封装。




---
## Spring 事务

1. 简述
        在声明式的事务处理中，要配置一个切面，其中就用到了propagation，表示打算对这些方法怎么使用事务，是用还是不用，其中propagation有七种配置，REQUIRED、SUPPORTS、MANDATORY、REQUIRES_NEW、NOT_SUPPORTED、NEVER、NESTED。默认是REQUIRED。
 
2. Spring中七种Propagation类的事务属性详解：
 
 REQUIRED：支持当前事务，如果当前没有事务，就新建一个事务。这是最常见的选择。 

 SUPPORTS：支持当前事务，如果当前没有事务，就以非事务方式执行。 

 MANDATORY：支持当前事务，如果当前没有事务，就抛出异常。 

 REQUIRES_NEW：新建事务，如果当前存在事务，把当前事务挂起。 

 NOT_SUPPORTED：以非事务方式执行操作，如果当前存在事务，就把当前事务挂起。 

 NEVER：以非事务方式执行，如果当前存在事务，则抛出异常。 

 NESTED：支持当前事务，如果当前事务存在，则执行一个嵌套事务，如果当前没有事务，就新建一个事务。
