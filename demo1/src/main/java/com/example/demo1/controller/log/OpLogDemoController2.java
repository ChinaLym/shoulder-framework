package com.example.demo1.controller.log;

import org.shoulder.log.operation.annotation.OperationLog;
import org.shoulder.log.operation.annotation.OperationLogConfig;
import org.shoulder.log.operation.context.OpLogContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 操作日志入门 Demo2 —— 使用可选类注解 {@link OperationLogConfig} 进一步简化
 *  使用 @OperationLogConfig 就不用再在每个方法上写 objectType=xxx strategy=xxx
 *
 *  通常情况下一个 Controller / Service 类中不同方法记录操作日志时，他们的操作的对象往往是相同的（模块内聚）
 *  把他们直接放到类上统一维护，就不需要每个方法中都写一遍啦。
 *
 * @author lym
 */
@OperationLogConfig(objectType = "shop") //
@RequestMapping("oplog/config")
@RestController
public class OpLogDemoController2 {


    /**
     * 当 OperationLog 注解中没有 moduleId、ObjectType，值时，会尝试使用类的 OperationLogConfig 中的值
     */
    @OperationLog(operation = "configTest")
    @GetMapping("1")
    public String test1() {
        // 从日志上下文中拿出日志 DTO，getObjectType 获取类型
        return OpLogContextHolder.getContextOrException().getOperationLog().getObjectType();
    }


    /**
     * 如果 OperationLog 注解中有 ObjectType，值时，就不会取类上的
     */
    @OperationLog(operation = "configTest", objectType = "方法注解上的objectType")
    @GetMapping("2")
    public String test2() {
        return OpLogContextHolder.getContextOrException().getOperationLog().toString();
    }

    /** 异步线程中也可以自动获取到父线程中的用户信息，而且会自动清理线程变量 ~ */
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


        return "日志记录的过程并不在当前线程，在 DefaultOperationLogger 里添加断点，会发现接口仍然立即返回，shoulder 不会忘记高性能高并发。";
    }


}
