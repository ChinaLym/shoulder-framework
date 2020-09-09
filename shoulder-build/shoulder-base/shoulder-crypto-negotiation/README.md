# shoulder-crypto-negotiation

密钥协商算法包，可以实现在不安全的网络中协商密钥，以实现安全传输等。

密钥协商可以采用 DH（基于DSA）、ECDH（基于ECC）

    
 ## 安全传输（ECDH）
 
 使用方式：在传输的参数或返回值DTO上添加 `@Secret` 注解，即可自动实现安全传输，可以通过抓包工具校验。
  支持 RestTemplate、Feign（未完成） 两种
 ```java
// todo 添加传输加密代码示范

```
