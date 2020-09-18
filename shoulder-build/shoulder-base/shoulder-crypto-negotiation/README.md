# shoulder-crypto-negotiation

密钥协商算法包，可以实现在不安全的网络中协商密钥，以实现安全传输等。

密钥协商可以采用 DH（基于DSA）、ECDH（基于ECC）

    
 ## 安全传输（ECDH）
 
 使用方式：在传输的参数或返回值DTO上添加 `@Secret` 注解，即可自动实现安全传输，可以通过抓包工具校验。
  支持 RestTemplate、Feign（未完成） 两种
 ```java
// todo 添加传输加密代码示范

```

----

## 设计思路

希望使用者不改变原有习惯仍然通过 `RestTemplate` 发起请求调用，带特定注解的（如 `@Secret`）字段自动加解密

流程整理：
- 请求前确保已经进行过密钥交换
- 最终发送的参数的敏感字段已经被加密
- 接收的响应中敏感字段自动解密
 

## 寻找扩展点

### 加密扩展点探索

跟随 restTemplate 源码，发现可以在 `RestTemplate.HttpEntityRequestCallback.doWithRequest` 代码中，
有一步 `for (HttpMessageConverter<?> messageConverter : getMessageConverters())` 的代码，以责任链的形式
尝试序列化参数，可以在这个 HttpMessageConverter 中获取实际参数，这里修改其加了 `@Secret`注解的字段并覆盖原有。
**但这是一个私有方法**，可以通过继承并改造 `httpEntityCallback()` 方法来实现。

这种思路需要改造的点有：
- 在注入 RestTemplate 时替换原有 MessageConverters，改为支持加密的

改动较小，但需要这时候已经完成加解密的协商才行

---

从上面的结论中，可以看到，基本都是由 `MappingJackson2HttpMessageConverter` 来触发的，因此可以通过 jackson 的扩展点来间接解决。
通过改变其序列化实现来支持

```
@JacksonAnnotationsInside
@JsonSerialize(using = RequestEncryptStdSerializer.class)
@JsonDeserialize(using = RequestDecryptStdDeserializer.class)
```

这种思路需要改造的点有：
- 在自定义注解上添加 Jackson 注解
- 添加序列化和反序列化实现 Serializer，这里实现加解密

实现简单，但这种方案会造成序列化时都会触发，且依赖 `jackson`，若使用者未清楚定义 DTO 等类的职责可能会因json序列化/反序列化意外触发加解密

---
