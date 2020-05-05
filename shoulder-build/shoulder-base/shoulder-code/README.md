# 灵活，扩展性强，使用简单的验证码框架核

使用者可以基于这一核心部分轻松实现一个配置灵活，低依赖的验证码框架

- TODO
    - 调用频率保护，如防止同一手机号频发调用短信验证码接口（优先级低，可以通过其他方式实现）

## 基本原理

ValidateCodeSecurityConfig 将 ValidateCodeFilter 注册进 spring 容器

ValidateCodeFilter 作为安全过滤器上的一环，每个请求都会走它的 `doFilterInternal()` 方法来过滤

该方法会根据配置项判定是否要需要检查验证码，

如果未通过校验，则交给

## 设计和实现说明

#### 当一个请求达到 ValidateCodeFilter 后 doFilterInternal 方法做了什么？

- 根据配置确定是否需要校验验证码？
    - 需要校验（如登录认证请求、支付请求等）
        - 根据配置，获取该请求校验方式（如图形验证码、短信验证码等）
        - 从 `ValidateCodeProcessorHolder` 中获取对应类型的验证码处理器
        - 能否找到一个该验证方式对应的处理器？
            - 找到
                - 将请求交给对应的处理器（`ValidateCodeProcessor`）处理
            - 未找到
                - 抛出异常，交给 `AuthenticationFailureHandler` 处理 
    - 不需要校验
        - 放行


#### 类职责说明

**ValidateCodeProcessorHolder**

维护所有的 ValidateCodeProcessor，便于通过 ValidateCodeType 获取对应的处理器


**ValidateCodeProcessor**

验证码处理器，负责验证码的 生成(create)、校验(validate)

`AbstractValidateCodeProcessor` 是他的一个基本实现

- 生成(create)逻辑
    - 通过 ValidateCodeGenerator 生成，然后委托给 ValidateCodeStore 存储
- 校验(validate)逻辑
    -  获取请求中的验证码，从 ValidateCodeStore 获取验证码，进行比对


**ValidateCodeGenerator**

验证码生成器，负责生成验证码DTO `(ValidateCodeDTO)`


**ValidateCodeStore**

验证码存储，负责保存验证码DTO `(ValidateCodeDTO)`


**ValidateCodeDTO**

验证码DTO


## 如何扩展：
- 新建验证码类型 ValidateType
- （调整继承 ValidateCodeDTO）
- 