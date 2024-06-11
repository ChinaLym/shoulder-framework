# 🔍问题排查指南

## 👀1.确认问题
**首先** 要确定该能力是否是 `Shoulder` 提供的，不然方向错了，就白忙活啦~ （注：可以通过包路径、类路径来判断~）

`Shoulder` 提供的能力可以参见[功能介绍.md](ability-intro.md)

## 🔥 2.非 Shoulder 问题（主要）

`Shoulder` 自动引入并管理了 `Spring Boot` 的版本，但未屏蔽或改变 `Spring Boot` 用法，一些功能并不是 Shoulder
提供的，Shoulder仅仅是帮您引入咯~
如 `Spring Boot`、`Spring Cloud` 等优秀第三方库为我们提供了大量能力（致敬），使用这些时可以去看他们官方的一手教程，或到搜索引擎搜索对应关键词。如

- shoulder Web 工程中如何使用添加自己的过滤器、拦截器
    - 过滤器、拦截器等的基本功能是 `Spring Boot` 提供的，应该搜索 `Spring Boot 自定义过滤器` 而不是 ~~`Shoulder 自定义过滤器`~~
- shoulder Web 工程中如何访问静态资源文件
    - web 的基础功能是由`Spring Boot` 提供哒，所以这么搜索会更合适哟~ `Spring Boot 如何访问静态资源文件`

## 🤝 3.Shoulder 自身问题

这类问题比较少，若您发现，请自豪得在 `ISSUE` 或 [向 cn_lym@foxmail.com 发送邮件](mailto:cn_lym@foxmail.com)
告诉我们您发现了一个，我们将收集 & 记录 & 确认问题，若需要解决则会尽快修复问题，感谢您的贡献！
