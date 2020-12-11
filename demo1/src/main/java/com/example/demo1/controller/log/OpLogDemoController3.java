package com.example.demo1.controller.log;

import com.example.demo1.util.MockBusinessOperation;
import org.shoulder.log.operation.annotation.OperationLog;
import org.shoulder.log.operation.annotation.OperationLogConfig;
import org.shoulder.log.operation.context.OpLogContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.Executor;

/**
 * 操作日志-上下文
 *
 * @author lym
 */
@OperationLogConfig(objectType = "shop") //
@RequestMapping("oplog/context")
@RestController
public class OpLogDemoController3 {


    /**
     * 当 OperationLog 注解中没有 moduleId、ObjectType，值时，会尝试使用类的 OperationLogConfig 中的值
     */
    @OperationLog(operation = "contextTest")
    @GetMapping("1")
    public String test1() {
        // 从日志上下文中拿出日志 DTO，getObjectType 获取类型
        return OpLogContextHolder.getContextOrException().getOperationLog().getObjectType();
    }


    /**
     * 如果 OperationLog 注解中有 ObjectType，值时，就不会取类上的
     */
    @OperationLog(operation = "contextTest")
    @GetMapping("2")
    public String test2() {
        return OpLogContextHolder.getContextOrException().getOperationLog().toString();
    }



    // ====================================== 跨线程上下文传递 ============================================


    /**
     * 框架会自动处理注入到 Spring 中的线程池，使其拥有自动传递日志上下文的能力
     */
    @Autowired
    @Qualifier("shoulderThreadPool")
    Executor shoulderThreadPool;


    /**
     * 异步线程中也可以自动获取到父线程中的操作者等日志信息，且会自动清理线程变量 ~
     * 不需要关心线程变量如何跨线程使用
     * <a href="http://localhost:8080/oplog/context/crossThread" />
     */
    @OperationLog(operation = "crossThread")
    @GetMapping("crossThread")
    public String asyncSet() {

        // 随便填充点东西，看看异步线程里能不能看到
        OpLogContextHolder.getContextOrException().getOperationLog()
                .setObjectId("objId")
                .setObjectName("objName");

        shoulderThreadPool.execute(MockBusinessOperation.mockAsyncBusiness());

        // 不同线程之间操作也是线程安全的 ~ 这里修改了，不会影响子线程的日志
        OpLogContextHolder.getContextOrException().getOperationLog()
                .setDetail("这是 Controller 线程的日志");

        // 由于将业务委托给异步线程执行，因此可能不再需要本方法执行完毕后记录操作日志，这里可以选择关闭
        OpLogContextHolder.getContextOrException().setAutoLog(false);

        return "已经开启新线程了。去控制台里等待日志的打印吧~";
    }

    
}
