# 开始学习 Shoulder - 核心能力！

下载本项目

```
git clone https://gitee.com/ChinaLym/shoulder-framework-demo
```

并打开 `demo1` 工程将其在本地运行（默认8080端口）

进入 `com.example.demo1.controller` 目录，打开对应的类，参照类上的注解进行测试与查看。（为了方便初学者快速浏览，在 IDE 中按住 `ctrl` 点击注释内 `url` 即可测试与访问）

建议根据以下的顺序了解 `Shoulder` 的使用

- log   打印日志
- ex    处理异常与错误码
- response  统一响应格式
- convert   枚举参数自动转换
- i18n      多语言翻译
- crypto    加密
- delay     延迟任务

## 功能介绍

**demo1** 工程包含最基本功能的使用，更多优雅的设计期待被探索...

---

## 常见问题

### `xxx.propreties` 文件中写入中文后保存乱码？或显示为 unicode 格式？

IDEA 可以为我们自动转化：
- 打开设置 `File -> Settings -> Editor -> File Encodings`
- 勾选 `Transparent native-to-ascii conversion`