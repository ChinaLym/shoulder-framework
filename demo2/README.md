# 开始学习 Shoulder - 数据库访问！

下载本项目

```
git clone https://gitee.com/ChinaLym/shoulder-framework-demo
```

在自己的数据库，如mysql中建立 `demo_shoulder` 数据库
- 导入建表语句 `demo_shoulder.ddl` 即可

打开 `demo2` 工程，修改 `application.yml` 中数据库配置信息：url、用户名、密码等

将其在本地运行（默认8080端口）

进入 `com.example.demo2.controller` 目录，打开对应的类，参照类上的注解进行测试与查看。（为了方便初学者快速浏览，在 IDE 中按住 `ctrl` 点击注释内 `url` 即可测试与访问）

建议根据以下的顺序了解 `Shoulder` 的使用

## 功能介绍

**demo2** 工程包含数据库快捷开发，该部分提供了Mybatis、JPA的简单增强

- Mybatis 方式：
    - 引入了 `Mybatis Plus`，具体使用方法可以查看 mybatis-plus 官网
    - TODO 使用方式
- JPA：
    - 提供了常用的类型转换，如 varchar(jsonStr) 与 list、set、枚举类型转换

- 全局锁（分布式锁-基于数据库）

- 批处理
    快速实现一些业务的批量处理 / 导入 / 导出