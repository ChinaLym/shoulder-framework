package org.shoulder.log.operation.logger;

import org.shoulder.log.operation.dto.Operable;
import org.shoulder.log.operation.entity.OperationLogEntity;
import org.shoulder.log.operation.intercept.OperationLoggerInterceptor;
import org.shoulder.log.operation.util.OperationLogHolder;
import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.List;

/**
 * 操作日志记录器
 *  子类要求：不能抛出异常，遇到异常需要 logger处理。因为抛出异常可能导致上层业务失败
 *
 * @author lym
 */
public interface OperationLogger {

    /**
     * 根据 OperationLogUtils 里的内容记录日志
     * */
    default void log(){
        OperationLogEntity opLogEntity = OperationLogHolder.getLog();
        if(opLogEntity != null){
            List<? extends Operable> operableCollection = OperationLogHolder.getOperableObjects();

            if (operableCollection == null || operableCollection.isEmpty()) {
                this.log(opLogEntity);
            } else {
                this.log(opLogEntity, operableCollection);
            }
        }
    }

    /**
     * 记录一条操作日志
     * @param opLogEntity 操作日志对象
     */
    void log(OperationLogEntity opLogEntity);

    /**
     * 记录多条操作日志
     * @param opLogEntityList 操作日志对象集合
     */
    void log(@NonNull Collection<? extends OperationLogEntity> opLogEntityList);

    /**
     * 组装并记录多条操作日志
     * @param opLogEntity     记录模板
     * @param operableList     被操作对象集合
     */
    void log(@NonNull OperationLogEntity opLogEntity, List<? extends Operable> operableList);

    /**
     * 注册拦截器
     * @param logInterceptor 拦截器
     */
    void addInterceptor(OperationLoggerInterceptor logInterceptor);
}
