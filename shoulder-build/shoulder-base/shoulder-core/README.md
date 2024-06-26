# shoulder-core

## 基础日志

可以直接使用 `AppLoggers` 定义好的日志
> 基于 Spring 官方推荐方式，Shoulder默认提供了两套配置：

> 当spring.profile=dev，会认为在本地开发阶段，日志输出到控制台，彩色打印，支持点击跳转（无须 idea 日志彩色、日志跳转等插件），且日志内容尽可能想尽

> 当spring.profile=test 或 prod，会认为在服务器测试/部署阶段，日志分类输出到不同文件，高性能打印模式，忽略一些无关紧要的日志（如 DEBUG、TRACE级别日志）

> 日志功能完全支持 spring boot 定义的 logging.xxx 配置。
> Shoulder 总是遵循SpringBoot官方推荐的扩展方式，使用者可以总是只关注 SpringBoot 的扩展能力，对于Shoulder扩展功能的用法几乎都可以去 SpringBoot官网找到。

```java
class LogExample {
    // 简单快速使用
    private Logger log = AppLoggers.APP_DEFAULT;

}
```


## ~~插件机制设计~~
提供加载机制，参考：
https://gitee.com/core-lib/slot-maven-plugin
https://github.com/zlt2000/springs-boot-plugin-test

## 简化异常日志处理

Shoulder 提供了 [shoulder-lombok](https://github.com/ChinaLym/shoulder-lombok)（在 lombok 的基础上扩展了 shoulder 日志注解 `SLog`），使用效果如图

![@SLog示例](img/shoulder-lombok-SLog.png)

- 与 `@Slf4j` 类似，如果类内部已经定义，IDEA也会有提示，且可以通过编译。使用注解将在编译时自动生成类似以下代码

![@SLog实例2](img/shoulder-lombok-Logger.png)

- 日志logger默认变量名为 log，当然也可以修改，若想修改则新建 lombok.config 文件，写入 `lombok.log.fieldName=xxx` xxx 为自己希望的名字，如 logger

也可以搜索 `lombok 修改log变量名`

引入方式
```xml
        <dependency>
            <groupId>cn.itlym</groupId>
            <artifactId>shoulder-lombok</artifactId>
        </dependency>

```

使用方式

```java

@SLog
class LogExample {

}
```

编译后实际代码


```java

class LogExample {
private static final org.shoulder.core.log.Logger log = org.shoulder.core.log.LoggerFactory.getLogger(LogExample.class);

}
```


> 注：IDEA 中自动提示需要安装 [shoulder-lombok-plugins](https://github.com/ChinaLym/lombok-intellij-plugin)


---

## 翻译与多语言

#### Spring 多语言文件命名限制
 由于采用了 jdk 定义的 `ResourceBundle` 的思想加载多语言文件，故对多语言资源文件命名有 `ResourceBundle` 命名限制

#### Spring 的资源文件加载顺序
默认共尝试加载7处
- 按照传入 Locale，从详细到粗略加载一轮
    - 【资源名_语言_地区_变种】
    - 【资源名_语言_地区】
    - 【资源名_语言】
- 使用当前操作系统语言再次尝试加载一轮
    - 【资源名_语言_地区_变种】
    - 【资源名_语言_地区】
    - 【资源名_语言】
- 使用不带语言标识加载一次

翻译场景推荐 注：Thymeleaf、FreeMark 等动态页面由后端翻译，html静态页面或前后分离时推荐由前端翻译
 * 若有大量重复 message 映射时，如多租户，每个租户可以定制自己的界面和提示信息，可采用继承方式简化多语言管理

----

## [GUID（全局唯一标识生成器）](https://github.com/ChinaLym/shoulder-framework/tree/master/shoulder-build/shoulder-base/shoulder-core#guid%E5%85%A8%E5%B1%80%E5%94%AF%E4%B8%80%E6%A0%87%E8%AF%86%E7%AC%A6%E7%94%9F%E6%88%90%E5%99%A8)

全局唯一 id 生成器，接口有 LongGuidGenerator、StringGuidGenerator 两种，常用于生成流水数据的主键、标识字段，经测试分布式场景不会重复，且性能相当高，并容忍时间回拨。

## 各家类雪花算法性能对比

- 参与 pk 算法：`Shoulder-guid`、`百度的 uid-generator`、`美团的 leaf` / `网上流传最广的 "高性能雪花算法Java实现"`

> BTW [滴滴 tinyId](https://github.com/didi/tinyid.git) 仅实现了号段模式，更新号段依赖外部存储（默认 mysql），执行速度远远不如类雪花算法，不做比较。

- 测试目标：比较类雪花算法的生成性能（网上流传版本/美团版本/百度魔改版本）。

- 测试机器：对比环境均为 `3700X` `16G` `DDR4 3000`。

- 测试方式：去掉所有日志、统计等无关代码，仅保留原生代码，生成 1kw 条id，执行10次，非完全精确测试，所有测试均未预热。

### JDK-uuid

`UUID.randomUUID().toString().replace("-", "");` 单线程大概需要 5700ms，8线程需要 6214ms（少量竞争）


### [美团-点评 leaf](https://github.com/Meituan-Dianping/Leaf) / 网上流传的最多`"高性能雪花算法java实现"`的代码

美团开源项目中提供了 twitter 的雪花算法的Java实现，和网上流传的最多`"高性能雪花算法java实现"`的代码基本相同，由于未作预支策略，每秒生成固定为 4096。不支持配置。

不支持时间回拨、生成 1kw 条需要半个多小时。

#### 结论

twitter 首个提出 GUID 的可行方案，该算法容易理解，为进一步了解 GUID 做了基础，最广泛流传的代码最简单，可以作为学习 GUID 算法的第一步。

但其默认无法处理时钟回拨，且其性能有效，即使加上时间预支、使用逻辑时钟，也还需要处理变量同步、变量共享、多变量原子操作，读写冲突等问题，不太推荐大规模生产环境中使用。

### [百度 uid](https://github.com/baidu/uid-generator)

官方号称每秒生成接近 700w，由于我电脑配置好于标注的性能配置，我认为在我电脑上百度算法的性能会更高，在自己电脑测试大概为 `1100w/s`，具体见下。

测试方式：使用官方号称最快的 CachedUidGenerator，去掉所有日志打印代码，去掉spring运行环境，只保留纯原生生成相关代码，序列位与标准雪花对齐，使用12bit

#### 测试 & 结果：

- 单线程运行需要 850-900ms 左右，且有概率因为获取过快出现异常（默认的逻辑，可自行改为阻塞）。

![单线程测试代码](img/guid_baidu.png)

- `8`线程生成时需要 2300-2350ms 左右（由于竞争带来的性能下降）

#### 小结

百度提出了利用逻辑时钟 + 启动时机器号从外界获取不会重复唯一标识来解决时钟回拨，利用双 buffer，去伪共享，异步预填充机制降低并发读写同步带来地性能影响，保证了较高性能，但在超大规模场景还有待提升。

### [ShoulderGuid](https://github.com/ChinaLym/shoulder-framework/blob/master/shoulder-build/shoulder-base/shoulder-core/src/main/java/org/shoulder/core/uuid/ShoulderGuidGenerator.java) （Shoulder Globally Unique Identifier 全局唯一标识符）

测试代码见单元测试，与其他开源实现相比，除了提供`递增`、`唯一`的 `GUID`
算法与文档外，shoulder还有完善的[单元测试（性能、重复测试、扩展测试、多线程测试）](https://github.com/ChinaLym/shoulder-framework/blob/master/shoulder-build/shoulder-base/shoulder-core/src/test/java/org/shoulder/core/uuid/ShoulderGuidTest.java)

#### 测试 & 结果：

![shoulder / 百度 / UUID 性能比较](img/guid-compare.png)


采用标准的雪花算法模式，未选用性能更高的配置

- 1. 单线程运行需要 135ms 左右，性能是百度算法的 **300 %** 倍。
![shoulder-guid 单线程测试](img/guid_single.png)

- 2. 单线程**批量生成**需要 55ms 左右，性能是百度算法的 **1800 %** 倍。
![shoulder-guid 单线程批量获取测试](img/guid_multi.png)

> 为了证明 `Shoulder` 是高性能不是"投机取巧"换来的，特附 `shoulder-guid` 生成id是的`单调递增``无重复`的测试图证明。
> 这 1kw 个id是无重复的，完全单调递增的！您可能 Shoulder-Guid 的高超性能感觉惊讶，没错！学 Shoulder，带您认使高并发！
![shoulder-guid 单线程批量获取测试-无重复-单调递增](img/guid_notRpeat_increasing.png)

- 3. `8`线程生成需要 335-360ms 左右（由于竞争带来的性能下降，CAS导致范围波地较大），性能是百度算法的 **660 %** 倍。
![shoulder-guid 8线程测试](img/guid_single.png)

- 4. `8`线程**批量生成**需要 40ms 左右，性能是百度算法的 **5810 %** 倍。（多线程虽然增加了并发冲突因素，但由于CAS冲突减少，且并发获取，多线程获取整体耗时减少）【精髓】
![shoulder-guid 8线程批量测试](img/guid_multi_threads.png)

Shoulder 提供了**批量生成**方式，并对批量获取id做了额外的算法优化，使用时一般会通过二级缓存调用批量方法，因此主要关注第 2/4次的批量获取测试结果即可。

#### 小结

shoulder-guid 将各个 bit 段支持可扩展，可以根据自己的业务场景变更 guid 格式。

shoulder-guid 利用了时间预支解决了单时间段序列达到上限问题，利用 buffer 缓存解决时钟回拨问题，利用CPU cacheLine 减少内存访问，多个点巧妙利用 CAS 制去掉了锁，实现真正地无锁化、高性能！

采用标准的雪花算法格式与其他实现做对比，而未对bit段参数调优，就已经远超其他实现，实际通过调优还可以翻倍获取更高的性能，至此，`shoulder-guid` 成为当之无愧的 `GUID` 性能王！

#### 扩展

* 在大多数业务场景下，不必过于在意 guid 算法的性能，优化后的号段模式基本够用了，作者更推荐关注各算法对于序列超拿、时钟回拨的处理，可扩展性以及高可用方案。

* 在海量日志分布式入库、大规模事件处理、分布式爬虫数据整理、大数据处理场景推荐使用 `shoulder-guid`，它性能最高、扩展性较好、可自由替换处理时钟回拨策略，留有监控的扩展点。

* 具体采用哪种方案应按照业务量体裁衣，根据不同业务场景，选用不同的生成方式。
** 比如业务上希望id存储空间对计算机更友好，则考虑使用 int32/int64 类型存储，优先考虑号段模式、类雪花算法。
*** 除此之外，还希望能在id中解析出时间戳、高性能，推荐使用雪花算法分布式生成，如日志id，调用链标识。
*** 若不要求id中有时间相关、且希望号段紧密的非敏感业务，可使用号段模式，如系统内部的记录。

* 若业务中希望流水号是随机生成，且保留靓号，可以采用分段预生成+随机存储策略，如用户id。

* 若业务场景中希望流水号更简短，方便客服反馈交流更方便，推荐使用 Base16 / Base 36 / Base 62 编码后使用，显示时分段展示并提供快速复制功能，保证看起来或者读起来更顺畅，如错误码、订单号。


---

# 高级·Shoulder框架开发者须知
## 模块定位

定位：本框架核心能力类定义，模块定位类似 `Spring`中的` spring-core`。

模块内容：异常、枚举、DTO(入参、响应)、日志Logger、多语言、动态字典、模型转换、并发类、通用工具等。

目的：这些类为 `Shoulder` 其他模块能力提供规范定义与约束，以保证`Shoulder`其他各模块遵循统一的规范，更易于维护，同时也为使用者提供一些通用能力。

使用场景，该模块不限制使用场景，可以在无Spring、无WEB的任何java场景用。

## 框架设计理念-统一返回值格式

> 统一接口的返回值可以大大提升多团队协作效率，也是接口复用提效的有力手段。

Shoulder 中 HTTP 接口返回值统一按照以下 JSON 格式 `{"code":"0x00abcd", "msg":"operation msg.", "data":{}}`，另外也推荐在维护大量接口时使用 `Restful` 风格，在海量接口场景，`Restful`风格的接口定义相比传统定义维护起来更简单～

## 框架设计理念-通用日志

> 统一日志格式可以大幅降低运维团队对系统洞察的成本，如服务监控、问题定位、AI 自动分析等。

- 日志：日期格式 `yyyy-MM-dd`
- 日志：支持记录带错误码

- 日志划分：
    - biz 摘要日志，记录一次请求关键信息：时间 / 业务 / 执行方法 / 结果 / 耗时
    - 业务日志 info：记录执行信息
    - 业务日志 warn 记录业务重要关注点，不常触发的兜底逻辑
    - 业务日志 error 记录业务失败
    - 通用 info/warn/error：业务无关代码 / 中间件依赖未分类时默认打印文件（理论都要分类）
    - 通用 perf：记录线程池等表现，用于监控（可选，可以通过 endpoint 提供）
    - integration-info
    - integration-digest
- 模块日志
    - 超时上报的case，配置刷新，关键运行日志()
    - 框架切面执行，耗时，异常、周期运行摘要、限流等
      spring、core、mq、shoulder-common、shoulder-runtime（关键）、stdout.log、log-clean.log、gc.log、参数中心、mq等、sentinel、endpoint、thread、缓存、数据库、机器cpu等监控、tracelog


## 框架设计理念-异常处理：异常、错误码、错误日志

> `错误码`是`异常`定位的有力抓手，而`日志`又是还原现场的必备手段，故谈异常处理时三者不分家，放一起聊。

异常设计规范：响应为 4xx/5xx 类的错误码（如参数错误，下游调用超时）可直接抛出，不强制使用者显示编写代码捕获处理.
> 设计出发点：
> 现有痛点：层次代码 catch Exception打印异常日志，继续上抛异常，这导致**代码片段长**且**大量重复日志打印**，**处理方式不统一**，**日志打印格式不统一**，不利于排查问题&维护代码；
>
> 建议：`Shoulder` 推荐由全局异常处理器识别处理异常，根据异常类和属性自动返回正确的HTTP Status、错误码、打印日志，另外这样做不会限制对新鲜方式接受度低的使用者编码，其仍可自行捕获异常实现更高级的编码，
> 如：调用A服务超时，使用者可以自行捕获 runtimeException 判断错误码是调用第三方超时，可编排流程兜底调用B等。
> 同时，这样做也能减少使用者大量重复编码，将宝贵的精力放在核心逻辑开发上，或更好的生活。

异常设计规范：
- 异常需要带错误码，便于团队开发的场景，遇到异常/报错，可以通过`错误码`查询`错误码中心`
- 使用者可二次开发，定义异常为检查异常，但检查异常实现类需要定义带错误码的异常。
> 综上，错误码，异常不分家，将错误码定义为接口，将异常定义为RuntimeException，并继承错误码接口定义。


### 全局异常处理

虽然`shoulder-core`中无全局异常处理能力，但不排除其他模块（如`shoulder-web`），或二次开发者希望实现，故`shoulder-core`不能随意定义通用异常或照查其他项目。

首先，来看一下开发者在抛异常的编码方式：

1. 定义异常类，throw 该异常，对于编码方式一般有三种：
- 直接抛，不需要填充参数，如 `throw InvokeGptTimeoutRuntimeException();`
  - InvokeGptTimeoutRuntimeException.class 中已经定义了错误码，报错信息，这类非常报错精准，全局异常处理器在处理HTTP 接口返回值时可以非常轻松的拿到错误信息。
- 只填充`msg`（或部分msg），如 `throw ThirdServiceTimeoutException("invoke gpt timeout.");`,  `throw ThirdServiceTimeoutException("gpt.");` + 异常内部根据参数`"gpt"`填充参数
- 填充`msg` + `上级异常`，如 `throw ThirdServiceInvokeFailException("invoke gpt timeout.", e);`，这里上级异常可能是 `SocketTimeoutException`

> 第一种编码最清晰准确，适合小型或通用性强的项目中，如一些工具类lib中，`JDK`,`common-lang`,`hutool-xx`,但在大量业务逻辑场景往往伴随大量错误码，故这样做会发生`类爆炸`问题，导致JVM大量类加载，影响性能，且类多了就会导致代码可读性、可维护性下降。

> 第二种编码已经有一定的通用性了，适合在一些逻辑复杂的模块中使用这类设计，既能一定程度复用，又能较清晰地呈现异常原因，如`加密`模块中定义几类异常。

> 第三种编码方式通常是追求异常处理，可以大幅降低开发者对异常感知程度。减少异常处理的代码，通常在定义在 `框架通用异常` / `应用通用异常`。


一个应用往往有上百个错误码，这显然适不适合定义为异常类的，很多使用者会用枚举来管理错误码, 如定义 `XxxErrorCodeEnum`，在使用错误码时通常会这么写：
```java
throw new ThirdServiceInvokeFailException(XxxErrorCodeEnum.INVOKE_GPT_TIMEOUT.getErrorCode(), XxxErrorCodeEnum.INVOKE_GPT_TIMEOUT.getMsg());
```
但`Shoulder`更建议这么写：
```java
// 减少 import 类，减少冗余代码，减少代码量，提升代码可读性，可维护性
XxxErrorCodeEnum.INVOKE_GPT_TIMEOUT.throwEx();
```
或使用`AssertUtil`工具类：
```java
AssertUtil.notNull(gptResp, XxxErrorCodeEnum.INVOKE_GPT_TIMEOUT);
```


## 工具包

优先 `apache-commons`、其次 Spring 的工具类、`google` 的 `guava`、第三方的，如 `hutools` 等

文档解析工具：

JSON，采用 jackson，比 fastJson 更安全，同时性能基本无差别。

XML，可选依赖：Xstream：API简单，使用方便（这里适合读取配置文件，而非作为传输体请求/响应界别的频繁读写的场景，更快的选型可考虑 `Toplink JAXB` 等）
