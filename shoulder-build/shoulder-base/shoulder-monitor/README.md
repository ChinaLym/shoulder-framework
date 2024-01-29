# Shoulder 监控（Base on Spring Boot 监控解决方案）

让开发者看见~ 监控系统是架构师的眼睛，只有将指标看清，系统才能的更大、更稳定！

`spring boot` 的 `org.springframework.boot.actuate.autoconfigure.metrics` 包下已经提供了很多可以用的监控，无特殊需求可直接使用其提供的。
如：`org.springframework.boot.actuate.autoconfigure.metrics.web.client.HttpClientMetricsAutoConfiguration`

spring boot 2.x 支持的是 `io.micrometer` 

`EnableSpringBootMetricsCollector` 等 `prometheus` 下的不再支持

## 配置

#### `application.properties` 添加配置

- Actuator
management.endpoints.web.exposure.include=*
- Prometheus
~~management.metrics.tags.application=${spring.application.name}~~ 无需添加 已经自动配置

#### `propemtheus.yml` 添加配置
- job_name: 'spring-prometheus'
  metrics_path: '/actuator/prometheus'
  scrape_interval: 10s
  static_configs:
    - targets:
      - host.docker.internal:8081
      
#### Grafana Dashboard 配置

如 [6756](https://grafana.com/grafana/dashboards/6756)

需要修改变量的值：

Dashboard Setting -> Variables，选择相应的变量进行修改，这里修改两个：application 和 instance

- label_values(application)
- label_values(jvm_memory_used_bytes{application="$application"},instance) 


---

代码中使用

## 原生使用

- Gauges（度量）
- Counters（计数器）
- Histograms（直方图数据）
- Meters（TPS计算器）
- Timers（计时器）

### Counter
Counter(计数器)简单64位的计数器，他可以增加（~~和减少~~）。它通常用于记录服务的请求数量、完成的任务数量、错误的发生数量等等，它只具备增量计数能力（只是增加计数）。举个例子：
```java

        //tag必须成对出现，也就是偶数个
		Counter counter = Counter.builder("counter")
				.tag("counter", "counter")
				.description("counter")
				.register(new SimpleMeterRegistry());
		counter.increment();
		counter.increment(2D);
		System.out.println(counter.count());
		System.out.println(counter.measure());
		//全局静态方法
		Metrics.addRegistry(new SimpleMeterRegistry());
		counter = Metrics.counter("counter", "counter", "counter");
		counter.increment(10086D);
		counter.increment(10087D);
		System.out.println(counter.count());
		System.out.println(counter.measure());
```

### Gauge

Gauge(仪表)是一个表示单个数值的度量，最简单的度量类型，只有一个简单的返回值，用来记录一些对象或者事物的瞬时值（突出可变化，瞬息万变）。它可以表示任意地上下移动的数值测量。Gauge
通常用于变动的测量值，如当前的内存使用情况、队列中的消息数量、线程池中线程个数、可使用的资源/锁数量、已经占用的资源数量等

Gauge 关注的度量统计角度是VALUE(值)，它的构建方法中依赖于函数式接口ToDoubleFunction的实例(如例子中的实例方法引用AtomicInteger::get)
和一个依赖于ToDoubleFunction改变自身值的实例(如例子中的AtomicInteger实例)
```java

        AtomicInteger atomicInteger = new AtomicInteger();
		Gauge gauge = Gauge.builder("gauge", atomicInteger, AtomicInteger::get)
				.tag("gauge", "gauge")
				.description("gauge")
				.register(new SimpleMeterRegistry());
		atomicInteger.addAndGet(5);
		System.out.println(gauge.value());
		System.out.println(gauge.measure());
		atomicInteger.decrementAndGet();
		System.out.println(gauge.value());
		System.out.println(gauge.measure());
		//全局静态方法，返回值竟然是依赖值，有点奇怪，暂时不选用
		Metrics.addRegistry(new SimpleMeterRegistry());
		AtomicInteger other = Metrics.gauge("gauge", atomicInteger, AtomicInteger::get);
		
```
###Timer
Timer(计时器)同时测量一个特定的代码逻辑块的调用(执行)速度和它的时间分布。简单来说，就是在调用结束的时间点记录整个调用块执行的总时间，适用于测量短时间执行的事件的耗时分布，例如消息队列消息的消费速率。

Timer的度量统计角度主要包括记录执行的`最大时间`、`总时间`、`平均时间`、`执行完成的总任务数`

```java
    public Timer testTimer() {
    	//我们一般通过建造者模式构建Timer，builder方法的入参用于定义Timer埋点Name
        return Timer.builder("test_timer_point_1")
                //tags用于定义埋点的标签，入参为一个数组。每2个组成一个key-value对
                //这里实际定义了2个tag：disc=test;status=success
                //Builder类中还有一个tag()方法，用于定义单个key-value
                .tags("disc", "test", "status", "success"))
                //用于定义埋点的描述，对统计没有实际意义
                .description("用于Timer埋点测试")
                //用于管理所有类型Point的registry实例
                .register(registry);
    }


```

### Summary
Summary(汇总)用于跟踪事件的分布。它类似于一个计时器，但更一般的情况是，它的大小并不一定是一段时间的测量值。在micrometer中，对应的类是DistributionSummary，它的用法有点像Timer
，**但是记录的值是需要直接指定**，而不是通过测量一个任务的执行时间。举个例子：

Summary的度量统计角度主要包括记录过的数据中的`最大值`、`总数值`、`平均值`和`总次数`。另外，一些度量属性(如`下限`和`上限`)或者`单位`可以自行配置，具体属性的相关内容可以查看`DistributionStatisticConfig`

```java
        DistributionSummary summary = DistributionSummary.builder("summary")
				.tag("summary", "summary")
				.description("summary")
				.register(new SimpleMeterRegistry());
		summary.record(2D);
		summary.record(3D);
		summary.record(4D);
		System.out.println(summary.measure());
		System.out.println(summary.count());
		System.out.println(summary.max());
		System.out.println(summary.mean());
		System.out.println(summary.totalAmount());
```

### DistributionSummary
`DistributionSummary` 是用于跟踪事件的分布情况，有多个指标组成：

- count，事件的个数，聚合指标，如响应的个数
- sum，综合，聚合指标，如响应大小的综合
- histogram，分布，聚合指标，包含le标签用于区分bucket，例如web.response.size.historgram{le=512} = 99,表示响应大小不超过512（Byte)的响应个数是99个。一般有多个bucket，如le=128，le=256，le=512，le=1024,le=+Inf等。 

每个bucket展示为一条时间序列，会得到类似下面的图。

更多：`micrometer-spring-legacy`

---

## 注解使用


```java
// 注入切面
@EnableAspectJAutoProxy
@Configuration
public class PrometheusAspectConfig {

    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    @Bean
    public CountedAspect countedAspect(MeterRegistry registry) {
        return new CountedAspect(registry);
    }
}
```

`io.micrometer:micrometer-core` 中的注解
- `@Timed`
    - 标注在方法上，记录执行耗时
- `@Counted`
    - 标准在方法上，统计一定时间内次数
- `@MethodMetric`
    - 标注在
```java
 @Timed(value = "custom_scheduled_sync_user", extraTags = {"name", "自定义同步用户信息任务"}, description = "自定义定时任务监控")
```

## 自定义监控指标

prometheus.集的指标由`指标名称 metric name`、`标签 label name` 两部分构成,格式:
`<metric name>{<label name>=<lable value>,<label name>=<lable value>....}`

每一个时间序列数据由metric度量指标名称和它的标签labels键值对集合唯一确定。

- 指标名称( metric name):
    - 指标名称用于说明指标的含义
    - 由字母、数值、下划线或冒号(分隔符)组成
    - 注意：冒号保留用于用户定义的录制规则。 它们不应被exporter或直接仪表使用。
- 标签( label):
  - 标签体现指标的维度特征,用于过滤和聚标签名和标签值,形成多种维度的表达。

`abc_efg{n1="xx",n2="xxx",n3="xxx"}`

`abc_efg`是指标名;n1 n2 n3是三个标签(label),也就是三个维度查询语句可以基于这些标签维度进行过滤和聚合。

指标名称( metric name)首先需满足 `prometheus` 指标规苑要求,其次需满足唯一性、反应功能/业务含义

指标格式: `应用标识_业务/功能名称`。业务/功能名称由大小写字母开头包含字母或者数字的字符串构成（小驼峰命名）


----

## 流量分类（标签）

- 正常流量：用户正常访问，有问题会影响用户体验
- 测试流量：压力测试、灰度验证，内部测试，有问题不影响用户体验
- 异常流量：爬虫、攻击等，影响监控数据准确性

异常分类：

- 系统异常：往往是故障，影响某个功能，若出现需要立即响应与修复
- 业务规则：如参数错误，通常是预期内错误，正常业务错误量下一般不需要关注，但若指标异常上升可能是规则不适影响到了真实用户，需要响应并进行风险排除，长期来看需要优化，以提升用户体验
- 用户操作：如：密码错误、幂等，这类异常一定会出现，一般不需要过渡介入处理，但长期可以考虑优化，提升用户体验，降低报错成本。
---

Spring Boot EndPoint

- @EndPoint中的id~~不能使用驼峰法，需要以-分割，以规避大小写不敏感问题~~，新版本里不能用中划线，而是要求驼峰
- @Spring Boot会去扫描@EndPoint注解下的 @ReadOperation, @WriteOperation, @DeleteOperation 注解，分别对应生成Get/Post/Delete的Mapping。
  注解中有个produces参数，可以指定media type, 如：application/json等。

----

参考：

- [spring boot actuator demo](https://www.cnblogs.com/jmcui/p/9820579.html)

- [micrometer.io 官网](http://micrometer.io/docs/concepts#_the_timed_annotation)

- [Prometheus 中文文档](http://www.coderdocument.com/docs/prometheus/v2.14/best_practices/metric_and_label_naming.htm)

- [使用 Prometheus 和 Grafana 监控 Spring Boot 应用](https://blog.csdn.net/u013360850/article/details/106159086)

- [Spring Boot 2.x监控数据可视化(Actuator + Prometheus + Grafana手把手](https://blog.csdn.net/xudc0521/article/details/89916714)

- [实战：micrometer+prometheus+grafana搭建Java程序的监控系统](https://blog.csdn.net/weixin_38569499/article/details/85344317)

- [Spring Boot Metrics监控之Prometheus](http://www.pianshen.com/article/6974270533/)

- [使用 Micrometer 记录 Java 应用性能指标](https://www.ibm.com/developerworks/cn/java/j-using-micrometer-to-record-java-metric/index.html)

- [JVM应用度量框架Micrometer实战](http://www.throwable.club/2018/11/17/jvm-micrometer-prometheus/#)

- [Prometheus+Springboot2.x实用实战——Timer（一）之@Timed初探](https://blog.csdn.net/weixin_42182797/article/details/102614969)

- [基于Prometheus搭建SpringCloud全方位立体监控体系](https://www.cnblogs.com/throwable/p/9346547.html)

- [Prometheus 概念：数据模型、metric类型、任务、实例](https://www.cnblogs.com/zhoujinyi/p/11936865.html)
