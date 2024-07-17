package org.shoulder.batch.progress;

import org.shoulder.core.concurrent.PeriodicTask;
import org.shoulder.core.concurrent.Threads;
import org.springframework.cache.Cache;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 任务进度存储：单机存储
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

    @SuppressWarnings("unchecked")
    public <T> T getNativeCache() {
        return (T) progressCache.getNativeCache();
    }

    /**
     * 触发异步刷进度
     * 备注：场景适用于单个机器写，多个机器读
     *
     * @param task 需要被刷进度的 task
     */
    @Override
    public void triggerFlushProgress(ProgressAble task) {
        PeriodicTask flushBatchProgressTask = new PeriodicTask() {

            private final String taskName = "flushBatchProgressTask-" + task.getBatchProgress().getId();

            private final ProgressAble progressHolder = task;

            @Override public String getTaskName() {
                return taskName;
            }

            @Override public void process() {
                BatchProgressRecord batchProgressRecord = progressHolder.getBatchProgress();
                String id = batchProgressRecord.getId();
                if (batchProgressRecord.hasFinish()) {
                    // 处理完毕，更新状态
                    progressHolder.onFinished(id, progressHolder);
                }
                // 刷缓存
                progressCache.put(id, batchProgressRecord);
            }

            @Override public Instant calculateNextRunTime(Instant now, int runCount) {
                return task.getBatchProgress().hasFinish() ? NO_NEED_EXECUTE : now.plus(Duration.ofSeconds(2));
            }
        };
        Threads.schedule(flushBatchProgressTask, Instant.now());
    }

    @Override
    public void evict(String id) {
        progressCache.evict(id);
    }

    @Override
    public void clear() {
        progressCache.clear();
    }

    /**
     * 刷缓存线程不安全，需要加锁，最少是 batchId 级别，这里默认实现直接用 sync 了
     *
     * @param batchProgress 进度
     */
    @Override
    public synchronized void flushProgress(ProgressAble batchProgress) {
        BatchProgressRecord record = batchProgress.getBatchProgress();
        progressCache.put(record.getId(), record);
    }

    @SuppressWarnings("unechked")
    @Override
    public Iterable<String> getAllTaskProgressId() {
        Object nativeCache = getNativeCache();
        if (nativeCache instanceof Map) {
            Map<Object, Object> nMap = ((Map<Object, Object>) nativeCache);
            return nMap.keySet().stream().map(String::valueOf).collect(Collectors.toSet());
        }
//        if (ConcurrentMapCache.class.isAssignableFrom(progressCache.getClass())) {
//
//            return ((ConcurrentMapCache) progressCache).getNativeCache().keySet().stream().map(String::valueOf).collect(Collectors.toSet());
//        }
        throw new UnsupportedOperationException();
//        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Progress> getAllTaskProgress() {
        Object nativeCache = getNativeCache();
        if (nativeCache instanceof Map) {
            Map<Object, Object> nMap = ((Map<Object, Object>) nativeCache);
            return nMap.entrySet().stream()
                    .collect(Collectors.toMap(e -> String.valueOf(e.getKey()),
                            e -> (Progress) e.getValue())
                    );
        }
//        if (ConcurrentMapCache.class.isAssignableFrom(progressCache.getClass())) {
//            return ((ConcurrentMapCache) progressCache).getNativeCache().entrySet().stream()
//                    .collect(Collectors.toMap(e -> String.valueOf(e.getKey()),
//                            e -> (ProgressAble) e.getValue())
//                    );
//        }
        throw new UnsupportedOperationException();
//        return Collections.emptyList();
    }

    /**
     * 获取任务进度
     *
     * @param progressId progressId
     * @return 任务
     */
    @Override
    public Progress getProgress(String progressId) {
        Cache.ValueWrapper valueWrapper = progressCache.get(progressId);
        return valueWrapper == null ? null : (Progress) valueWrapper.get();
    }

}
