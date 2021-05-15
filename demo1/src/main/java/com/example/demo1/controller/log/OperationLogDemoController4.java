package com.example.demo1.controller.log;

import com.example.demo1.bo.UserInfo;
import com.example.demo1.config.DemoOperationLogInterceptor;
import com.example.demo1.dto.OperationRecord;
import com.example.demo1.util.MockBusinessOperation;
import io.swagger.annotations.ApiOperation;
import org.shoulder.log.operation.annotation.OperationLog;
import org.shoulder.log.operation.context.OpLogContextHolder;
import org.shoulder.log.operation.enums.OperationResult;
import org.shoulder.log.operation.model.OperationLogDTO;
import org.shoulder.log.operation.model.sample.OperateRecordDto;
import org.shoulder.web.annotation.SkipResponseWrap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * 实战 —— 批量记录演示
 * 批量操作设计思想介绍（基于模板的思想，压缩内存存储空间，压缩（减少）使用者需要写的代码行数）
 *
 * @author lym
 */
@SkipResponseWrap // 该类所有方法的返回值将不被包装
@RestController
@RequestMapping("oplog/batch")
public class OperationLogDemoController4 {

    /**
     * 虽然是批量操作，但希望只记录一条操作日志时：如添加了10个用户信息，但认证这是一次操作
     * 如批量处理数据只记录成功数，失败数，而不记录每次处理的数据详情
     * <a href="http://localhost:8080/oplog/batch/1" />
     */
    @OperationLog(operation = "demo4_1", detailKey = "demo4_1.intro")
    @GetMapping("1")
    public OperationLogDTO oneRecord() {
        int total = 20;
        // 模拟解析完上传的文件，包含多条用户信息
        List<UserInfo> importUserInfo = MockBusinessOperation.newRandomUsers(total);
        // 模拟校验校验这些信息
        List<OperationRecord<UserInfo>> operationRecords = MockBusinessOperation.process(importUserInfo);

        // 计算成功多少条，失败多少条准备填充 detail
        List<String> actionDetails = Arrays.asList(
                String.valueOf(operationRecords.stream().filter(OperateRecordDto::isSuccess).count()),
                String.valueOf(operationRecords.stream().filter(result -> !result.success()).count())
        );

        OpLogContextHolder.getLog().setResult(OperationResult.of(operationRecords))
                .setDetailItems(actionDetails);

        return OpLogContextHolder.getLog();
    }


    /**
     * 记录条数和批量操作实体数相同
     * 批量锁定 10 个用户账号，记录 10 条操作日志
     *
     * <a href="http://localhost:8080/oplog/batch/2" />
     */
    @OperationLog(operation = "demo4_2", detailKey = "demo4_2.intro")
    @GetMapping("2")
    public void allRecords() {
        int total = 20;
        List<UserInfo> importUserInfo = MockBusinessOperation.newRandomUsers(total);
        List<OperationRecord<UserInfo>> operationRecords = MockBusinessOperation.process(importUserInfo);

        OpLogContextHolder.setOperableObjects(operationRecords);
    }


    /**
     * 处理 n 条数据，记录 n 条批量操作日志，具体如何记录由业务定义，m=fx(n), x ∈ {'addUserBatch', 'updateUserBatch', ....}【最灵活的记录方式】
     * 代码与 {@link #allRecords} 方法完全相同，只是拦截器中做了特殊处理 {@link DemoOperationLogInterceptor#beforeAssembleBatchLogs}
     * <a href="http://localhost:8080/oplog/batch/3" />
     */
    @OperationLog(operation = "demo4_3", detailKey = "demo4_3.intro")
    @GetMapping("3")
    @ApiOperation(value = "自定义策略", notes = "比如一条操作日志记录 5 次操作。若操作100条，记录20条操作日志。")
    public void customRecords() {
        int total = 20;
        List<UserInfo> importUserInfo = MockBusinessOperation.newRandomUsers(total);
        List<OperationRecord<UserInfo>> operationRecords = MockBusinessOperation.process(importUserInfo);

        OpLogContextHolder.setOperableObjects(operationRecords);
    }


}
