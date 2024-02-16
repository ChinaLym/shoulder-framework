package org.shoulder.batch.service;

import org.shoulder.batch.model.BatchData;
import org.shoulder.batch.model.BatchDataSlice;
import org.shoulder.batch.progress.BatchProgressRecord;
import org.shoulder.batch.service.impl.BatchManager;
import org.shoulder.batch.service.impl.BatchProcessor;
import org.shoulder.batch.spi.BatchTaskSliceHandler;
import org.shoulder.batch.spi.DataItem;
import org.shoulder.core.context.AppContext;

import java.util.Locale;

/**
 * 批处理
 * <p>
 * shoulder.Batch 不仅仅提供了一个自动适应且支持单机/集群模式的 进度条查询能力，且提供了一套较完整的批处理框架、并在导入导出这里提供了较完整的解决方案：
 * <hr>
 * 使用：
 * 1. new BatchData();                               // new BatchData 并设置值
 * 2. batchId = batchService.doProcess(batchData)     // 提交处理，拿到 batchId
 * 3. batchService.queryBatchProgress(batchId)        // 根据 batchId 查询实施进度
 *
 * @author lym
 * @see BatchData 整体任务，会由 {@link BatchManager} 封装成 {@link BatchProcessor}(Runnable)，其会根据 {@link BatchTaskSliceHandler} 拆成多个 BatchDataSlice
 * @see BatchDataSlice 是任务的一个分片，但一个分片可能含有多个原子数据 {@link DataItem}
 * @see DataItem 被处理的原子数据（可能是一行数据、也可能是一个对象等）
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
     * @return 批量批处理任务id
     */
    default String doProcess(BatchData batchData) {
        return this.doProcess(batchData, AppContext.getUserId(), AppContext.getLocaleOrDefault());
    }

    /**
     * 批量信息
     *
     * @param batchData 批量/更新
     * @param userId    用户信息
     * @param locale    语言标识
     * @return 批量批处理任务id
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
     * @return 批量批处理任务id
     */
    String doProcess(BatchData batchData, String userId, Locale locale, BatchTaskSliceHandler batchTaskSliceHandler);

    /**
     * 获取批量进度与结果
     *
     * @param batchId 批处理id
     * @return Object 批量进度或者结果
     */
    BatchProgressRecord queryBatchProgress(String batchId);

}
