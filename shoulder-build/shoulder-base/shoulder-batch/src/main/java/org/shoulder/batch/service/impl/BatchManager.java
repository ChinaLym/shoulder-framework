package org.shoulder.batch.service.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.shoulder.batch.enums.BatchResultEnum;
import org.shoulder.batch.model.*;
import org.shoulder.batch.repository.mapper.BatchRecordDetailMapper;
import org.shoulder.batch.repository.mapper.BatchRecordMapper;
import org.shoulder.core.context.AppContext;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;
import org.shoulder.core.util.JsonUtils;
import org.shoulder.core.util.SpringUtils;
import org.shoulder.log.operation.enums.OperationResult;
import org.shoulder.log.operation.util.OpLogContextHolder;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 批处理管理员
 * 负责分成小的任务，交给 Worker 执行
 *
 * @author lym
 */
public class BatchManager implements Runnable, ProgressAble {

    private final static Logger log = LoggerFactory.getLogger(BatchManager.class);

    /**
     * 添加数据默认单次处理最大数目
     */
    private static final int DEFAULT_MAX_TASK_SLICE_NUM = 200;

    /**
     * 异步工作单元的最大数量
     */
    private static final int MAX_WORKER_SIZE = 4;

    /**
     * 线程池
     */
    private ExecutorService threadPool = SpringUtils.getBean("batchThreadPool");

    /**
     * 批量处理记录
     */
    private BatchRecordMapper batchRecordMapper = SpringUtils.getBean(BatchRecordMapper.class);

    /**
     * 批处理记录详情
     */
    private BatchRecordDetailMapper batchRecordDetailMapper = SpringUtils.getBean(BatchRecordDetailMapper.class);

    // ------------------------------------------------

    /**
     * 操作用户
     */
    private Long userId;
    /**
     * 语言标识
     */
    private String languageId;

    /**
     * 本次要批量处理的数据
     */
    private BatchData batchData;

    /**
     * 当前进度
     */
    private BatchProgress progress;

    /**
     * 本次任务结果汇总
     */
    private BatchRecord result;

    /**
     * 任务队列，任务向这里丢
     * todo 待定 后续考虑共享队列，将属性抽出来，一个 manager 可以同时处理多次批处理任务
     */
    private BlockingQueue<BatchDataSlice> jobQueue;

    /**
     * 结果队列，结果从这里取
     */
    private BlockingQueue<BatchRecordDetail> resultQueue;


    public BatchManager(BatchData batchData) {
        this.userId = Long.valueOf(AppContext.getUserId());
        this.languageId = AppContext.getLocale().toString();
        this.batchData = batchData;
        this.batchData.setSuccessList(ListUtils.emptyIfNull(batchData.getSuccessList()));
        this.batchData.setFailList(ListUtils.emptyIfNull(batchData.getFailList()));

        // 初始化进度对象（保证在构造器中完成）
        this.progress = new BatchProgress();
        this.progress.setTaskId(UUID.randomUUID().toString());
        int total = batchData.getBatchListMap().values().stream()
            .map(List::size).reduce(Integer::sum).orElse(0);
        this.progress.setTotal(total);
        this.progress.addSuccess(batchData.getSuccessList().size());
        this.progress.addFail(batchData.getFailList().size());
    }


    /**
     * 使命 | 职责
     */
    @Override
    public void run() {
        progress.start();
        preHandle();

        // 任务分片，初始化任务队列、结果队列
        List<BatchDataSlice> taskSlice = splitTask(batchData);
        int jobSize = taskSlice.size();
        jobQueue = new LinkedBlockingQueue<>(jobSize);
        jobQueue.addAll(taskSlice);

        // 安排工人
        int needToBeProcessed = progress.getTotal() - progress.getSuccess() - progress.getFail();
        int workerNum = decideWorkerNum(needToBeProcessed, jobSize);
        resultQueue = new LinkedBlockingQueue<>(needToBeProcessed);
        log.info("taskQueue.size={}, resultQueue.size={}, workers={}", jobQueue.size(), resultQueue.size(), workerNum);

        // 开始分配任务
        for (int i = 0; i < workerNum - 1; i++) {
            BatchProcessor worker = new BatchProcessor(getTaskId(), jobQueue, resultQueue);
            if (!employWorker(worker)) {
                // 提交失败，说明当且服务器较忙，无法雇佣工人，因此中断委派，转由当前线程执行全部任务
                log.warnWithErrorCode(CommonErrorCodeEnum.SERVER_BUSY.getCode(),
                    "employ workers fail, fail back to execute by current, it may cost more time.");
                break;
            }
        }
        // 当且线程也参与做任务
        BatchProcessor worker = new BatchProcessor(getTaskId(), jobQueue, resultQueue);
        worker.run();

        // 阻塞式处理结果
        handleResult(needToBeProcessed);
        progress.finish();
        persistentImportRecord();
        fillOperationLog();
        log.info("batch task finished.");
    }

    /**
     * 预处理
     */
    private void preHandle() {
        printStartLog();

        // 初始化数据处理结果对象 Record
        int total = progress.getTotal();
        this.result = BatchRecord.builder()
            .id(getTaskId())
            .dataType(batchData.getDataType())
            .totalNum(total)
            .createTime(new Date())
            .creator(userId)
            .build();

        // 初始化数据处理详情对象 List<RecordDetail>
        List<BatchRecordDetail> detailList = new ArrayList<>(total);
        for (int i = 0; i < total; i++) {
            BatchRecordDetail detailItem = BatchRecordDetail.builder()
                .recordId(getTaskId())
                .rowNum(i)
                .build();
            // 这里认为 rowNum 唯一的，所以是 set，而非 add
            detailList.set(i, detailItem);
        }
        this.result.setDetailList(detailList);

        // 预填充数据处理详情对象 List<RecordDetail> 的待处理部分
        batchData.getBatchListMap().forEach((operationType, dataList) -> {
            for (DataItem dataItem : dataList) {
                // 这里认为 total 是所有校验的数据，若 total = 100，则不可能有 rowNum > 100 的数据
                detailList.get(dataItem.getRowNum())
                    .setResult(BatchResultEnum.VALIDATE_SUCCESS.getCode())
                    .setSource(convertObjectForExport(dataItem));
            }
        });
        // 预填充数据处理详情对象 List<RecordDetail> 的直接成功/失败部分（重复且不处理的，校验失败无法处理的）
        for (DataItem dataItem : batchData.getSuccessList()) {
            result.getDetailList().get(dataItem.getRowNum())
                .setSource(convertObjectForExport(dataItem))
                .setResult(BatchResultEnum.SKIP_REPEAT.getCode());
        }
        for (DataItem dataItem : batchData.getFailList()) {
            // getFailReason 不可能为 null，否则就是使用者错误，未塞入错误原因
            result.getDetailList().get(dataItem.getRowNum())
                .setSource(convertObjectForExport(dataItem))
                .setResult(BatchResultEnum.SKIP_REPEAT.getCode())
                .setFailReason(batchData.getFailReason().get(dataItem.getRowNum()));
        }
        log.info("Directly: success:{}, fail:{}", batchData.getSuccessList().size(), batchData.getFailList().size());
        // 可能直接完成了
        if (progress.hasFinish()) {
            this.progress.finish();
        }
    }

    /**
     * 开始前记录日志
     */
    private void printStartLog() {
        StringBuilder beginLog = new StringBuilder("batch task start, dataType=");
        beginLog.append(batchData.getDataType());
        batchData.getBatchListMap().forEach((operationType, dataList) ->
            beginLog.append(", ")
                .append(operationType).append(":").append(dataList.size())
        );
        log.info(beginLog.toString());
    }


    /**
     * 把原始数据转为 {@link BatchRecordDetail#setSource(String)} 字段
     * 筛选出部分字段，并脱敏等处理
     */
    private String convertObjectForExport(DataItem importData) {
        // 这里直接 json
        return JsonUtils.toJson(importData);
    }


    // ================================= 任务分片与执行 ==================================


    /**
     * 确定需要多少worker线程
     *
     * @param needToBeProcessed 需要处理的数据条目的大小
     * @param jobSize           需要处理的任务大小
     * @return 决定需要多少任务线程
     */
    private int decideWorkerNum(int needToBeProcessed, int jobSize) {
        // 若总数量小于默认单次处理量，则单线程处理，否则按 min(job 分片数目 / 最大工人数)
        return needToBeProcessed < DEFAULT_MAX_TASK_SLICE_NUM ? 1 :
            Integer.min(MAX_WORKER_SIZE, jobSize);
    }

    /**
     * 任务分片
     *
     * @param batchData 所有数据
     * @return 分片后的
     */
    protected List<BatchDataSlice> splitTask(BatchData batchData) {
        // 默认将每类任务划分为一片、每片最多200个
        if (MapUtils.isEmpty(batchData.getBatchListMap())) {
            return Collections.emptyList();
        }
        List<BatchDataSlice> tasks = new LinkedList<>();
        AtomicInteger sequence = new AtomicInteger(0);
        batchData.getBatchListMap().forEach((operationType, dataList) -> {
            List<? extends DataItem> toProcessedData = new ArrayList<>(dataList);
            // 切片
            List<? extends List<? extends DataItem>> pages = ListUtils.partition(toProcessedData, DEFAULT_MAX_TASK_SLICE_NUM);
            for (List<? extends DataItem> page : pages) {
                if (CollectionUtils.isNotEmpty(page)) {
                    tasks.add(new BatchDataSlice(getTaskId(), sequence.getAndIncrement(),
                        batchData.getDataType(), operationType, page)
                    );
                }
            }
        });
        return tasks;
    }

    /**
     * 雇佣工人（提交到线程池）
     *
     * @param worker 工作线程
     */
    private boolean employWorker(BatchProcessor worker) {
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
            /*
             * worker 未捕获的异常会交给 UncaughtExceptionHandler，这里设计时让worker保证一定返回结果
             */
            BatchRecordDetail taskResultDetail = takeUnExceptInterrupted(resultQueue);
            if (taskResultDetail.isCalculateProgress()) {
                boolean success = BatchResultEnum.IMPORT_SUCCESS.getCode() == taskResultDetail.getResult();
                if (success) {
                    progress.addSuccess(1);
                } else {
                    progress.addFail(1);
                }
                result.getDetailList().set(taskResultDetail.getRowNum(), taskResultDetail);
            }
        }
        if (!jobQueue.isEmpty()) {
            // 已经结束，不应该还有
            //throw new IllegalStateException("jobQueue not empty")
            log.errorWithErrorCode(CommonErrorCodeEnum.UNKNOWN.getCode(), "jobQueue not empty!");
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
        try {
            result.setSuccessNum(progress.getSuccess());
            result.setFailNum(progress.getFail());
            batchRecordMapper.insert(result);
            // todo 一致性/性能 最后保存一次？ 或在 work 中与批处理在同一事务进行保存？
            persistentBatchDetail();
        } catch (Exception e) {
            throw CommonErrorCodeEnum.PERSISTENCE_TO_DB_FAIL.toException(e);
        }
    }

    /**
     * 填充操作日志
     */
    private void fillOperationLog() {
        // 根据处理结果判断总体结果
        OperationResult result = OperationResult.of(progress.getSuccess() > 0, progress.getFail() > 0);
        OpLogContextHolder.getLog().setResult(result)
            .addDetailItem(String.valueOf(progress.getSuccess()))
            .addDetailItem(String.valueOf(progress.getFail()))
            .setObjectId(progress.getTaskId())
            .setObjectType(batchData.getDataType());
        OpLogContextHolder.enableAutoLog();
    }

    /**
     * 持久化详情信息，注意批量插入分片保存，避免大事务
     */
    protected void persistentBatchDetail() {
        batchRecordDetailMapper.batchInsertRecordDetail(result.getDetailList());
    }

    public String getTaskId() {
        return progress.getTaskId();
    }

    @Override
    public BatchProgress getBatchProgress() {
        return progress;
    }
}
