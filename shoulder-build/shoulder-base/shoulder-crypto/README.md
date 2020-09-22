# shoulder-crypto

常用加解密实现以及通用场景的加密方案

- 对称加密
    目前安全的主流选择只有 AES，因此只支持 AES 
    - AES 128/192/256，以及各个模式，推荐密钥长度：256位以上。
    - ECB：
    - CBC:
    - 
    - 
    
- 非对称加密
    - RSA: 速度较慢；主要用于签名、少量数据的加解密；推荐密钥长度 2048 
    
    - ECC（推荐）: 加解密速度快比 RSA 非常多、且安全性更高。 推荐密钥长度 192 位以上，如ECC/256
    
- 签名、验签，三种均可
    - RSA:   基于 RSA   
    - ECDSA  基于 ECC
    - HmacSha256 
    
- 信息摘要（哈希算法）：
    哈希算法很容易受彩虹表的攻击，因此直接使用时不安全！尤其md5。推荐使用 `SHA256`配合`加盐`/`多次迭代`。
    - MD5 一般仅用于快速比较内容是否相同
    - SHA/256
    - SHA/256 + 随机盐

## 场景算法
- 存储加解密
    - LocalCrypto：适合只有本应用能解密出明文的数据。如密码、密钥等。
- 前后传输加解密
    - AsymmetricCrypto：适合前后传输加密少量数据
    - AsymmetricCrypto + AES：适合前后传输加密较多数据
- 传输加解密
    - `ECDH` （JDK官方表示在未来 **jdk15** 首次提供，`Shoulder` 在 `jdk8` 时就注意到这种高性能的安全算法且应用于实践中了，让秘钥协商性能提升**百倍以上**）


## 加解密方案抉择

> 要提升系统的安全性是需要付出一定成本的（开发周期、性能、依赖），并不是所有系统、所有信息都需要绝对安全的，需要使用者自己权衡

- 前后端传输必须要有 https！
- 加密算法采用（RSA/ECC） + AES
    - 非对称秘钥是为了保护服务端私钥不在网络中传输
    - aes用来提升加解密性能。
- 流程
    - 客户端生成随机数 key、iv 作为aes秘钥、填充偏移量
    - 使用 aes256 key iv 传给服务端
    - 同时，将对称秘钥使用使用服务端公钥加密后经 base64编码后放于请求头中
    - iv base64编码直接放入请求头中
- 服务端使用私钥解密出对称秘钥 key，使用对称秘钥 key 解密数据密文，得到明文


## 选型依据

### 不安全（推荐在只在https中使用）

- 敏感信息无加密、防伪、防篡改等措施（无安全性）
    - 明文传输
    - 密码 hash 或 hash 加盐后传输（因为盐值必须要传输）
- 在 **加密算法不能泄漏/公开** 前提下，通讯双方的所有消息也能保证均是`正确的`、`不可篡改的`、`不可抵赖的`、`可追溯的`，敏感信息是第三方`不可破解的`（严重安全隐患）
    - 私有自定义算法
    - 不能保证秘钥在传输过程中不被泄漏、不被篡改，如直接将 aes 秘钥传输
    - 单一种类的加密算法，如只使用 RSA、AES

不推荐在公网且http协议中使用这种算法，不安全，这种只适用于小型内部系统，或信息敏感度低的系统。


### 非严格意义安全

在**一定前提**下，即使 **加密算法泄漏/公开**，通讯双方的所有消息也能保证均是`正确的`、`不可篡改的`、`不可抵赖的`、`可追溯的`，敏感信息是第三方`不可破解的`。

如 `DH` 秘钥协商算法及其衍生算法。

要保证这类算法的绝对安全只需要确保第一次信息交互的正确性即可，详见下文。

由于确保绝绝对安全只需要保证第一次通信的正确性，代价较低，有相对苛刻破解条件，故一般选用这类【灵活性好、无苛刻使用依赖】的算法，只需要第一次访问在安全网络情况下就好了，如 `Https`


### 绝对安全

即使 **加密算法泄漏/公开**，通讯双方的所有消息也能保证均是`正确的`、`不可篡改的`、`不可抵赖的`、`可追溯的`，敏感信息是第三方`不可破解的`。

而这一关键，就是确保首次通信信息的`正确性`：B系统收到的信息，就是A系统要发送的，即消息**没有被篡改**（允许被窃取、监听）。而这一条件在不可信网络中是不可能直接做到的，需要依赖一些约定才可以！

注意，只要是在不可信网络中传输，`通信双方在无任何约定`，且 `通信只能两者之间` 这两个前提下，所有加密方案均 **不是绝对安全的**，若希望 
**绝对安全**，则需要破除以上两个条件的任意一个

- 如`提前约定`
    - 不需要引入第三方
    - 该方案限制了两个系统必须同时完成且安全范围无法扩展
    - 双方有且仅有双方知道的共同的信息（对称秘钥）
    - 举例：A、B两同一组织开发的系统，拥有相同的共享秘钥（AES算法的秘钥）、相同的算法
- `依赖第三者`
    - 扩展性好，可以扩展至任意系统通信
    - 但必须要保证与第三者之间的通信是绝对安全的
    - 必须通过 `提前约定` 的方式，让所有的通讯参与者知道正确的第三方信息（公钥）
    - 举例：`Https`、`Kerbose`...

这两种方式均是：通过各种手段确保首次通信信息的`正确性`。

由于根本必须要通信双方有共同的信息或一方知晓另一方的公钥信息，系统间会产生依赖，故不推荐在 `toC` 系统中使用，但其可以在内部网络中使用，可以在不使用 `Https` 的前提下，进行高性能的安全通信。

