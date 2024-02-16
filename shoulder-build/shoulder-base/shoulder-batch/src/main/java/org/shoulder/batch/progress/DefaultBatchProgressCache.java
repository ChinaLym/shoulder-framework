package org.shoulder.batch.progress;

import org.shoulder.core.concurrent.Threads;
import org.springframework.cache.Cache;

import java.util.Map;
import java.util.concurrent.TimeUnit;
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
        BatchProgressRecord batchProgressRecord = task.getBatchProgress();
        progressCache.put(batchProgressRecord.getId(), batchProgressRecord);
        Threads.execute(genFlushProgressTask(task));
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

    @Override
    public Map<String, ProgressAble> getAllTaskProgress() {
        Object nativeCache = getNativeCache();
        if (nativeCache instanceof Map) {
            Map<Object, Object> nMap = ((Map<Object, Object>) nativeCache);
            return nMap.entrySet().stream()
                    .collect(Collectors.toMap(e -> String.valueOf(e.getKey()),
                            e -> (ProgressAble) e.getValue())
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
    public ProgressAble getTaskProgress(String progressId) {
        Cache.ValueWrapper valueWrapper = progressCache.get(progressId);
        return valueWrapper == null ? null : (ProgressAble) valueWrapper.get();
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
            String id = batchProgressRecord.getId();
            if (!batchProgressRecord.hasFinish()) {
                // 未处理完毕，仍需要执行这个任务
                Threads.delay(genFlushProgressTask(progressHolder), 1, TimeUnit.SECONDS);
            } else {
                progressHolder.onFinished(id, progressHolder);
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
            String id = batchProgress.getId();
            if (!batchProgress.hasFinish()) {
                // 未处理完毕，仍需要执行这个任务
                Threads.delay(genFlushProgressTask(batchProgress), 1, TimeUnit.SECONDS);
            }
            progressCache.put(id, batchProgress);
        };
    }

}
