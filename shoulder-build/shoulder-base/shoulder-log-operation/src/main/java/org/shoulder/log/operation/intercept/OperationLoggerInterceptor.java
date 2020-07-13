package org.shoulder.log.operation.intercept;

import org.shoulder.log.operation.dto.Operable;
import org.shoulder.log.operation.entity.OperationLogEntity;

import java.util.List;

/**
 * 日志记录拦截器
 * 可以在日志记录前后做一些事情
 *
 * @author lym
 */
public interface OperationLoggerInterceptor {

    /**
     * 可以在日志组装前后做一些事情 —— 批量操作日志组装之前
     *      可以在这里对操作对象集合进行压缩、填充或者剔除
     *
     * @param template      日志模板，可以通过该参数获取是哪个 module，哪个 action 等
     * @param operableList  待组装的被操作对象
     * @return 个性处理后的 operableList
     */
    List<? extends Operable> beforeAssembleBatchLogs(OperationLogEntity template, List<?
            extends Operable> operableList);

    /**
     * 可以在日志组装前后做一些事情 —— 批量操作日志组装之后
     *      可以在这里对批量操作日志对象进行最终的处理
     *
     * @param operationLogEntities 组装完毕后的操作日志实体
     * @return 个性处理后的 operationLogEntities
     */
    List<? extends OperationLogEntity> afterAssembleBatchLogs(List<? extends OperationLogEntity> operationLogEntities);



    /**
     * 在验证之前。
     *      可以继续针对自己的应用统一补充某些有规律的值
     *
     * @param opLogEntity 待验证的日志实体
     */
    void beforeValidate(OperationLogEntity opLogEntity);

    /**
     * 在日志字段检查 失败后。
     *      可以做出一些补偿之类的措施
     *      【后续版本补充，需要统一一下日志部分的错误】
     */
    //void afterValidateFail(OperationLogEntity opLogEntity);


    /**
     * 记录日后。
     *      如果 beforeValidate 设置类某些线程变量，可以在这里完成清理工作。
     *
     * @param opLogEntity 记录完毕的日志实体。该变量主要是利于组件区分是哪个 module，哪个action。
     *                     1. 日志已经记录，修改该变量已经没有意义。
     *                     2. 为了利于GC，不要增加该变量的引用
     */
    void afterLog(OperationLogEntity opLogEntity);





}
