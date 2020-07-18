package org.shoulder.log.operation.util;


import org.apache.commons.collections4.CollectionUtils;
import org.shoulder.core.context.BaseContextHolder;
import org.shoulder.log.operation.dto.Operable;
import org.shoulder.log.operation.dto.Operator;
import org.shoulder.log.operation.entity.OperationLogEntity;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 操作日志实体构建器
 *
 * （当前主要面向框架内部，不建议使用者调用该类中的方法，方法名称和参数不稳定）推荐使用注解方式。
 *
 * @author lym
 */
public class OperationLogBuilder {

    /**
     * 创建一条操作日志
     *  1. 创建对象
     *  2. 填充为内部任务日志
     *  3. 若上下文存在登录的用户则将其当做操作者
     */
    public static OperationLogEntity newLog(String operation){
        Operator currentOperator = OpLogContextHolder.getCurrentOperator();
        return new OperationLogEntity(operation)
                .setOperator(currentOperator)
                .setServiceId(BaseContextHolder.getServiceId());
    }

    /**
     * 1. 根据 operationLogTemplate 创建 operableList.size() 条操作日志
     * 2. 用 operableList 分别填充每条日志的被操作对象和 detailItem 字段
     *
     *      如果 operableList 为空，则返回日志模板 operationLogTemplate。
     *
     * @param operationLogTemplate 日志模板
     * @param operableList        被操作对象集合
     * @return 多条填充完毕的日志对象，至少返回包含一条日志的 List
     * */
    public static List<OperationLogEntity> newLogsFrom(OperationLogEntity operationLogTemplate,
                                                       @Nullable Collection<? extends Operable> operableList){
        if(operationLogTemplate == null || CollectionUtils.isEmpty(operableList)){
            return Collections.singletonList(operationLogTemplate);
        }
        List<OperationLogEntity> resultLogs = new ArrayList<>(operableList.size());
        for (Operable operable : operableList) {
            OperationLogEntity logEntity = operationLogTemplate.clone();
            resultLogs.add(logEntity.setOperableObject(operable));
        }
        return resultLogs;
    }

}
