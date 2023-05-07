package org.shoulder.batch.cache;

import org.shoulder.batch.model.BatchProgressRecord;
import org.shoulder.batch.service.impl.ProgressAble;
import org.shoulder.core.concurrent.Threads;
import org.springframework.cache.Cache;
import org.springframework.cache.concurrent.ConcurrentMapCache;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 任务进度存储
 *
 * @author lym
 */
public class DefaultBatchProgressCache implements BatchProgressCache {

    /**
     * 允许进度以多种方式保存在其他地方，如 redis
     */
    private final Cache progressCache;

    /**
     * 缓存名
     */
    public static final String CACHE_NAME = "shoulder-batch-progressCache_DEFAULT";

    public DefaultBatchProgressCache(Cache progressCache) {
        this.progressCache = progressCache;
    }

    /**
     * 触发异步刷进度
     *
     * @param task 需要被刷进度的 task
     */
    @Override
    public void triggerFlushProgress(ProgressAble task) {
        BatchProgressRecord batchProgressRecord = task.getBatchProgress();
        progressCache.put(batchProgressRecord.getTaskId(), batchProgressRecord);
        Threads.execute(genFlushProgressTask(task));
    }

    @Override
    public void triggerFlushProgress(BatchProgressRecord batchProgress) {
        progressCache.put(batchProgress.getTaskId(), batchProgress);
        Threads.execute(genFlushProgressTask(batchProgress));
    }

    public List<String> getAllProcessId() {
        if (ConcurrentMapCache.class.isAssignableFrom(progressCache.getClass())) {
            return ((ConcurrentMapCache) progressCache).getNativeCache().keySet().stream().map(String::valueOf).collect(Collectors.toList());
        }
        throw new UnsupportedOperationException();
//        return Collections.emptyList();
    }

    public Map<String, BatchProgressRecord> getAllProgress() {
        if (ConcurrentMapCache.class.isAssignableFrom(progressCache.getClass())) {
            return ((ConcurrentMapCache) progressCache).getNativeCache().entrySet().stream()
                    .collect(Collectors.toMap(e -> String.valueOf(e.getKey()),
                            e -> (BatchProgressRecord) e.getValue())
                    );
        }
        throw new UnsupportedOperationException();
//        return Collections.emptyList();
    }

    /**
     * 获取任务进度
     *
     * @param id taskId
     * @return 任务
     */
    @Override
    public BatchProgressRecord getTaskProgress(String id) {
        Cache.ValueWrapper valueWrapper = progressCache.get(id);
        return valueWrapper == null ? null : (BatchProgressRecord) valueWrapper.get();
    }

    /**
     * 创建一个刷进度的任务
     *
     * @param progressHolder 需要被刷进度的任务
     * @return 刷进度的任务
     */
    private Runnable genFlushProgressTask(ProgressAble progressHolder) {
        return () -> {
            BatchProgressRecord batchProgressRecord = progressHolder.getBatchProgress();
            String id = batchProgressRecord.getTaskId();
            if (!batchProgressRecord.hasFinish()) {
                // 未处理完毕，仍需要执行这个任务
                Threads.delay(genFlushProgressTask(progressHolder), 1, TimeUnit.SECONDS);
            } else {
                progressHolder.afterFinished(id, progressHolder);
            }
            progressCache.put(id, batchProgressRecord);
        };
    }

    /**
     * 创建一个刷进度的任务
     *
     * @param batchProgress 进度条
     * @return 刷进度的任务
     */
    private Runnable genFlushProgressTask(BatchProgressRecord batchProgress) {
        return () -> {
            String id = batchProgress.getTaskId();
            if (!batchProgress.hasFinish()) {
                // 未处理完毕，仍需要执行这个任务
                Threads.delay(genFlushProgressTask(batchProgress), 1, TimeUnit.SECONDS);
            }
            progressCache.put(id, batchProgress);
        };
    }

}
