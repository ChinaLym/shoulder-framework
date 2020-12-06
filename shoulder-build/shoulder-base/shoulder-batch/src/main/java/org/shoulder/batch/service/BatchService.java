package org.shoulder.batch.service;


import org.shoulder.batch.model.BatchData;
import org.shoulder.batch.model.BatchProgress;
import org.shoulder.batch.service.ext.BatchTaskSliceHandler;
import org.shoulder.core.context.AppContext;

import java.util.Locale;

/**
 * 批处理
 *
 * @author lym
 */
public interface BatchService {


    /**
     * 判断是否允许执行
     *
     * @return boolean
     */
    boolean canExecute();

    /**
     * 批量信息
     *
     * @param batchData 批量/更新
     * @return 批量任务标识
     */
    default String doProcess(BatchData batchData) {
        return this.doProcess(batchData, AppContext.getUserId(), AppContext.getLocale());
    }

    /**
     * 批量信息
     *
     * @param batchData 批量/更新
     * @param userId    用户信息
     * @param locale    语言标识
     * @return 批量任务标识
     */
    default String doProcess(BatchData batchData, String userId, Locale locale) {
        return this.doProcess(batchData, userId, locale, null);
    }


    /**
     * 处理
     *
     * @param batchData             批量入参
     * @param userId                用户信息
     * @param locale                语言标识
     * @param batchTaskSliceHandler 特殊业务处理器
     * @return 批量任务标识
     */
    String doProcess(BatchData batchData, String userId, Locale locale, BatchTaskSliceHandler batchTaskSliceHandler);

    /**
     * 获取批量进度与结果
     *
     * @param taskId 用户信息
     * @return Object 批量进度或者结果
     */
    BatchProgress queryBatchProgress(String taskId);

}
