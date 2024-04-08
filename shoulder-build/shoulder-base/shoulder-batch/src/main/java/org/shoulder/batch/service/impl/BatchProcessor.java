package org.shoulder.batch.service.impl;

import jakarta.annotation.Nonnull;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.shoulder.batch.enums.BatchDetailResultStatusEnum;
import org.shoulder.batch.enums.BatchErrorCodeEnum;
import org.shoulder.batch.log.ShoulderBatchLoggers;
import org.shoulder.batch.model.BatchDataSlice;
import org.shoulder.batch.model.BatchRecordDetail;
import org.shoulder.batch.spi.BatchTaskSliceHandler;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.i18.Translator;
import org.shoulder.core.log.Logger;
import org.shoulder.core.util.ContextUtils;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

/**
 * 批处理工人
 * 实现了返回结果，具体操作还需要使用者实现 {@link BatchTaskSliceHandler}
 *
 * @author lym
 */
public class BatchProcessor implements Runnable {

    private final static Logger log = ShoulderBatchLoggers.DEFAULT;

    /**
     * 任务队列
     */
    private final BlockingQueue<BatchDataSlice> taskQueue;
    /**
     * 产生结果队列
     */
    private final BlockingQueue<BatchRecordDetail> resultQueue;

    /**
     * 数据处理器
     */
    private final Collection<BatchTaskSliceHandler> batchTaskSliceHandlers =
            ContextUtils.getBeansOfType(BatchTaskSliceHandler.class).values();

    protected Translator translator;

    protected String batchId;

    public BatchProcessor(String batchId, BlockingQueue<BatchDataSlice> taskQueue,
                          BlockingQueue<BatchRecordDetail> resultQueue) {
        this.batchId = batchId;
        this.taskQueue = taskQueue;
        this.resultQueue = resultQueue;
        this.translator = ContextUtils.getBean(Translator.class);
    }

    @Override
    public void run() {
        BatchDataSlice task;
        // 不断尝试从任务队列取，直至取不到，结束
        int taskProcessed = 0;
        for (; (task = taskQueue.poll()) != null; taskProcessed++) {
            List<BatchRecordDetail> results = doWork(task);
            putResult(results);
        }
        log.info("{} stop, processed {}", getBatchId(), taskProcessed);
    }

    /**
     * 放入结果队列
     *
     * @param results 处理完毕的结果
     */
    private void putResult(List<BatchRecordDetail> results) {
        int put = 0;
        try {
            for (; put < results.size(); put++) {
                resultQueue.put(results.get(put));
            }
        } catch (InterruptedException e) {
            log.error("put result into queue FAIL, size=" + results.size() + " put=" + put, e);
        }
    }

    public String getBatchId() {
        return batchId;
    }

    /**
     * 执行批处理任务
     * 数据类型 dataType, 操作类型 operationType 全部确定，且所有数据相同
     */
    public List<BatchRecordDetail> doWork(@Nonnull BatchDataSlice task) {
        log.info("task start. {}", task);
        if (CollectionUtils.isEmpty(task.getBatchList())) {
            return Collections.emptyList();
        }
        // 这里委派给使用者提供的数据处理扩展点
        String dataType = task.getDataType();
        String operation = task.getOperationType();
        BatchTaskSliceHandler taskHandler = batchTaskSliceHandlers.stream()
                .filter(handler -> handler.support(dataType, operation))
                .findFirst()
                // 若不存在则肯定是代码写错了，直接抛出异常
                .orElseThrow(() -> BatchErrorCodeEnum.DATA_TYPE_OR_OPERATION_NOT_SUPPORT
                        .toException(dataType, operation));
        List<BatchRecordDetail> taskResult = null;
        try {
            log.debug("begin_handle dataType=" + dataType + "operation=" + operation
                    + ",batchId=" + getBatchId() + ", slice=" + task.getSequence()
                    + ", handler=" + taskHandler.getClass().getName());
            taskResult = taskHandler.handle(task);
        } catch (Exception e) {
            log.error("Batch Process FAIL! dataType=" + dataType + "operation=" + operation
                    + ",batchId=" + getBatchId() + ", slice=" + task.getSequence()
                    + ", handler=" + taskHandler.getClass().getName(), e);
        }
        log.info("task {}-{} finished", task.getBatchId(), task.getSequence());
        return polluteUnknownIfMissingResult(task, ListUtils.emptyIfNull(taskResult));
    }

    /**
     * 检查结果数，是否和数据数目一致，否则自动补充失败
     */
    private List<BatchRecordDetail> polluteUnknownIfMissingResult(@Nonnull BatchDataSlice task,
                                                                  @Nonnull List<BatchRecordDetail> resultDetailList) {
        int exceptNum = task.calculateDataSize();
        int actuallyNum = resultDetailList.size();
        if (exceptNum == actuallyNum) {
            return resultDetailList;
        }
        log.warnWithErrorCode(BatchErrorCodeEnum.TASK_SLICE_RESULT_INVALID.getCode(),
                BatchErrorCodeEnum.TASK_SLICE_RESULT_INVALID.getMessage(), exceptNum, actuallyNum);

        // 为没有返回结果的任务进行补偿填充，认为失败了
        Map<Integer, BatchRecordDetail> indexedBatchRecordDetailMap = resultDetailList.stream().collect(
                Collectors.toMap(BatchRecordDetail::getIndex, o -> o, (o1, o2) -> o2));

        task.dataIndexStream()
                .map(index ->
                        Optional.ofNullable(indexedBatchRecordDetailMap.get(index))
                                .orElse(new BatchRecordDetail(index, "UNKNOWN", BatchDetailResultStatusEnum.FAILED.getCode(),
                                        CommonErrorCodeEnum.UNKNOWN.getCode()))
                ).forEach(resultDetailList::add);

        return resultDetailList;
    }

}

