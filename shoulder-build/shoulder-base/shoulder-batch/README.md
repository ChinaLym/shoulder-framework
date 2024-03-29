# shoulder-batch

批量处理模块，提供务的异步处理与进度追踪能力，支持集群部署；

并提供开箱即用的常用功能：下载导入模板、导入、校验、异步保存、进度查询、查询导入记录、导出数据 等。

异步导入：

![importUserCase.png](importUserCase.png)

同步导入：

![exportUserCase.png](exportUserCase.png)
## 导入：

* 用户下载CSV导入模板，服务端提供接口
* 用户上传数据文件，服务端安全解析 csv/excel
* 服务端校验解析的数据（提供异步处理实现、存储进度/结果、支持集群模式）
  * 对校验完毕的数据拆分处理，`MapReduce` 分成小任务并发处理（提供异步实现、任务划分、可查进度/结果、支持集群模式）
  * 对已经执行导入的任务记录操作日志
* 用户多次调用查询接口查看校验进度/结果
* 用户调用确认导入接口执行导入
* 用户多次调用查询接口查看导入进度/结果
* 提供查询历史批量处理记录、导入详情的功能
* 支持限制单用户/总体 的 导入频次/线程数
* 支持退出登录后，在进入时若有任务未完成，则继续展示

【注意当导入数据量大时，应先将文件渐进式保存至服务器本地，然后渐进式读取】

## 导出：

* 接收 List<DTO>, outputStream, outputType
* 为导出提供统一接口，并支持大批量数据流式写

---

导入流程解析：

* 用户（csv/excel） --前端--> 后端，解析csv推荐放在前端，后端处理 List<DTO> 即可，对于小型项目，也可放在后端处理。
* 后端收到请求后，返回前端一个 batchId
* 前端收到后使用 batchId 轮询后端校验进度（如每两秒）
* 直到校验完毕/异常失败后，查询校验结果，获取校验失败的/成功的详细记录，提示给用户
* 用户决定是否导入
* 执行导入，前端向后台发起执行导入请求 batchId，后端收到后异步处理，并返回给前端新的 batchId
* 前端收到后使用 batchId 轮询后端导入进度（如每两秒）
* 直到导入完毕/异常失败后，查询导入结果，展示给用户

-----------

## 使用

ExportConfigManager.putConfig 配置模板
下载导入模板
校验
查看失败原因
重新校验
导入
查看导入历史
查看导入详情
导出预览
导出



------------------

## 技术选型

### CSV

 [2018 csv 开源工具比较](https://github.com/uniVocity/csv-parsers-comparison)

- opencsv
- univocity-parsers
```xml
        <!-- https://opencsv.sourceforge.net/ -->
        <dependency>
            <groupId>com.opencsv</groupId>
            <artifactId>opencsv</artifactId>
            <version>4.1</version>
            <optional>true</optional>
        </dependency>

        <!-- https://github.com/uniVocity/univocity-parsers/releases -->
        <dependency>
            <groupId>com.univocity</groupId>
            <artifactId>univocity-parsers</artifactId>
            <version>2.9.0</version>
            <optional>true</optional>
        </dependency>

        <!-- https://github.com/apache/commons-csv/releases -->
        <!--http://commons.apache.org/proper/commons-csv/user-guide.html-->
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-csv</artifactId>
            <version>1.8</version>
        </dependency>

```
### EXCEL


```xml

        <!-- =============================================================== -->

        <!-- https://github.com/alibaba/easyexcel/releases -->
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>easyexcel</artifactId>
            <version>2.2.6</version>
        </dependency>

```


### XML 转换

三个注解

- XStreamAlias 输入/输出使用别名（默认类全路径名/属性名）
- XStreamAsAttribute 作为父标签的属性，而非默认的下级标签
- XStreamImplicit 简化/去除 集合 / List 的外层标签包装
- XStreamOmitField 跳过该字段


```xml
        <!-- xml 转换 https://github.com/x-stream/xstream/releases -->
        <dependency>
            <groupId>com.thoughtworks.xstream</groupId>
            <artifactId>xstream</artifactId>
            <version>1.4.15</version>
        </dependency>
```

其它 xml

xpp3

https://blog.csdn.net/zmx729618/article/details/52787638
