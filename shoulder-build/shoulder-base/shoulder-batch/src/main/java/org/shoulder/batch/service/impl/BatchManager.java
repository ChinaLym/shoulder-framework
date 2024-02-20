package org.shoulder.batch.service.impl;

import org.apache.commons.collections4.ListUtils;
import org.shoulder.batch.constant.BatchConstants;
import org.shoulder.batch.enums.BatchDetailResultStatusEnum;
import org.shoulder.batch.model.BatchData;
import org.shoulder.batch.model.BatchDataSlice;
import org.shoulder.batch.model.BatchRecord;
import org.shoulder.batch.model.BatchRecordDetail;
import org.shoulder.batch.progress.BatchProgressRecord;
import org.shoulder.batch.progress.ProgressAble;
import org.shoulder.batch.repository.BatchRecordDetailPersistentService;
import org.shoulder.batch.repository.BatchRecordPersistentService;
import org.shoulder.batch.spi.DataItem;
import org.shoulder.batch.spi.DefaultTaskSplitHandler;
import org.shoulder.batch.spi.TaskSplitHandler;
import org.shoulder.core.context.AppContext;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;
import org.shoulder.core.util.AssertUtils;
import org.shoulder.core.util.ContextUtils;
import org.shoulder.log.operation.context.OpLogContextHolder;
import org.shoulder.log.operation.enums.OperationResult;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

/**
 * 批处理管理员
 * 负责分成小的任务，交给 Worker 执行
 *
 * @author lym
 */
public class BatchManager implements Runnable, ProgressAble {

    protected final static Logger log = LoggerFactory.getLogger(BatchManager.class);

    /**
     * 异步工作单元的最大数量
     */
    private static final int MAX_WORKER_SIZE = 4;

    /**
     * 线程池
     */
    protected ExecutorService threadPool = ContextUtils.getBean(BatchConstants.BATCH_THREAD_POOL_NAME);

    /**
     * 批量处理记录
     */
    protected BatchRecordPersistentService batchRecordPersistentService =
            ContextUtils.getBean(BatchRecordPersistentService.class);

    /**
     * 批处理记录详情
     */
    protected BatchRecordDetailPersistentService batchRecordDetailPersistentService =
            ContextUtils.getBean(BatchRecordDetailPersistentService.class);

    // ------------------------------------------------

    /**
     * 操作用户
     */
    protected Long userId;
    /**
     * 语言标识
     */
    protected String languageId;

    /**
     * 本次要批量处理的数据
     */
    protected BatchData batchData;

    /**
     * 当前进度
     */
    protected BatchProgressRecord progress;

    /**
     * 本次任务结果汇总
     */
    protected BatchRecord result;

    /**
     * 任务队列，任务向这里丢
     * todo 【扩展性】后续考虑共享队列，将属性抽出来，一个 manager 可以同时处理多次批处理任务
     */
    protected BlockingQueue<BatchDataSlice> jobQueue;

    /**
     * 结果队列，结果从这里取
     */
    protected BlockingQueue<BatchRecordDetail> resultQueue;

    public BatchManager(BatchData batchData) {
        batchData.setBatchId(generateBatchId());
        AssertUtils.notNull(batchData, CommonErrorCodeEnum.ILLEGAL_PARAM);
        AssertUtils.notNull(batchData.getDataType(), CommonErrorCodeEnum.ILLEGAL_PARAM);
        AssertUtils.notNull(batchData.getOperation(), CommonErrorCodeEnum.ILLEGAL_PARAM);
        AssertUtils.notEmpty(batchData.getBatchListMap(), CommonErrorCodeEnum.ILLEGAL_PARAM);
        int total = batchData.getBatchListMap().values().stream()
                .map(List::size).reduce(Integer::sum).orElse(0);
        AssertUtils.isTrue(total > 0, CommonErrorCodeEnum.ILLEGAL_PARAM, "batchList.total must > 0");

        String currentUserId = AppContext.getUserId();
        this.userId = currentUserId == null ? 0 : Long.parseLong(currentUserId);
        this.languageId = AppContext.getLocaleOrDefault().toString();
        this.batchData = batchData;
        this.batchData.setSuccessList(ListUtils.emptyIfNull(batchData.getSuccessList()));
        this.batchData.setFailList(ListUtils.emptyIfNull(batchData.getFailList()));

        // 初始化进度对象（保证在构造器中完成）
        this.progress = new BatchProgressRecord();
        this.progress.setId(batchData.getBatchId());
        this.progress.setTotal(total);
        this.progress.addSuccess(batchData.getSuccessList().size());
        this.progress.addFail(batchData.getFailList().size());
    }

    private static String generateBatchId() {
        return UUID.randomUUID().toString();
    }

    /**
     * 使命 | 职责
     */
    @Override
    public void run() {

        log.debug("batch task start, dataType={}, operation={}", batchData.getDataType(), batchData.getOperation());

        // 任务分片，初始化任务队列、结果队列
        List<BatchDataSlice> batchSliceList = splitTask(batchData);

        int jobSize = batchSliceList.size();
        AssertUtils.isTrue(jobSize > 0, CommonErrorCodeEnum.ILLEGAL_PARAM, "after splitTask, jobSize can't be 0");
        jobQueue = new LinkedBlockingQueue<>(jobSize);
        jobQueue.addAll(batchSliceList);

        // 安排工人
        int dataItemTotalNum = batchSliceList.stream()
                .map(BatchDataSlice::calculateDataSize)
                .reduce(Integer::sum).orElse(0);
        // 最大不能超过 MAX_WORKER_SIZE
        int workerNum = Integer.min(MAX_WORKER_SIZE, jobSize);
        resultQueue = new LinkedBlockingQueue<>(dataItemTotalNum);
        if (dataItemTotalNum < 2) {
            // 数据量极少还用批处理框架通常是不符合预期的，打印 warn
            log.warn("batch task split and Total only={}! subJobNum={}, workNum={}", dataItemTotalNum, jobSize, workerNum);
        } else {
            log.debug("batch task split, total={}, subJobNum={}, workNum={}", dataItemTotalNum, jobSize, workerNum);
        }
        this.progress.setTotal(dataItemTotalNum);
        compositeBatchRecords(dataItemTotalNum);
        progress.start();

        // 开始分配任务
        for (int i = 0; i < workerNum - 1; i++) {
            BatchProcessor worker = new BatchProcessor(batchData.getBatchId(), jobQueue, resultQueue);
            if (!canEmployWorker(worker)) {
                // 提交失败，说明当且服务器较忙，无法雇佣工人，因此中断委派，转由当前线程执行全部任务
                log.warnWithErrorCode(CommonErrorCodeEnum.SERVER_BUSY.getCode(),
                        "employ workers fail, fail back to execute by current, it may cost more time.");
                break;
            }
        }
        // 当且线程也参与做任务
        BatchProcessor worker = new BatchProcessor(batchData.getBatchId(), jobQueue, resultQueue);
        worker.run();

        // 阻塞式处理结果
        handleResult(dataItemTotalNum);
        progress.finish();
        //result.setTotalNum();
        result.setSuccessNum(progress.getSuccessNum());
        result.setFailNum(progress.getFailNum());
        // todo 【进阶】持久化时机优化
        persistentImportRecord();
        fillOperationLog();
        log.info("batch task finished.");
    }

    /**
     * 提前组装批处理记录，方便后续写入到数据库
     */
    private void compositeBatchRecords(int total) {
        // 初始化数据处理结果对象 Record todo total
        this.result = BatchRecord.builder()
                .id(batchData.getBatchId())
                .dataType(batchData.getDataType())
                .operation(batchData.getOperation())
                .totalNum(total)
                .createTime(new Date())
                .creator(userId)
                .build();

        // 初始化数据处理详情对象 List<RecordDetail>
        this.result.setDetailList(new ArrayList<>(total));
//        List<BatchRecordDetail> detailList = new ArrayList<>(total);
//        for (int i = 0; i < total; i++) {
//            BatchRecordDetail detailItem = BatchRecordDetail.builder()
//                    .recordId(batchData.getBatchId())
//                    .index(i)
//                    .build();
//            // 这里认为 index 唯一的，所以是 set，而非 add
//            detailList.add(detailItem);
//        }

        // 预填充数据处理详情对象 List<RecordDetail> 的待处理部分
//        batchData.getBatchListMap().forEach((operationType, dataList) -> {
//            for (DataItem dataItem : dataList) {
//                // 这里认为 total 是所有校验的数据，若 total = 100，则不可能有 index > 100 的数据
//                detailList.get(dataItem.getIndex())
//                        .setIndex(dataItem.getIndex())
//                        .setRecordId(batchData.getBatchId())
//                        .setOperation(operationType)
//                        .setStatus(BatchDetailResultStatusEnum.SUCCESS.getCode())
//                        .setSource(serializeSource(dataItem));
//            }
//        });
        // 预填充数据处理详情对象 List<RecordDetail> 的直接成功/失败部分（重复且不处理的，校验失败无法处理的） todo 【模型升级】 跳过状态定义
//        for (DataItem dataItem : batchData.getSuccessList()) {
//            result.getDetailList().get(dataItem.getIndex())
//                    .setRecordId(batchData.getBatchId())
//                    .setIndex(dataItem.getIndex())
//                    .setOperation(batchData.getOperation())
//                    .setSource(serializeSource(dataItem))
//                    .setStatus(BatchDetailResultStatusEnum.SKIP_FOR_REPEAT.getCode());
//        }
//        for (DataItem dataItem : batchData.getFailList()) {
//            // getFailReason 不可能为 null，否则就是使用者错误，未塞入错误原因
//            result.getDetailList().get(dataItem.getIndex())
//                    .setRecordId(batchData.getBatchId())
//                    .setIndex(dataItem.getIndex())
//                    .setOperation(batchData.getOperation())
//                    .setSource(serializeSource(dataItem))
//                    .setStatus(BatchDetailResultStatusEnum.SKIP_FOR_INVALID.getCode())
//                    .setFailReason(batchData.getFailReason().get(dataItem.getIndex()));
//        }
//        log.info("Directly: success:{}, fail:{}", batchData.getSuccessList().size(), batchData.getFailList().size());
        // 可能直接完成了
//        if (progress.hasFinish()) {
//            this.progress.finish();
//        }
    }

    /**
     * 把原始数据转为 {@link BatchRecordDetail#setSource(String)} 字段
     * 筛选出部分字段，并脱敏等处理
     */
    private String serializeSource(DataItem importData) {
        // 这里直接 json
        return importData.serialize();
    }

    // ================================= 任务分片与执行 ==================================

    /**
     * 任务分片
     *
     * @param batchData 所有数据
     * @return 分片后的
     */
    protected List<BatchDataSlice> splitTask(BatchData batchData) {
        Map<String, TaskSplitHandler> taskSplitHandlerMap = ContextUtils.getBeansOfType(TaskSplitHandler.class);

        List<BatchDataSlice> result = taskSplitHandlerMap.values().stream()
                .filter(s -> !(s instanceof DefaultTaskSplitHandler))
                .filter(s -> s.support(batchData))
                .findFirst()
                .map(s -> s.splitTask(batchData))
                .orElse(null);
        if (result != null) {
            return result;
        }

        TaskSplitHandler defaultTaskSplitHandler = ContextUtils.getBean(DefaultTaskSplitHandler.class);
        return defaultTaskSplitHandler.splitTask(batchData);
    }

    /**
     * 雇佣工人（提交到线程池）
     *
     * @param worker 工作线程
     */
    private boolean canEmployWorker(BatchProcessor worker) {
        try {
            threadPool.execute(worker);
            return true;
        } catch (Exception e) {
            // 这里有可能线程池满了，拒绝执行
            log.warn(CommonErrorCodeEnum.SERVER_BUSY, e);
            return false;
        }
    }

    // ================================= 处理结果 ==================================

    /**
     * 处理结果
     *
     * @param n 期待多少个结果
     */
    private void handleResult(int n) {
        for (int i = 0; i < n; i++) {
            // worker 未捕获的异常会交给 UncaughtExceptionHandler，这里设计时让worker保证一定返回结果
            BatchRecordDetail taskResultDetail = takeUnExceptInterrupted(resultQueue);
//            if (taskResultDetail.isCalculateProgress()) {
            boolean success = BatchDetailResultStatusEnum.SUCCESS.getCode() == taskResultDetail.getStatus();
            if (success) {
                progress.addSuccess(1);
            } else {
                progress.addFail(1);
            }
            // 调度者只能修改处理结果和原因
            taskResultDetail.setOperation(batchData.getOperation());
            taskResultDetail.setRecordId(batchData.getBatchId());
            result.getDetailList().add(taskResultDetail);
//            }
        }
        if (!jobQueue.isEmpty()) {
            // 已经结束，不应该还有
            //throw new IllegalStateException("jobQueue not empty")
            log.errorWithErrorCode(CommonErrorCodeEnum.CODING.getCode(), "jobQueue not empty!");
            jobQueue.clear();
        }
    }

    /**
     * 自信取，自信地认为不会比中断
     */
    private <T> T takeUnExceptInterrupted(BlockingQueue<T> queue) {
        try {
            return queue.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    // ================================= 后置，如保存记录 ==================================

    /**
     * 根据任务处理的总体结果，增加一条处理记录
     */
    protected void persistentImportRecord() {
        if (!batchData.isPersistentRecord()) {
            return;
        }
        boolean notAllSetSourceStr = result.getDetailList().stream()
                .map(BatchRecordDetail::getSource)
                .anyMatch(Objects::isNull);
        AssertUtils.isFalse(notAllSetSourceStr, CommonErrorCodeEnum.CODING, "impl need invoke setSource().");

        try {
            batchRecordPersistentService.insert(result);
            // 当前: 最后保存一次, 简单。
            // 考虑：事前保存一次，每个 worker 持久化，最终更新；满足事务一致性，同时避免大事务

            batchRecordDetailPersistentService.batchSave(result.getId(),
                    result.getDetailList().stream()
                            .sorted(Comparator.comparingInt(BatchRecordDetail::getIndex))
                            .collect(Collectors.toList())
            );
        } catch (Exception e) {
            log.warnWithErrorCode(CommonErrorCodeEnum.DATA_STORAGE_FAIL.getCode(), "persistentImportRecord fail", e);
            throw CommonErrorCodeEnum.DATA_STORAGE_FAIL.toException(e);
        }
    }

    /**
     * 填充操作日志
     */
    private void fillOperationLog() {
        // 根据处理结果判断总体结果
        OperationResult opResult = OperationResult.of(progress.getSuccessNum() > 0, progress.getFailNum() > 0);
        if (OpLogContextHolder.getContext() == null || OpLogContextHolder.getLog() == null) {
            // 当前没有上下文
            return;
        }
        OpLogContextHolder.getLog().setResult(opResult)
                .addDetailItem(String.valueOf(progress.getSuccessNum()))
                .addDetailItem(String.valueOf(progress.getFailNum()))
                .setObjectId(batchData.getBatchId())
                .setObjectType(batchData.getDataType());
        OpLogContextHolder.enableAutoLog();
    }

    public String getBatchId() {
        return batchData.getBatchId();
    }

    public BatchProgressRecord getBatchProgress() {
        return progress;
    }

}
