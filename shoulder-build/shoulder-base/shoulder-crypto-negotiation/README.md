# shoulder-crypto-negotiation

密钥协商算法包，可以实现在不安全的网络中协商密钥，以实现安全传输等。

密钥协商可以采用 DH（基于DSA）、**ECDH（基于ECC）**

> 因需要出口海外的软件，必须符合国际安全算法规范，shoulder 默认使用美国国家安全局提倡的高性能、银行级安全的 `ECC 256`(算法强度 > `RSA 3072`)
> 若国内特殊项目需要符合国产化需求，仅注入非对称算法Bean即可（`SM2`基本等于`ECC 256`，仅曲线参数不同）
> 另：对称加密默认为 AES(128/192/256随机长度 - CBC) ，若需要国产化，同样仅注入对应的 `SM2` 算法即可（相当于 `AES 128`）
    
## 安全传输（ECDH）
 
 使用方式：在传输的参数或返回值DTO上添加 `@Secret` 注解，即可自动实现安全传输，可以通过抓包工具校验。
  支持 RestTemplate、Feign（未完成） 两种
  
## 使用介绍

见 [demo5](https://gitee.com/ChinaLym/shoulder-framework-demo)

 ```java
// -------------------- DTO --------------------
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiParam {

    String text;

    @RequestSecret
    String cipher;

}

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResult {

    String text;

    @ResponseSecret
    String cipher;

}

// ---------------- 发请求 --------

class SendRequest {

    @Autowired
    private SecurityRestTemplate restTemplate;

    public void sendRequest(){
        restTemplate....
    }

}


// ---------------- 提供接口 -------------

    /**
     * 测试直接请求加密接口  <a href="http://localhost:80/cipherapi/receive"/>
     */
    @PostMapping("receive")
    public ApiResult receive(@RequestBody ApiParam param) {
        System.out.println(param);
        ApiResult result = new ApiResult();
        result.setCipher("shoulder");
        result.setText("666");
        return result;
    }

```

----

## 设计思路

希望使用者不改变原有习惯仍然通过 `RestTemplate` 发起请求调用，带特定注解的（如 `@Secret`）字段自动加解密

流程整理：
- 请求前确保已经进行过密钥交换
- 最终发送的参数的敏感字段已经被加密
- 接收的响应中敏感字段自动解密
 

## RestTemplate 自动加密扩展点探索

- 基于 Spring 提供的 RestTemplate 扩展点来实现整个加密流程 **【shoulder实现】**
    - 实现较为复杂，对希望了解 shoulder 原理的开发者增加了阅读门槛，要求阅读者有一定的技术功底。
- 依据 RestTemplate 扩展点实现部分逻辑，加解密通过 jackson 提供的注解以及扩展机制实现。
    - 无加解密环境下，可能导致序列化时失败，给使用者造成困扰

#### 方式一 【采用】

跟随 restTemplate 源码，发现可以在 `RestTemplate.HttpEntityRequestCallback.doWithRequest` 代码中，
有一步 `for (HttpMessageConverter<?> messageConverter : getMessageConverters())` 的代码，以责任链的形式
尝试序列化参数，可以在这个 HttpMessageConverter 中获取实际参数，这里修改其加了 `@Secret`注解的字段并覆盖原有。
**但这是一个私有方法**，可以通过继承并改造 `httpEntityCallback()` 方法来实现。

这种思路需要改造的点有：
- 在注入 RestTemplate 时替换原有 MessageConverters，改为支持加密的

改动较小，但需要这时候已经完成加解密的协商才行，且该扩展点或之前可在 `doExecute` 中拿到请求的url，来识别目标服务。**注意这种方式只支持带请求体的类型，如** `POST`

流程：

- doExecute
    - 是密钥交换接口（放线程变量）
        - 取消所有增强
    - 否则请求前交换密钥
        - 尝试调用交换密钥接口协商密钥
        - 若请求中带有敏感数据
            - 生成请求加密器
            - SensitiveDateEncryptMessageConverter 负责加密数据，并将原始数据缓存（于线程变量），防止密钥过期
            - 添加加密相关请求头
    - 【发送请求】
    - todo 响应密钥交换失败错误（http 状态码 401，错误码为对方的密钥过期错误码），则尝试重新发起密钥交换，并再次调用 doExecute()
    - 请求正确返回（200）则需要解析响应，并对其进行解密
    
所有需要做的事情
- 客户端
    - 发送前确保已经进行密钥交换，若未进行密钥交换则尝试交换，自带重试（TODO 接口失败则仅一次）
    - 发送前根据密钥结果创建 `请求加密处理器`
    - 发送前根据 `请求加密处理器` 加密参数（管理秘密参数）
    - 【发送请求】（服务端处理）
    - 接收响应前在拦截器中根据响应的 Header 信息
        - 存在强制重新协商标识（服务器密钥协商结果缓存丢失）：
            - 删除与目标服务的相关密钥交换缓存，重新执行**整个流程**
        - 不存在强制重新协商标识（正常流程）：
            - 创建 `响应解密处理器`
    - 接收响应前根据 `响应解密处理器` 解密返回值（管理秘密响应）
    - 清理本次请求的线程变量
- 服务端
    - `SensitiveApiInterceptor` 拦截带 `@ResponseSecret` 的注解，若请求不符合格式要求，则拒绝处理
        - 调用者未按照规定传输
            - 请求头中未携带 `xSecurityId`
            - 请求头中未正确携带真实密钥密文信息 `xDk`
            - 请求头中未携带 `Token`，因无法确保 xDk 未被篡改故拒绝
        - xSecurityId 不正确
            - xSecurityId 不正确/不存在，可能是 xSecurityId 对应的密钥对刚好过期或本服务重启丢失，需要对方重新握手，返回特定响应头标识
        - Token 不正确
            - 很可能是被篡改或请求过期，如token可能仅 5 分钟有效，可能是被抓包，也可能是两方服务器时钟差过大，还可能是客户端错误地使用了缓存
        - xDk 不正确
            - 无法解密出 xDk，不该出现地问题，可能遭到攻击，故拒绝
    - 若通过上述检测，则根据请求头中的信息生成 `请求解密处理器`
    - 根据 `请求解密处理器` 解密请求参数
    - 【api 接口处理】
    - 若返回值中携带敏感信息，返回前重写响应
        - 创建 `响应加密处理器`
        - 根据 `响应加密处理器` 和注解加密返回值


---

从上面的结论中，可以看到，基本都是由 `MappingJackson2HttpMessageConverter` 来触发的，因此可以通过 jackson 的扩展点来间接解决。
通过改变其序列化实现来支持

#### 方式二

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

拓展：

- consultation 和 negotiation 和 discussion 有什么区别？
    - consultation: 偏正式场所，正式协商或讨论的行动或过程，尤至诊所。
    - negotiation: 谈判，谈论目标，且产生`双方都认可的结果`(`agreement`)，如砍价。
    - discussion: 商讨，说出每个人不同的的观点、想法，如开会讨论、探讨。

- 为什么一些地方也叫 key agreement、key negotiation？
    - key agreement，协商的密钥，尤其至协商完毕的共识密钥。
    - key negotiation，密钥协商的过程。


