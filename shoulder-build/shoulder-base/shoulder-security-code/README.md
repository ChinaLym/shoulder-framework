# 灵活，扩展性强，使用简单的验证码框架核

使用者可以基于这一核心部分轻松实现一个配置灵活，低依赖、松耦合、高性能，非常轻量的验证码框架（仅几十kb）

- TODO
    - 调用频率保护，如防止同一手机号频发调用短信验证码接口（优先级低，可以通过其他方式实现）

## 基本原理

`ValidateCodeSecurityConfig` 将 `ValidateCodeFilter` 注册进 spring 容器

`ValidateCodeFilter` 作为安全过滤器上的一环，每个请求都会走它的 `doFilterInternal()` 方法来过滤

该方法会根据配置项判定是否要需要检查验证码，

如果未通过校验，则交给登陆失败处理器

## 设计和实现说明

#### 当一个请求达到 ValidateCodeFilter 后 doFilterInternal 方法做了什么？

- 根据配置确定是否需要校验验证码？
    - 需要校验（如登录认证请求、支付请求等，该部分支持通过配置文件动态扩展）
        - 根据配置的验证码类型（如图形验证码、短信验证码等，该部分支持扩展和配置）
        - 从 `ValidateCodeProcessorHolder#getProcessor` 中获取对应类型的验证码处理器 `ValidateCodeProcessor`
        - 能否找到一个该验证方式对应的处理器？
            - 找到
                - 将请求交给对应的处理器（`ValidateCodeProcessor`）处理
                    - 校验通过，请求放行
                    - 校验不通过（可能是：验证码错误、验证码过期、验证码不存在等），抛出 `ValidateCodeAuthenticationException`
            - 未找到
                - 抛出 `NoSuchValidateCodeProcessorException` 异常，交给 `AuthenticationFailureHandler` 处理
    - 不需要校验（未在配置文件配置）
        - 放行

#### 类职责说明

**ValidateCodeProcessorHolder**

维护所有的 `ValidateCodeProcessor`，便于通过 `ValidateCodeType` 获取对应的处理器，通过 Spring 的Bean注入实现了简单的 `SPI` 机制

**ValidateCodeProcessor**

验证码处理器，负责验证码的 生成(create)、校验(validate)

`AbstractValidateCodeProcessor` 是他的一个基本实现，进一步抽象了一般情况的验证码的生成和校验逻辑

- 生成(`create`)逻辑
    - 通过 `ValidateCodeGenerator` 生成，然后委托给 `ValidateCodeStore` 存储
- 校验(`validate`)逻辑
    -  获取请求中的验证码，从 `ValidateCodeStore` 获取验证码，进行比对

**ValidateCodeGenerator**

验证码生成器，负责生成验证码DTO `(ValidateCodeDTO)`，其继承了 `ValidateCodeType` 表示负责特定类型验证码的生成（单一职责）

**ValidateCodeType**

验证码类型，用于统一标记类的职责

**ValidateCodeStore**

验证码存储，负责保存验证码DTO `(ValidateCodeDTO)`，如保存在 `Session` 中，为了同时支持实现单机和集群，故定义该接口。


**ValidateCodeDTO**

验证码DTO，保存验证码相关信息，以图形验证码为例：图片高、宽等

## 默认提供的实现与扩展

#### 验证码校验方式

`learn-security-starter-code` 中提供了 `图形验证码（ImageValidateCodeType）`、`短信验证码（SmsValidateCodeType）` 完整流程的实现。

- 扩展：图片验证码默认内容为随机字母与数字，希望修改其图片形式（如改为类似 `1 + 3 = ?` 的算式形式、或是改为拖动拼图等形式）。（有多种实现方式，下仅举例一种）
    - 参考 `ImageCodeProcessor` 实现生成验证码的方法，并注入到 Spring 中
    - 新建 `MyImageCodeProcessor` 继承 `AbstractValidateCodeProcessor` 实现 `validate` 方法，并注入到 Spring 中


- 扩展：新增一个新形式的验证码，如邮件验证码
    - 可参照 `starter` 中的进行扩展
    - 新建邮件验证码类型 `EmailValidateCodeType`，以及对应的处理器 `EmailCodeProcessor`
    - 新建 `EmailCodeGenerator` 或在 `EmailCodeProcessor` 中重写 `create` 方法
    - 将 `EmailCodeProcessor` 注入到 Spring 容器中


#### 浏览器、客户端如何获取验证码

`ValidateCodeController` 提供了用于获取验证码的默认接口，路径为 `/code?type=xxx`

- 扩展：修改获取验证码的 url
    - 添加自己的 `Controller`，可以根据请求参数从 `ValidateCodeProcessorHolder` 中获取

#### 验证码存储，支持单机、集群

提供了 `ValidateCodeStore` 两种实现
- 存储到 `session` 中（默认）
- 存储到 `redis` 中

- 扩展：希望其他存储方式，如 `Ehcache`、`Hazelcast`等
    - 实现 `ValidateCodeStore` 接口，实现 `save`、`get`、`remove` 方法即可

---

## 启发

本验证码校验框架的设计方式仅仅是一种设计思路，展示了高度的可扩展性。

在其他业务场景中，一样可以通过类似的思想来实现可扩展的业务插件。
