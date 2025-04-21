package org.shoulder.batch.progress;

import org.shoulder.core.util.ContextUtils;
import org.springframework.core.GenericTypeResolver;

import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * 枚举形式 BatchActivity
 *
 * @author lym
 */
public interface BatchActivityEnum<E extends Enum<? extends BatchActivityEnum<?>>> {

    static BatchProgressCache progressCache() {
        if(!ContextUtils.hasContextRefreshed()) {
            throw new IllegalStateException("unSupport access progressCache util app context refreshed in default. pleas wait app started.");
//            new DefaultBatchProgressCache(new ConcurrentMapCache("DefaultBatchProgressCache"), Duration.ofSeconds(5));
        }
        return ContextUtils.getBean(BatchProgressCache.class);

    }

    String getKey();

    boolean hasSubTask();

    String getDisplayName();

    default BatchProgress findProgressOrCreate(String progressId) {
        BatchProgressCache progressCache = progressCache();

        String realProgressId = genCacheKey(progressId);// DCL create
        Progress cached = progressCache.findProgress(realProgressId);
        if (cached != null) {
            return (BatchProgress) cached;
        }
        synchronized (this) {
            cached = progressCache.findProgress(realProgressId);
            if (cached != null) {
                return (BatchProgress) cached;
            }
            BatchProgress progress = new BatchProgress();
            progress.setId(realProgressId);
            progressCache.triggerFlushProgress(progress);
            return progress;
        }
    }

    default String genCacheKey(String prefix) {
        return prefix + getKey();
    }

    default void start(String progressId) {
        findProgressOrCreate(progressId).start();
    }

    default void failStop(String progressId) {
        findProgressOrCreate(progressId).failStop();
    }

    default void finish(String progressId) {
        // fixme 特殊判断 如果状态是等待中，则先开始再结束
        findProgressOrCreate(progressId).finish();
    }

    default void addSuccess(String progressId) {
        findProgressOrCreate(progressId).addSuccess(1);
    }

    default boolean hasFinish(String progressId) {
        return findProgressOrCreate(progressId).hasFinish();
    }

    default Long calculateProcessedTime(String progressId) {
        return findProgressOrCreate(progressId).calculateProcessedTime();
    }

    default float calculateProgress(String progressId) {
        return findProgressOrCreate(progressId).calculateProgress();
    }

    default long calculateTimeLeft(String progressId) {
        return findProgressOrCreate(progressId).calculateTimeLeft();
    }

    default BatchProgress getBatchProgress(String progressId) {
        return findProgressOrCreate(progressId);
    }

    default String getId(String progressId) {
        return findProgressOrCreate(progressId).getId();
    }

    default void finishPart(String progressId, int partIndex) {

        findProgressOrCreate(progressId).finishPart(partIndex);
    }

    default void startOneStageTask(String progressId) {
        setTotalAndStart(progressId, 1);
    }

    default void setTotalAndStart(String progressId, int total) {
        findProgressOrCreate(progressId).setTotalAndStart(total);
    }

    default void setOnFinishCallback(String progressId, BiConsumer<String, Progress> onFinishedCallback) {
        findProgressOrCreate(progressId).setOnFinishCallback(onFinishedCallback);
    }

    static Class<?> resovleEnumClass(Class<?> realClass) {
        //第二个泛型是itemId 类型
        return Optional.ofNullable(GenericTypeResolver.resolveTypeArguments(realClass, BatchActivityEnum.class))
                .orElseThrow()[0];
    }

    default Class<?> getEnumClass() {
        //第二个泛型是itemId 类型
        return resovleEnumClass(getClass());
    }

    /**
     * 在第几个处理流程展示
     *
     * @return 0, 1, 2,3...
     */
    String getDisplayEmoji();

    /**
     * 在第几个处理流程展示
     *
     * @return 0, 1, 2,3...
     */
    int displayBlockNum();

    /**
     * 在第几列展示
     *
     * @return 0:串行节点;1.并行节点的第一列2.并行节点的第二列...
     */
    int getDisplayColumnNum();

    default boolean isEndStep() {
        return ((Enum<?>) this).ordinal() == getClass().getEnumConstants().length - 1;
    }
}