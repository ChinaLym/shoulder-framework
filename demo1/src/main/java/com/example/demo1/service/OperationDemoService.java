package com.example.demo1.service;

import org.shoulder.log.operation.context.OpLogContextHolder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 测试 @Async 异步
 *
 * @author lym
 */
@Service
public class OperationDemoService {


    @Async
    public void asyncTest() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 可以对比 operation 字段是否与 Controller 中传递的相同
        System.out.println("操作日志上下文支持 @Async 自动跨线程测试： asyncTest:" + (OpLogContextHolder.getLog() != null));
    }

}
