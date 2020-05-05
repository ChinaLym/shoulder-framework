# 其他信息

分支管理、发布流程、包管理：
借鉴 spring 的模式，并结合其他流行的方式


## 分支管理

### 分支命名

【1.0之前只有主分支】

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

# 发布流程 TODO CICD
- **发布前（准备）**
    - TODO 需要插件
    - 更新依赖版本
    - 更新发行版本号
    - 本地构建、测试
    - CHANGELOG、README修订或使用工具 TODO 根据 commit 自动生成

- **发布**
    - 推送到`Maven Central`，提交至代码库（git）并触发(Jenkins) CICD
    - 打TAG，推到 github

- **发布后（清理）**
    - 在社交平台宣布发布一个新版本
    - pom.xml更新到下一个开发版本，推到github
    - 在web页面上更新版本
    - 在GitHub上更新发行说明
    - 关闭/创建里程碑、标签。TODO spring 开源的 label robot
    - 分支清理，删掉里程碑内的无用分支，包括开发分支，发布分支
    - 在其他频道、社交平台上宣布发行


# 维护分支开发与发布流程
场景描述：已经发布`1.1.0.RELEASE`，但发现缺陷，需要发布`1.1.1.RELEASE`修复版本

1.	基于1.1.0分支创建1.1.x的维护分支：maintain/1.1.x，1.1.x后续所有维护修改均以该分支为基础开发。
2.	maintain/1.1.x 上将版本修改为1.1.1-SNAPSHOT，完成代码开发
3.	基于maintain/1.1.x分支创建release/1.1.1分支，修改版本号为：1.1.1. RELEASE
4.	release/1.1.1 上发布到maven仓库
5.	release/1.1.1 上创建tag：v1.1.1.release

参考：[spring security 发布流程](https://github.com/spring-projects/spring-security/wiki/Release-Process)