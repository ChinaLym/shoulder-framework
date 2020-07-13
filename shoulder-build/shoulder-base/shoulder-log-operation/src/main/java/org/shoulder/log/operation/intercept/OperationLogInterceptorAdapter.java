package org.shoulder.log.operation.intercept;

import org.shoulder.log.operation.dto.Operable;
import org.shoulder.log.operation.entity.OperationLogEntity;

import java.util.List;

/**
 * 操作日志拦截器，用于对即将记录的日志进行数据清洗等工作。
 * 用途举例：
 *      空值填充：如日志中有 objectId 但是缺少 objectName，可以在这里查数据库填充
 *      数据转换：如日志中表示性别的部分为 0 或 1，需要转换成有意义的数据
 *      数据标准：批量记录时将10条合成为1条记录。
 *      合法校验：过滤不符合预期的日志
 *      格式规范：将字段统一格式。
 *      其他自定义业务规则.....
 *
 * @implSpec 该类覆盖了所有操作日志可以拦截的时刻，默认实现为什么都不做
 * @author lym
 */
public interface OperationLogInterceptorAdapter extends OperationLoggerInterceptor {

    // ======================================== 批量 =========================================

    /**
     * 在生成批量操作日志之前
     * 可以在这里对被操作对象集合进行压缩或者过滤剔除
     *
     * @param template     日志组装模板
     * @param operableList 待组装的被操作对象
     * @return 需要参与组装的被操作对象 List
     */
    @Override
    default List<? extends Operable> beforeAssembleBatchLogs(OperationLogEntity template, List<? extends Operable> operableList) {
        return operableList;
    }

    /**
     * 在生成批量操作日志之后
     * 可以在这里对待记录的批量操作日志对象进行最终的处理
     *
     * @param OperationLogEntities 组装完毕后的操作日志实体
     * @return 需要记录的操作日志
     */
    @Override
    default List<? extends OperationLogEntity> afterAssembleBatchLogs(List<? extends OperationLogEntity> OperationLogEntities) {
        return OperationLogEntities;
    }

    // ======================================== 每条 =========================================

    /**
     * 在验证之前。
     * 可以继续针对自己的应用统一补充某些有规律的值
     *
     * @param opLogEntity 待验证的日志实体
     */
    @Override
    default void beforeValidate(OperationLogEntity opLogEntity) {

    }

    /**
     * 在日志字段检查 失败后。
     *      可以做出一些补偿之类的措施
     *      【后续版本补充，需要统一一下日志部分的错误】
     */
    //void afterValidateFail(OperationLogEntity opLogEntity);


    /**
     * 记录日后。
     * 如果 beforeValidate 设置类某些线程变量，可以在这里完成清理工作。
     *
     * @param opLogEntity 记录完毕的日志实体，一般不需要引用该变量
     */
    @Override
    default void afterLog(OperationLogEntity opLogEntity) {

    }
}
