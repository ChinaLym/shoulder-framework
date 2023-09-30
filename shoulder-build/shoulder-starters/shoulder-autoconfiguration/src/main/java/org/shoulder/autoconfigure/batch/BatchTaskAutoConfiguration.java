package org.shoulder.autoconfigure.batch;

import com.univocity.parsers.csv.CsvWriter;
import org.shoulder.autoconfigure.condition.ConditionalOnCluster;
import org.shoulder.batch.cache.BatchProgressCache;
import org.shoulder.batch.cache.DefaultBatchProgressCache;
import org.shoulder.batch.constant.BatchConstants;
import org.shoulder.batch.model.BatchData;
import org.shoulder.batch.repository.BatchRecordDetailPersistentService;
import org.shoulder.batch.repository.BatchRecordPersistentService;
import org.shoulder.batch.repository.JdbcBatchRecordDetailPersistentService;
import org.shoulder.batch.repository.JdbcBatchRecordPersistentService;
import org.shoulder.batch.service.BatchAndExportService;
import org.shoulder.batch.service.impl.CsvExporter;
import org.shoulder.batch.service.impl.DataExporter;
import org.shoulder.batch.service.impl.DefaultBatchExportService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

/**
 * 批处理相关自动装配
 * 默认只提供 BatchProgressCache，方便使用管理进度，未激活其他功能
 *
 * @author lym
 */
@ConditionalOnClass(BatchData.class)
@AutoConfiguration
public class BatchTaskAutoConfiguration {

    public BatchTaskAutoConfiguration() {
        // just for debug
    }

    /**
     * csvImpl
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(CsvWriter.class)
    public CsvExporter csvExporter() {
        return new CsvExporter();
    }


    /**
     * service
     */
    @Bean
    @ConditionalOnBean(DataExporter.class)
    @ConditionalOnMissingBean
    public BatchAndExportService batchAndExportService() {
        return new DefaultBatchExportService();
    }

    /**
     * 批处理线程池
     */
    @Bean(BatchConstants.BATCH_THREAD_POOL_NAME)
    @ConditionalOnMissingBean(name = BatchConstants.BATCH_THREAD_POOL_NAME)
    public ThreadPoolExecutor shoulderBatchThreadPool() {
        // 默认使用 5 个线程
        return new ThreadPoolExecutor(5, 5,
                60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(3000),
                new CustomizableThreadFactory("shoulder-batch"));
    }

    /**
     * 进度缓存
     * 非集群使用本地缓存
     */
    @Bean
    @ConditionalOnCluster(cluster = false)
    @ConditionalOnMissingBean
    public BatchProgressCache defaultBatchProgressCache() {
        return new DefaultBatchProgressCache(new ConcurrentMapCache(DefaultBatchProgressCache.CACHE_NAME));
    }

    /**
     * 进度缓存【说明，该注入方式无法通过 cache.getProgressId 写，且只能单节点写】
     * 集群模式从缓存管理器中获取
     * CacheManager 的、来源：1. @EnableCaching 2. 自己new
     * 如果不用自动装配，则自己造 BatchProgressCache 或者关掉该 batch 功能
     */
    @Bean
    @ConditionalOnCluster
    @ConditionalOnMissingBean
    @ConditionalOnBean(CacheManager.class)
    public BatchProgressCache redisBatchProgressCache(CacheManager cacheManager) {
        return new DefaultBatchProgressCache(cacheManager.getCache(DefaultBatchProgressCache.CACHE_NAME));
    }


    @AutoConfiguration
    @ConditionalOnClass(DataSource.class)
    @ConditionalOnProperty(name = "shoulder.batch.record.persistent.type", havingValue = "jdbc", matchIfMissing = true)
    static class JdbcLockAutoConfiguration {

        /**
         * jdbc
         */
        @Bean
        @ConditionalOnBean(DataSource.class)
        @ConditionalOnMissingBean
        public BatchRecordPersistentService batchRecordPersistentService(DataSource dataSource) {
            return new JdbcBatchRecordPersistentService(dataSource);
        }

        /**
         * jdbc
         */
        @Bean
        @ConditionalOnBean(DataSource.class)
        @ConditionalOnMissingBean
        public BatchRecordDetailPersistentService batchRecordDetailPersistentService(DataSource dataSource) {
            return new JdbcBatchRecordDetailPersistentService(dataSource);
        }
    }

}
