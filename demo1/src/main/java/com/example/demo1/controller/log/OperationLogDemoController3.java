package com.example.demo1.controller.log;

import com.example.demo1.bo.UserInfo;
import com.example.demo1.service.OperationDemoService;
import com.example.demo1.util.MockBusinessOperation;
import org.shoulder.core.exception.BaseRuntimeException;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.log.operation.annotation.OperationLog;
import org.shoulder.log.operation.context.OpLogContextHolder;
import org.shoulder.web.annotation.SkipResponseWrap;
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
@SkipResponseWrap // 该类所有方法的返回值将不被包装
@RequestMapping("oplog/context")
@RestController
public class OperationLogDemoController3 {

    /**
     * 添加
     * 演示异常后会自动记录日志
     * 模拟正常逻辑，未抛异常： http://localhost:8080/oplog/context/testFail?fail=false
     * 模拟业务失败，发生异常： http://localhost:8080/oplog/context/testFail?fail=true
     */
    @OperationLog(operation = "test_fail")
    @GetMapping("/testFail")
    public void testFail(Boolean fail) {
        UserInfo userInfo = MockBusinessOperation.newRandomUser();
        OpLogContextHolder.setOperableObject(userInfo);
        if (fail) {
            // 自动记录失败日志，错误码，失败详情：可以看到，失败后，日志中 result:"1"，errorCode:"xxx"
            throw new BaseRuntimeException(CommonErrorCodeEnum.UNKNOWN);
        }
    }


    @Autowired
    private OperationDemoService operationDemoService;

    /**
     * 演示对spring @Async 也支持注解跨线程访问 本线程生成的操作日志
     * 演示： http://localhost:8080/oplog/context/async
     */
    @GetMapping("async")
    @OperationLog(operation = "asyncTest")
    public String async() {
        operationDemoService.asyncTest();
        return "return~";
    }

    /**
     * 框架会自动处理注入到 Spring 中的线程池，使其拥有自动传递日志上下文的能力
     */
    @Autowired
    @Qualifier("shoulderThreadPool")
    Executor shoulderThreadPool;


    /**
     * 演示支持用户自定义的线程池也可自动跨线程，异步线程中也可以自动获取到父线程中的操作者等日志信息，且会自动清理线程变量 ~
     * 不需要关心线程变量如何跨线程使用
     * <a href="http://localhost:8080/oplog/context/crossThread" />
     */
    @OperationLog(operation = "crossThread")
    @GetMapping("crossThread")
    public String crossThread() {
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
