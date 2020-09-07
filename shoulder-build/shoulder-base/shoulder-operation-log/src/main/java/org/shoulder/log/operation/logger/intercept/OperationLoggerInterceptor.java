package org.shoulder.log.operation.logger.intercept;

import org.shoulder.log.operation.dto.Operable;
import org.shoulder.log.operation.dto.OperationLogDTO;

import java.util.List;

/**
 * 日志记录拦截器
 * 可以在日志记录前后做一些事情（对即将记录的日志进行数据清洗等工作）
 * 用途举例：
 * 空值填充：如日志中有 objectId 但是缺少 objectName，可以在这里查数据库填充
 * 数据转换：如日志中表示性别的部分为 0 或 1，需要转换成有意义的数据
 * 数据标准：批量记录时将10条合成为1条记录。
 * 合法校验：过滤不符合预期的日志
 * 格式规范：将字段统一格式。
 * 其他自定义业务规则.....
 *
 * @author lym
 * @implSpec 该类覆盖了所有操作日志可以拦截的时刻，默认实现为什么都不做
 */
public interface OperationLoggerInterceptor {

    /**
     * 可以在日志组装前后做一些事情 —— 批量操作日志组装之前
     * 可以在这里对操作对象集合进行压缩、填充或者剔除
     *
     * @param template     日志模板，可以通过该参数获取是哪个模块，操作类型
     * @param operableList 待组装的被操作对象
     * @return 个性处理后的 operableList
     */
    default List<? extends Operable> beforeAssembleBatchLogs(OperationLogDTO template, List<?
        extends Operable> operableList) {
        return operableList;
    }

    /**
     * 可以在日志组装前后做一些事情 —— 批量操作日志组装之后
     * 可以在这里对批量操作日志对象进行最终的处理
     *
     * @param opLogs 组装完毕后的操作日志实体
     * @return 个性处理后的 opLogs
     */
    default List<? extends OperationLogDTO> afterAssembleBatchLogs(List<? extends OperationLogDTO> opLogs) {
        return opLogs;
    }

    /**
     * 在记录日志之前。
     * 可以继续针对自己的应用统一补充某些有规律的值
     * 可以进行一些格式校验，以免记录的日志某些字段超长（加入存储是关系型数据库则推荐使用）
     *
     * @param opLog 待验证的日志实体
     */
    default void beforeLog(OperationLogDTO opLog) {

    }

    /**
     * 记录日后。
     * 如果 beforeLog 设置类某些线程变量，可以在这里完成清理工作。
     *
     * @param opLog 记录完毕的日志实体。该变量主要是利于组件区分是哪个模块的什么操作。
     *              1. 日志已经记录，修改该变量已经没有意义。
     *              2. 为了利于GC，不要增加该变量的引用
     */
    default void afterLog(OperationLogDTO opLog) {

    }


}
