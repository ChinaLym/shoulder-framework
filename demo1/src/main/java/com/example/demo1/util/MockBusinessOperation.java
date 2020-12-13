package com.example.demo1.util;

import com.example.demo1.bo.UserInfo;
import com.example.demo1.dto.OperationRecord;
import org.shoulder.log.operation.context.OpLogContext;
import org.shoulder.log.operation.context.OpLogContextHolder;
import org.shoulder.log.operation.context.OperationLogFactory;
import org.shoulder.log.operation.dto.OperationLogDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * 生成假数据，模拟实际开发中的业务数据
 *
 * @author lym
 */
public class MockBusinessOperation {


    /**
     * 随机 xx 条失败。模拟各种业务
     */
    public static List<OperationRecord<UserInfo>> process(List<UserInfo> userInfoList) {
        Random random = new Random();

        List<OperationRecord<UserInfo>> recordList = new ArrayList<>(userInfoList.size());
        userInfoList.forEach(userInfo -> {
                    OperationRecord<UserInfo> operateRecord = new OperationRecord<>(userInfo);
                    // 随机放一个结果
                    operateRecord.setSuccess(random.nextInt(10) > 3);
                    recordList.add(operateRecord);
                }
        );
        return recordList;
    }

    /**
     * 随机生成 num 个
     */
    public static List<UserInfo> newRandomUsers(int num) {
        List<UserInfo> result = new ArrayList<>(num);
        for (int i = 0; i < num; i++) {
            result.add(newRandomUser());
        }
        return result;
    }


    public static UserInfo newRandomUser() {
        UserInfo userInfo = new UserInfo();
        return userInfo.setId(UUID.randomUUID().toString())
                .setName(UUID.randomUUID().toString());
    }


    /**
     * 模拟异步处理业务
     *
     * @return 返回一个演示用的 runnable
     */
    public static Runnable mockAsyncBusiness() {
        return () -> {
            System.out.println("---- 异步线程测试： 业务处理需要 5s，之后记录操作日志");
            try {
                // 业务处理需要 5s
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            OpLogContext opLogContext = OpLogContextHolder.getContextOrException();

            OperationLogDTO oplog = opLogContext.getOperationLog();

            System.out.println("---- 异步线程也可以继续操作上个线程中的 OperationLogDTO 对象~ ---- 且线程安全");
            oplog.setDetail("这是异步线程的日志");

            System.out.println("---- 用户信息也可以跨线程~ 新线程里创建日志一样有用户信息 ----");
            OperationLogDTO testCreateNew = OperationLogFactory.create("async thread create new log");
            System.out.println(testCreateNew.getUserId());
            System.out.println(testCreateNew.getUserName());
        };
    }

}
