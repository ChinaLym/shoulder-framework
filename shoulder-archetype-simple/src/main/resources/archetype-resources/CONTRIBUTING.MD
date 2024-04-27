# 如何贡献我的源代码 TODO 根据自身情况决定删除或修改该文档

## 分支管理

### 分支命名

【`1.0.0-RELEASE` 之前只有主分支】

- 开发分支（开发完毕合并至预发布分支 或 维护分支）
    - 新功能分支命名：
      feature-{issue编号}

    - 缺陷修复分支命名：
      bug-{issue编号}

    - 重构分支命名：
      refactor-{issue编号}

    - 改进分支命名：
      improvement-{issue编号}

- 预发布分支（单人开发时，开发分支与预发布分支为同一分支）
  v{x.y.0}

- 发布分支（同 tag）：
  release-v{x.y.z}

- 维护分支（基于发布分支）：
  maintain-v{x.y.z}

- 创建 Tag（基于发布分支）：
  v{x.y.z}.RELEASE

### 分支管理模型图：

![分支管理模型图](doc/img/gitFlow.png)

---

## 克隆仓库与开发

1. Fork 本仓库
2. 新建 issue/{issue对应编号} 分支
3. 提交代码
4. 新建 Pull Request(建议PR前先 Merge)
5. 等待合并

## 发布流程

TODO 补充你的发布流程

----

## IED实用设置

#### 部分代码跳过格式化（spring security dsl）

IDEA：settings - Editor - Code Style - enable formatter marks in comments

#### `xxx.propreties` 文件中写入中文后保存乱码？或显示为 unicode 格式？

IDEA 可以为我们自动转化：

- 打开设置 `File -> Settings -> Editor -> File Encodings`
- 勾选 `Transparent native-to-ascii conversion`

gpg 发布错误
https://blog.csdn.net/u011781521/article/details/130454386
