> 该文件通过 cn.itlym:shoulder-archetype-simple 自动创建

# 如何贡献我的源代码

`${appId}` 欢迎各类型的贡献，不限于`优化代码格式`、`优化注释/JavaDoc`、`修复 BUG`、`新增功能`、`流程建议`、`宣传`。

您的任何贡献将成为 `${appId} Commiter`，对于创建 / 回复 / 提交代码活跃的`Commiter`将成为`${appId} Key Person`
，共同主导 `${appId}` 的演进。

# 导航

- [开发前准备](#开发前准备)
- [完成第一次PR](#完成第一次PR)
- [分支管理](#分支管理)
- [版本发布](#版本发布)
- [常见问题FAQ](常见问题FAQ)

# 开发前准备

`${appId}` 是多个标准的 `Spring Boot Starter` 项目（[Spring官方解释什么是 SpringBootStarter]()），使用 `Maven`
作为依赖管理和构建工具，只需要本地安装 `Maven 3.6` 或更高版本即可正常开发代码和单元测试，无需其它的环境准备工作。

可以参考 [工程目录](doc/module-intro.md) 快速理解 `${appId}` 的工程结构。

# 完成第一次PR

需要遵循以下步骤：

- 认领 ISSUE，确认代码改动目的
- 获取代码
- 修改代码
- 合并代码

## 认领 ISSUE

注意：不论您是修复 bug、新增功能或者改进现有功能，在您提交代码之前，请创建 或 评论一个 `Issue`，描述您要修复的问题或者要增加、改进的功能。

目的：说明改动背景，避免与他人重复工作，有利于代码合并请求通过。

## 获取源码

在创建或评论 Issue 后，点击右上角的`Fork`按钮，复制一份主干代码到您的代码仓库。

`Fork` 后，通过 `git clone xx` 或者在页面上下载，将代码下载到本地。

## 修改代码

拉完分支后，就可以修改代码了。

注意项：

- 代码风格保持一致，如 import顺序、空格、换行、注释等
- 补充单元测试代码
- 确保新修改后内容通过所有单元测试。您可以通过 `mvn clean test` 或 `IDEA` 触发所有单元测试
- 若为 bug 修复，希望提供新的单元测试来证明以前的代码存在 bug，且新的代码已经解决了这些 bug
- 确保相关文档也被更新，如一些 `README.MD` 文档

修改好后可以使用 git 提交，以下是几个例子

- `git commit -am 'feat: 添加xx功能'`
- `git commit -am 'bugfix: 修复xx场景下xxx'`
- `git commit -am 'doc: 修改xxx注释，说明文档'`

## 提交代码至远程

执行如下命令提交本地修改到您的远程仓库上：
`git push origin "branchname"`

## 合并代码

建议PR前先 Merge 您的目标分支代码，避免代码冲突。

进入您 `GitHub` 上对应仓库，按右上角的 `pull request` 按钮。选择目标分支，如 `0.8.x`并确认，PR校验 + CodeReview
通过后您的代码就会合入完成并作为`${appId}`的一部分。

---

# 分支管理

![分支管理模型图](doc/img/gitFlow.png)

## 命名策略

同 `Spring Boot`，但 `1.0.0-RELEASE` 之前只有主分支

- 开发分支（开发完毕合并至预发布分支 或 维护分支）
    - 新功能分支命名：
      feature-{issue编号}

    - 缺陷修复分支命名：
      bug-{issue编号}

    - 重构分支命名：
      refactor-{issue编号}

    - 改进分支命名：
      improve-{issue编号}

- 预发布分支（单人开发时，开发分支与预发布分支为同一分支）
  pre-{x.y.z}

- 已发布的版本，维护分支（基于发布分支）：
  {x.y}.x

- 发布 Tag 命名：
  {x.y.z}

---

# 版本发布

> 目前该流程由仓库负责人负责

- **发布前（准备）**
  - 更新依赖版本
  - 更新发行版本号
  - 本地构建、测试
  - CHANGELOG、README 修订或使用工具

- **发布**
  - 推送到`Maven Central`，提交至代码库（git）并触发 CI flow
    - 打TAG，推到 github

- **发布后（清理）**
  - 在社交平台宣布发布一个新版本
  - pom.xml更新到下一个开发版本，推到github
  - 在web页面上更新版本
  - 在GitHub上更新发行说明
  - 关闭/创建里程碑、标签。(TODO 后续使用自动化工具如 spring 开源的 label robot)
  - 分支清理，删掉里程碑内的无用分支，包括开发分支（如：1.5/dev/lym/feature-1），发布分支
    - 在其他频道、社交平台上宣布发行

## 维护分支开发与发布

场景描述：已经发布`1.1`，但发现缺陷，需要发布`1.1.1`修复版本

1. 基于1.1.0分支创建1.1.x的维护分支：maintain/1.1.x，1.1.x版本后续所有维护修改均以该分支为基础开发，停止维护时删除该分支。
2. 在 `maintain/1.1.x` 上将版本修改为 `1.1.1-SNAPSHOT`，修复缺陷
3. 集成测试通过后修改版本为 `1.1.1`
4. 将 `1.1.1` 的jar发布到maven仓库
5. 基于`maintain/1.1.x`分支创建`release/1.1.1`分支
6. 在 `release/1.1.1` 上创建tag `v1.1.1`
7. 删除 `release/1.1.1` 分支

注：

- 当`1.1.1-SNAPSHOT`再出现缺陷时，回到第2步直到7。

参考：[spring security 发布流程](https://github.com/spring-projects/spring-security/wiki/Release-Process)

----

# 常见问题FAQ

#### 部分代码跳过格式化（spring security dsl）
IDEA：settings - Editor - Code Style - enable formatter marks in comments

#### `xxx.propreties` 文件中写入中文后保存乱码？或显示为 unicode 格式？

IDEA 可以为我们自动转化：
- 打开设置 `File -> Settings -> Editor -> File Encodings`
- 勾选 `Transparent native-to-ascii conversion`

#### gpg 发布错误
https://blog.csdn.net/u011781521/article/details/130454386
