# ${appId}

该工程通过 `shoulder-archetype-simple` 创建

- Shoulder 开源地址：[github](https://github.com/ChinaLym/shoulder-framework)

## 包目录结构

```
groupId.artifactId
    ├─config                 配置类
    ├─constant               常量类
    ├─controller             Controller
    ├─dto                    DTO
    │  ├─param                  入参
    │  └─result                 出参
    ├─enums                  枚举类
    ├─exception              异常
    ├─repository             存储层
    │  └─impl                   存储层实现
    ├─service                业务层
    │  └─impl                   业务层实现
    └─util                   工具类
```

---


---

## 模板属性表
|属性 key | 说明 | 默认值 |
|----|----|----|
| appId | 应用/服务标识 | 使用 `${rootArtifactId}` 值 |
| package | 包路径 | `${groupId}.${appId}` |
| contextPath | 上下文路径 | `${appId}` |
| StartClassName | 启动类名 | ShoulderApplication |
