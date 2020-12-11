package com.example.demo1.task;

import com.example.demo1.bo.UserInfo;
import com.example.demo1.dto.OperationRecord;
import com.example.demo1.util.MockBusinessOperation;
import lombok.extern.slf4j.Slf4j;
import org.shoulder.log.operation.annotation.OperationLog;
import org.shoulder.log.operation.context.OpLogContextHolder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;
import java.util.concurrent.Executor;

/**
 * 无论是放在 Controller、Service、Scheduled 定时任务，操作日志的用法完全一致
 *
 * @author lym
 */
@Slf4j
//@Component // 去掉注释，重启查看演示
public class OperationLogTask implements InitializingBean {

    // 每 20s 触发一次
    private final String TASK1_SCHEDULED = "0/20 * * * * *";

    /**
     * 模拟设备注册补偿任务。
     */
    @Scheduled(cron = TASK1_SCHEDULED)
    @OperationLog(operation = "task1")
    public void task1() {
        System.out.println("操作日志-定时任务演示 1: 任务开始运行...");

        // 1. 模拟业务 从数据库中取出连续7天活跃的用户
        List<UserInfo> registerFailedDevice = MockBusinessOperation.newRandomUsers(10);

        // 2. 模拟业务 调用优惠券服务，给他们发优惠卷
        List<OperationRecord<UserInfo>> result = MockBusinessOperation.process(registerFailedDevice);

        // 3. 批量填充操作日志-被操作对象
        OpLogContextHolder.getContextOrException().setOperableObjects(result);

        System.out.println("操作日志-定时任务演示 1 结束. ");

        // 执行结束后框架自动记录一条操作日志

    }



    // 每 23s 触发一次
    private final String TASK2_SCHEDULED = "0/23 * * * * *";

    @Autowired
    @Qualifier("shoulderThreadPool")
    Executor shoulderThreadPool;


    /**
     * 定时任务里也一样支持跨线程使用 ~
     */
    @Scheduled(cron = TASK2_SCHEDULED)
    @OperationLog(operation = "task2")
    public void task2() {
        System.out.println("操作日志-定时任务演示 2: 任务开始运行...");

        OpLogContextHolder.getContextOrException().getOperationLog()
                .setObjectId("task async test");
        shoulderThreadPool.execute(MockBusinessOperation.mockAsyncBusiness());

        System.out.println("操作日志-定时任务演示 2 结束");

    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("操作日志-定时任务演示已经开启，如果干扰你学习其他 demo，来 " + getClass().getSimpleName() + " 关闭我~");
    }
}