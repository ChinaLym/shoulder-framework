package com.example.demo1.controller.log;

import org.shoulder.log.operation.annotation.OperationLog;
import org.shoulder.log.operation.annotation.OperationLogConfig;
import org.shoulder.log.operation.context.OpLogContextHolder;
import org.shoulder.log.operation.logger.impl.AsyncOperationLogger;
import org.shoulder.log.operation.logger.impl.BufferedOperationLogger;
import org.shoulder.log.operation.logger.impl.LogOperationLogger;
import org.shoulder.web.annotation.SkipResponseWrap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 操作日志入门 Demo2 —— 使用类注解 {@link OperationLogConfig} （可选）进一步简化方法注解
 *  使用 @OperationLogConfig 就不用再在每个方法上写 objectType=xxx strategy=xxx
 *
 *  通常情况下一个 Controller / Service 类中不同方法记录操作日志时，他们的操作的对象往往是相同的（模块内聚）
 *  把他们直接放到类上统一维护，就不需要每个方法中都写一遍啦。
 *
 * @author lym
 */
@SkipResponseWrap // 该类所有方法的返回值将不被包装
@OperationLogConfig(objectType = "类注解上的objectType") // 这里多了个 Config 注解！！！！
@RequestMapping("oplog/config")
@RestController
public class OperationLogDemoController2 {


    /**
     * 当 OperationLog 注解中没有 moduleId、ObjectType，值时，会尝试使用类的 OperationLogConfig 中的值
     * <a href="http://localhost:8080/oplog/config/1" />
     */
    @OperationLog(operation = "configTest")
    @GetMapping("1")
    public String test1() {
        // 从日志上下文中拿出日志 DTO，getObjectType 获取类型
        return OpLogContextHolder.getContextOrException().getOperationLog()
                .getObjectType();
    }


    /**
     * 如果 OperationLog 注解中有 ObjectType，值时，优先取方法上的
     * <a href="http://localhost:8080/oplog/config/2" />
     */
    @OperationLog(operation = "configTest", objectType = "方法注解上的objectType")
    @GetMapping("2")
    public String test2() {
        // OpLogContextHolder.getLog() 相当于 OpLogContextHolder.getContextOrException().getOperationLog()
        return OpLogContextHolder.getLog()
                .getObjectType();
    }

    /**
     * 异步记录操作日志
     * <a href="http://localhost:8080/oplog/config/3" />
     *
     * @see LogOperationLogger#doLog 在这里打断点，模拟记录日志耗时，发现是在异步线程记录的，故不会影响接口返回和响应
     * @see AsyncOperationLogger 被该 logger 包装后，将在异步线程中记录
     */
    @OperationLog(operation = "asyncLogger")
    @GetMapping("3")
    public String asyncLogger() {

        OpLogContextHolder.getContextOrException().getOperationLog()
                .addDetailItem("shoulder.log.operation.logger.type 选择记录器，默认为 log，通过日志系统记录")
                .addDetailItem("shoulder.log.operation.logger.async 是否启用异步线程记录，默认 true")
                .addDetailItem("shoulder.log.operation.logger.threadNum 异步记录线程数，默认 1")
                .addDetailItem("shoulder.log.operation.logger.threadName 异步记录线程名，默认 shoulder-async-operation-logger")

                // 一般取 null 或 ''，若需要区分，推荐 null，若不希望管理员（非程序员）在看系统日志时看到不认识的 null，可以改为其他
                .addDetailItem("shoulder.log.operation.nullParamOutput 记录参数时，遇到 null 的变量如何记录，默认 null")

                .addDetailItem("shoulder.log.operation.interceptorOrder 解析当前操作者信息的拦截器在 Spring MVC中的顺序，默认0");


        return "shoulder asyncLogger 异步记录，合理利用多线程并发助力高性能~";
    }

    /**
     * 缓冲日志记录器，记录单条操作日志时，不再立即输出，而是先缓冲，达到一定积累或一定时间后，一起记录，进一步减少因为频繁单次交互而带来的性能影响
     * 当使用数据库 / MQ / HTTP 方式保存操作日志时，使用 buffer 可以在一定程度上减轻承载操作日志服务器的压力（DB\MQ\LogCenter）
     * <a href="http://localhost:8080/oplog/config/4" />
     *
     * @see BufferedOperationLogger 被缓冲日志记录器包装后，单条记录时可能不再立即输出
     */
    @OperationLog(operation = "bufferedLogger")
    @GetMapping("4")
    public String bufferedLogger() {

        OpLogContextHolder.getContextOrException().getOperationLog()
                .addDetailItem("shoulder.log.operation.logger.buffered 是否启用缓冲池。优化频繁记录单条，默认 false，开启后可能无法查看到实时操作日志。场景：需将操作日志直接存数据库，每 0.2s 插入一次数据库 -> 每隔一段时间批量插入数据库。")
                .addDetailItem("shoulder.log.operation.logger.flushInterval buffer 日志记录器，每隔多少秒刷一次，默认 10s")
                .addDetailItem("shoulder.log.operation.logger.flushThreshold 当积攒的 buffer 中日志数达到 flushThreshold 条触发一次批量记录，默认 10")
                .addDetailItem("shoulder.log.operation.logger.perFlushMax 每次批量刷日志最大条数，推荐根据实际情况定制。如存志数据库，则可统计 Mysql单页可以存几条数据，取该值作为单次保存量");

        return "shoulder bufferedLogger 杜绝频繁小数据插入，批量优化助力高性能~";
    }


}
