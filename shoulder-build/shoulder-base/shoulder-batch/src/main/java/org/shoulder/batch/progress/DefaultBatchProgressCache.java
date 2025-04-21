package org.shoulder.batch.progress;

import org.shoulder.core.concurrent.PeriodicTask;
import org.shoulder.core.concurrent.Threads;
import org.springframework.cache.Cache;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
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
     * 默认刷缓存时间，非本地缓存才会使用
     */
    private final Duration defaultFlushCacheTime;

    /**
     * 是否本地缓存，本地缓存不定时刷新，节省资源
     */
    private final boolean localCache;

    /**
     * 已知的本地缓存类型
     */
    private static final Set<String> localCacheType = Set.of(
        "org.springframework.cache.support.NoOpCache",
        "org.springframework.cache.concurrent.ConcurrentMapCache",
        "org.springframework.cache.caffeine.CaffeineCache");

    /**
     * 缓存名
     */
    public static final String CACHE_NAME = "shoulder-batch-progressCache_DEFAULT";

    public DefaultBatchProgressCache(Cache progressCache, Duration defaultFlushCacheTime) {
        this.progressCache = progressCache;
        this.localCache = localCacheType.contains(progressCache.getClass().getName());
        this.defaultFlushCacheTime = defaultFlushCacheTime;
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
        // TODO 【性能】P2：triggerFlushProgress从每个任务manager刷新改为固定线程刷新所有任务
        if (localCache) {
            progressCache.put(task.toProgressRecord().getId(), task);
            return;
        }
        PeriodicTask flushBatchProgressTask = new PeriodicTask() {

            private final String taskName = "flushBatchProgressTask-" + task.toProgressRecord().getId();

            private final ProgressAble progressHolder = task;

            @Override public String getTaskName() {
                return taskName;
            }

            @Override public void process() {
                BatchProgressRecord batchProgressRecord = progressHolder.toProgressRecord();
                String id = batchProgressRecord.getId();
                if (batchProgressRecord.hasFinish()) {
                    // 处理完毕，更新状态
                    progressHolder.onFinished(id, progressHolder);
                }
                // 刷缓存
                progressCache.put(id, batchProgressRecord);
            }

            @Override public Instant calculateNextRunTime(Instant now, int runCount) {
                return task.toProgressRecord().hasFinish() ? NO_NEED_EXECUTE : now.plus(defaultFlushCacheTime);
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
     * @param progress 进度
     */
    @Override
    public synchronized void flushProgress(ProgressAble progress) {
        if (localCache) {
            progressCache.put(progress.toProgressRecord().getId(), progress);
            return;
        }
        BatchProgressRecord record = progress.toProgressRecord();
        progressCache.put(record.getId(), record);
    }

    @SuppressWarnings("unechked")
    @Override
    public Iterable<String> fetchAllTaskProgressId() {
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
    public Map<String, Progress> fetchAllTaskProgress() {
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
    public Progress findProgress(String progressId) {
        Cache.ValueWrapper valueWrapper = progressCache.get(progressId);
        return valueWrapper == null ? null : (Progress) valueWrapper.get();
    }

}
