package org.shoulder.data.dal.sequence.dao;

import lombok.Getter;
import lombok.Setter;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.util.AssertUtils;
import org.shoulder.data.dal.sequence.XDataSource;
import org.shoulder.core.log.LoggerFactory;

import org.shoulder.core.util.StringUtils;
import org.shoulder.data.dal.sequence.dialect.SequenceSqlDialect;
import org.shoulder.data.dal.sequence.model.SequenceRange;
import org.shoulder.data.dal.sequence.model.DoubleSequenceRange;
import org.shoulder.data.dal.sequence.monitor.SequenceMonitorThreadBuilder;
import org.shoulder.data.dal.sequence.exceptions.SequenceException;
import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 额外增加了 重试、锁、指标记录、双buffer cache
 *
 * @author lym
 */
public abstract class AbstractCacheAndRetryableSequenceDao implements IGenericSequenceDao, InitializingBean {

    public static final String JDBC_PARAMETER_BINDING_SYMBOL = "?";

    protected static final Logger logger = LoggerFactory.getLogger("EXECUTION");

    protected static final Logger monitorLogger = LoggerFactory.getLogger("MONITOR");


    // ---------------------------------------config----------------------------------------------

    /**
     * Retry times this DAO try to update sequence until success
     * 若DB数据为空，假设10个进程同时获取，同时进入初始化，都会尝试 select or insert，但只有一个成功，这时可以重试这个过程。
     */
    @Getter
    @Setter
    protected int maxRetryTimes = 2;

    @Getter
    @Setter
    private long minValue = 1;

    @Getter
    @Setter
    private long maxValue = 99999999;

    @Getter
    @Setter
    private long step = 1000;

    @Getter
    @Setter
    private long cacheSize = SequenceRangeCache.DEFAULT_CACHE_SIZE;

    @Getter
    @Setter
    private long cacheExpireSeconds = SequenceRangeCache.DEFAULT_CACHE_EXPIRE_SECONDS;

    /**
     * cache
     */
    @Getter
    private SequenceRangeCache sequenceRangeCache;

    /**
     * key: sequenceName-databaseId-tableId: user-1-1
     * value: sequence operation lock
     */
    @Getter
    private ConcurrentHashMap<String, Semaphore> sequenceSemaphoreMap = new ConcurrentHashMap<>();

    @Getter
    protected XDataSource dataSource;

    private TransactionTemplate transactionTemplate;

    @Getter
    @Setter
    private String sequenceTableName;

    @Getter
    @Setter
    private List<String> sequenceShardingColumnNames = new ArrayList<>();

    private AtomicBoolean sequenceMonitorInited = new AtomicBoolean(false);


    /**
     * 创建序列
     */
    abstract void insertSequenceRange(SequenceRange sequenceRange);

    /**
     * 更新序列
     */
    abstract int updateSequenceRange(SequenceRange remoteSequenceRange);

    /**
     * 查找序列
     */
    abstract SequenceRange selectSequenceRange(SequenceRange localSequenceRange);

    @Override
    public void afterPropertiesSet() throws Exception {
        initialize();
    }

    /**
     * 初始化
     */
    @Override
    public void initialize() throws Exception {
        this.sequenceRangeCache = new SequenceRangeCache(cacheSize, cacheExpireSeconds);
        if (sequenceMonitorInited.compareAndSet(false, true)) {
            SequenceMonitorThreadBuilder.build(dataSource, sequenceRangeCache, sequenceSemaphoreMap, this).start();
        }
    }

    /**
     * 先尝试从缓存拿，再从 DB 拿
     */
    @Override
    public SequenceRange getNextSequence(String sequenceName,
                                         List<Object> shardingParameters)
        throws Exception {

        // sharding
        String sequenceSourceId = computeSequenceLockId(sequenceName);
        Semaphore semaphore = sequenceSemaphoreMap.computeIfAbsent(sequenceSourceId, id -> new Semaphore(1));
        DoubleSequenceRange latestSequenceRange = sequenceRangeCache.get(sequenceSourceId);

        SequenceRange currentSequenceRange = latestSequenceRange != null ? latestSequenceRange.getCurrent() : null;
        if (null == currentSequenceRange) {
            // Initialize
            SequenceRange sequenceRange = initializeSequenceRangeInDbAndCache(sequenceName, latestSequenceRange, sequenceSourceId, semaphore);
            if (null != sequenceRange) {
                return sequenceRange;
            }
        } else if (currentSequenceRange.needRefresh()) {
            // refresh
            SequenceRange sequenceRange = refreshNextSequenceRange(latestSequenceRange, sequenceSourceId, semaphore);
            if (null != sequenceRange) {
                return sequenceRange;
            }
        } else {
            // Keeping using current
            return currentSequenceRange;
        }
        throw new SequenceException("Sequence Error: Fail to use or create sequence: " + sequenceName);
    }

    /**
     * 当前内存缓存用完了，去数据库取一段放内存缓存
     */
    private SequenceRange refreshNextSequenceRange(DoubleSequenceRange latestSequenceRange,
                                                   String sequenceSourceId,
                                                   Semaphore semaphore) throws Exception {
        if (!semaphore.tryAcquire(500, TimeUnit.MILLISECONDS)) {
            String errorMsg = "Sequence Error: Fail to refreshNextSequenceRange(try lock TimeOut): " + latestSequenceRange;
            monitorLogger.error(errorMsg);

            // 先返回当前的，可能已经刷新好了
            return latestSequenceRange.getCurrent();
        }
        try {
            latestSequenceRange = sequenceRangeCache.get(sequenceSourceId);
            if (latestSequenceRange != null && latestSequenceRange.getCurrent() != null && !latestSequenceRange.getCurrent().needRefresh()) {
                // 当前缓存未耗尽（其他线程加载好了），仍然可用
                return latestSequenceRange.getCurrent();
            }

            // latestSequenceRange 为 null 的可能性就是 range 用光了，再次获取的时候缓存又失效。可能性非常小
            SequenceRange currentSequenceRange = latestSequenceRange.getCurrent();
            SequenceRange nextSequenceRange = latestSequenceRange.switchNextAndGet();

            if (nextSequenceRange == null || nextSequenceRange.needRefresh()) {
                // 双 buffer 另一个为空，或者需要刷新，这里提前刷新
                SequenceRange newSequenceRange = loadNextSequenceFromDbViaNewTransaction(sequenceSourceId, currentSequenceRange);
                latestSequenceRange.setAndSwitchNext(newSequenceRange);

                AssertUtils.notNull(newSequenceRange, CommonErrorCodeEnum.CODING);
                sequenceRangeCache.put(sequenceSourceId, latestSequenceRange);
                return newSequenceRange;
            } else {
                return nextSequenceRange;
            }
        } catch (Exception e) {
            monitorLogger.error("Fail to refreshNextSequenceRange: ", latestSequenceRange, e);
            throw e;
        } finally {
            semaphore.release();
        }

    }

    /**
     * 初始化时执行：
     * 1. 初始化DB（尝试先查再insert）
     * 2. 初始化缓存
     */
    private SequenceRange initializeSequenceRangeInDbAndCache(String sequenceName,
                                                              DoubleSequenceRange latestSequenceRange,
                                                              String sequenceSourceId,
                                                              Semaphore semaphore) throws Exception {
        if (!semaphore.tryAcquire(500, TimeUnit.MILLISECONDS)) {
            String errorMsg = "Sequence Error: Fail to initializeSequenceRange(try lock TimeOut): " + sequenceName;
            monitorLogger.error(errorMsg);
            throw new SequenceException(errorMsg);
        }
        try {
            latestSequenceRange = sequenceRangeCache.get(sequenceSourceId);
            boolean alreadyInit = latestSequenceRange != null && latestSequenceRange.getCurrent() != null;
            if (alreadyInit) {
                return latestSequenceRange.getCurrent();
            }

            // create New
            SequenceRange sequenceRange = createNewSequenceRange(sequenceName);
            sequenceRange.setSequenceSourceId(sequenceSourceId);
            // ensure db exist or create
            initSequenceInDbViaNewTransaction(sequenceRange);

            // put Cache
            if (latestSequenceRange == null) {
                latestSequenceRange = new DoubleSequenceRange();
            }
            latestSequenceRange.setAndSwitchNext(sequenceRange);
            sequenceRangeCache.put(sequenceSourceId, latestSequenceRange);

            return latestSequenceRange.getCurrent();
        } catch (Exception e) {
            monitorLogger.error("Sequence Error: Fail to initializeSequenceRange(From DB): " + latestSequenceRange, e);
            throw e;
        } finally {
            semaphore.release();
        }
    }

    /**
     * we start new transaction to operate
     *
     * @param sequenceRange
     */
    public void initSequenceInDbViaNewTransaction(final SequenceRange sequenceRange) {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {

            /**
             * @see org.springframework.transaction.support.TransactionCallbackWithoutResult#doInTransactionWithoutResult(org.springframework.transaction.TransactionStatus)
             */
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                    initSequenceInDb(sequenceRange);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    // todo clean context
                }
            }
        });
    }

    /**
     * 开新事务去DB拿一段
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public SequenceRange loadNextSequenceFromDbViaNewTransaction(final String sequenceSourceId,
                                                                 final SequenceRange localSequenceRange) {
        return (SequenceRange) transactionTemplate.execute((TransactionCallback) status -> {
            try {
                return doLoadNextSequenceFromDb(sequenceSourceId, localSequenceRange);
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                // clean context
            }
        });
    }

    /**
     * Calculate the where the new sequence should start up and update the change into Database under the defined retry
     * time. Then return the latest sequence.
     */
    protected SequenceRange doLoadNextSequenceFromDb(String sequenceSourceId,
                                                     SequenceRange localSequenceRange)
        throws SequenceException {
        SequenceRange remoteSequenceRange;
        try {
            // setSqlRouterInfo(localSequenceRange);
            for (int i = 1; i <= maxRetryTimes; i++) {
                // 从数据库拿到最近的值
                remoteSequenceRange = selectSequenceRange(localSequenceRange);

                // 更新下一段值（当前value + step）
                long stepValue = remoteSequenceRange.getStep();
                adjustValue(null, remoteSequenceRange);
                // 在数据库执行更新
                int affectedRows = updateSequenceRange(remoteSequenceRange);

                if (affectedRows <= 0) {
                    // 更新失败，大概率是热点（其他实例也在更新），如果频繁出现可以调大 step、或辅助预热缓解
                    monitorLogger.error("Fail to update sequence(" + remoteSequenceRange.getName() + ")"
                        + ", oldValue=" + remoteSequenceRange.getValue()
                        + ", newValue=" + remoteSequenceRange.getLatestValue());
                    continue;
                }
                // 更新成功，可以适用
                remoteSequenceRange.cloneContextValue(localSequenceRange);
                remoteSequenceRange.setSequenceSourceId(sequenceSourceId);
                remoteSequenceRange.setFetchDate(new Date());

                printUpdateLogWhenUpdateDb(true, remoteSequenceRange, stepValue);
                return remoteSequenceRange;
            }
        } catch (Throwable t) {
            String msg = "Fail to loadNextSequenceFromDb: " + localSequenceRange;
            monitorLogger.error(msg, t);
            throw new SequenceException(msg, t);
        }
        throw new SequenceException("Failed to fetch next sequence range within try times "
            + this.maxRetryTimes);
    }

    protected void printUpdateLogWhenUpdateDb(boolean isSuccessful,
                                              SequenceRange sequenceRange, long stepValue) {
        logger.warn("load sequence(" + sequenceRange.getSequenceSourceId() + ") from db, result=" + isSuccessful
            + ", step=" + stepValue + ", randomStep=" + sequenceRange.getStep()
            + ", oldValue=" + sequenceRange.getValue() + ", newValue=" + sequenceRange.getLatestValue());
    }

    /**
     * 将sequenceRange 的value 改为正确的，确保其在 [min, max] 范围内
     */
    private long adjustValue(SequenceRange localSequenceRange,
                             SequenceRange remoteSequenceRange) {
        long newDbValue = 0, stepValue = remoteSequenceRange.getStep();
        if (remoteSequenceRange.getValue() == remoteSequenceRange.getMax()) {
            // Enable to restart from min value ?
            newDbValue = remoteSequenceRange.getMin();// Start from min
            if (localSequenceRange != null) {
                localSequenceRange.setValue(remoteSequenceRange.getValue());
            }
        } else if (remoteSequenceRange.getValue() < remoteSequenceRange.getMin()) {
            newDbValue = remoteSequenceRange.getMin() + remoteSequenceRange.getStep();
            if (localSequenceRange != null) {
                localSequenceRange.setValue(remoteSequenceRange.getMin());
            }
        } else {
            // The latest value = prior value + prior step + new step
            stepValue = randomUpStep(remoteSequenceRange.getStep());
            newDbValue = remoteSequenceRange.getValue() + stepValue;

            if (newDbValue >= remoteSequenceRange.getMax())
                newDbValue = remoteSequenceRange.getMin();
            if (localSequenceRange != null) {
                localSequenceRange.setValue(remoteSequenceRange.getValue());
            }
            remoteSequenceRange.setStep(stepValue);
        }
        // Ensure this calculate
        remoteSequenceRange.setLatestValue(newDbValue);
        return stepValue;
    }

    protected long randomUpStep(long step) {
        // 每次获取时候各实例布长一定随机性，避免同时 sequence 用尽争抢db锁
        return Math.round(step * (1F + ThreadLocalRandom.current().nextFloat(0.3f)));
    }

    /**
     * select == null ? insert : return
     */
    public void initSequenceInDb(SequenceRange sequenceRange) {
        for (int i = 1; i <= maxRetryTimes; i++) {
            // setSqlRouterInfo(sequenceRange);
            try {
                SequenceRange queriedSequenceRange = selectSequenceRange(sequenceRange);
                if (queriedSequenceRange == null) {
                    // DB 不存在，尝试初始化并插入，当且仅当app在（该分库分表）第一次用该sequence时
                    sequenceRange.setValue(sequenceRange.getMin() + sequenceRange.getStep());
                    insertSequenceRange(sequenceRange);
                    sequenceRange.setValue(sequenceRange.getMin());
                    sequenceRange.setSystemDate(new Date());
                    sequenceRange.setFetchDate(new Date());
                } else {
                    // 绝大部分情况：DB 中已经创建过了，只是内存还没有，需要获取下一段
                    long stepValue = queriedSequenceRange.getStep();
                    adjustValue(sequenceRange, queriedSequenceRange);
                    // queriedSequenceRange.setRouteCondition(sequenceRange.getRouteCondition());
                    int affectedRows = updateSequenceRange(queriedSequenceRange);
                    if (affectedRows <= 0) {
                        // 更新失败，可能有其他进程已经用了该分段了，打印日志，并重试
                        monitorLogger.warn("Warn ## Update sequence "
                            + queriedSequenceRange.getName() + " failed, oldValue="
                            + queriedSequenceRange.getValue() + ",newValue="
                            + queriedSequenceRange.getLatestValue() + ", please retry.");
                        continue;
                    } else {
                        // 绝大部份情况还是会更新成功，拿到这些 sequence
                        sequenceRange.setMax(queriedSequenceRange.getMax());
                        sequenceRange.setMin(queriedSequenceRange.getMin());
                        sequenceRange.setSystemDate(queriedSequenceRange.getSystemDate());
                        sequenceRange.setFetchDate(new Date());
                        sequenceRange.setStep(queriedSequenceRange.getStep());
                        sequenceRange.setLatestValue(queriedSequenceRange.getLatestValue());
                        printUpdateLogWhenUpdateDb(true, sequenceRange, stepValue);
                    }
                }
                return;
            } catch (Exception e) {
                int remainingRetries = maxRetryTimes - i;
                monitorLogger.warn("Failed to init sequenceInDb(DB exception, remainingRetries=" + remainingRetries + "): " + sequenceRange, e);
                throw new SequenceException("Failed to init sequenceInDb(retry " + this.maxRetryTimes + " times): " + sequenceRange
                    + sequenceRange.getSequenceSourceId(), e);
            }
        }
        // 重试几次也没成功，抛异常
        throw new SequenceException("Failed to init sequenceInDb(retry " + this.maxRetryTimes + " times): " + sequenceRange);
    }

    private String computeSequenceLockId(String sequenceName) {
        // todo loadtest mirror
        // return sequenceName + "_" + "01" + "_" + "01";
        return sequenceName;
    }

    protected void setSqlRouterInfo(SequenceRange sequenceRange) {
        // 设置分库分表，影子表等信息
    }

    private SequenceRange createNewSequenceRange(String sequenceName) {
        SequenceRange sequenceRange = new SequenceRange(
            sequenceName, getMinValue(), getMaxValue(), getStep(), 0);
        // 设置上下文信息 todo 分库分表信息
        // priorSequenceRange.captureAppContext();
        return sequenceRange;
    }


    public synchronized void setDataSource(XDataSource dataSource) {
        this.dataSource = dataSource;
        transactionTemplate = new TransactionTemplate(new DataSourceTransactionManager(dataSource));
        // always use new transaction
        transactionTemplate.setPropagationBehaviorName("PROPAGATION_REQUIRES_NEW");
    }


    /**
     * 替换sql中的表名称
     *
     * @param templateSql sql
     * @return completeSQL
     */
    protected String replaceTableName(String templateSql) {
        return StringUtils.replace(templateSql, SequenceSqlDialect.PLACEHOLDER_TABLE_NAME, getSequenceTableName());
    }


    /**
     * 替换sql中的列名称
     *
     * @param templateSql sql
     * @return completeSQL
     */
    protected String replaceColumnNames(String templateSql) {
        if (StringUtils.isEmpty(templateSql) || null == getSequenceShardingColumnNames())
            return templateSql;
        StringJoiner sj = new StringJoiner(",");
        getSequenceShardingColumnNames().forEach(sj::add);
        return StringUtils.replace(templateSql, SequenceSqlDialect.PLACEHOLDER_SHARDING_COLUMNS, sj.toString());
    }

    /**
     * 替换sql中的列名称
     *
     * @param templateSql sql
     * @return completeSQL
     */
    protected String replaceColumnValuesOnInsert(String templateSql) {
        if (StringUtils.isEmpty(templateSql) || null == getSequenceShardingColumnNames())
            return templateSql;
        int count = 0, size = getSequenceShardingColumnNames().size();
        StringBuilder strBuilder = new StringBuilder();
        while (++count < size) {
            strBuilder.append(SequenceSqlDialect.SQL_PARAMETER_BINDING_CHAR).append(",");
        }
        strBuilder.append(SequenceSqlDialect.SQL_PARAMETER_BINDING_CHAR);
        return StringUtils.replace(templateSql, SequenceSqlDialect.PLACEHOLDER_SHARDING_COLUMN_VALUES, strBuilder.toString());
    }

    public void setSequenceShardingColumnNames(String sequenceShardingColumnNameStr) {
        if (StringUtils.isEmpty(sequenceShardingColumnNameStr)) {
            return;
        }
        Arrays.stream(StringUtils.split(sequenceShardingColumnNameStr.trim(), ","))
            .map(String::trim)
            .peek(this.sequenceShardingColumnNames::add);
    }


}
