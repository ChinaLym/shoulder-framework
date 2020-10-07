# shoulder-archetype-simple

单模块的 maven 骨架工程，适合微型或需要快速迭代的项目，如课设、毕设等

## 保姆级使用介绍

下面以 IDEA 为例，介绍如何通过 `shoulder-archetype-simple` 快速创建一个引入了`shoulder`的 spring boot web 工程

### 添加 shoulder 的 archetype

第一次使用时需要添加，以后都不用这一步咯

![添加 shoulder 的 archetype](../doc/img/archetype/idea/add.png)


### 基于 shoulder 提供的模板创建 maven 工程

选择shoulder
![选择shoulder](../doc/img/archetype/idea/1.png)

输入 gourpId、artifactId
![输入 gourpId、artifactId](../doc/img/archetype/idea/2.png)

输入覆盖模板的值
![输入覆盖模板的值](../doc/img/archetype/idea/3.png)

完成创建
![完成创建](../doc/img/archetype/idea/4.png)

等待创建完毕
![等待创建完毕](../doc/img/archetype/idea/5.png)
- 如果不设置缓存，这一步因访问maven官网，可能会比较慢，解决方式参考 [IDEA 创建maven工程 create from archetype 很慢](https://blog.csdn.net/qq_35425070/article/details/108958087)
- 创建完毕后，我们需要 reimport maven 依赖，这里我们直接点击自动更新

启动运行
![启动运行](../doc/img/archetype/idea/6.png)

可以访问 DemoController [http://localhost:8080/demo/test](http://localhost:8080/demo/test) 查看一下
