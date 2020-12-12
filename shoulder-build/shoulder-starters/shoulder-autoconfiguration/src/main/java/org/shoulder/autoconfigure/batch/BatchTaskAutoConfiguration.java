package org.shoulder.autoconfigure.batch;

import org.shoulder.autoconfigure.condition.ConditionalOnCluster;
import org.shoulder.batch.cache.BatchProgressCache;
import org.shoulder.batch.cache.DefaultBatchProgressCache;
import org.shoulder.batch.enums.BatchConstants;
import org.shoulder.batch.model.BatchData;
import org.shoulder.batch.repository.BatchRecordDetailPersistentService;
import org.shoulder.batch.repository.BatchRecordPersistentService;
import org.shoulder.batch.repository.JdbcBatchRecordDetailPersistentService;
import org.shoulder.batch.repository.JdbcBatchRecordPersistentService;
import org.shoulder.batch.service.BatchAndExportService;
import org.shoulder.batch.service.impl.CsvExporter;
import org.shoulder.batch.service.impl.DefaultBatchExportService;
import org.shoulder.core.concurrent.Threads;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import javax.sql.DataSource;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 批处理相关自动装配
 *
 * @author lym
 */
@ConditionalOnClass(BatchData.class)
@Configuration(proxyBeanMethods = false)
public class BatchTaskAutoConfiguration {

    /**
     * service
     */
    @Bean
    @ConditionalOnMissingBean
    public BatchAndExportService batchAndExportService() {
        return new DefaultBatchExportService();
    }

    /**
     * 批处理线程池
     */
    @Bean(BatchConstants.THREAD_NAME)
    @ConditionalOnMissingBean(name = BatchConstants.THREAD_NAME)
    public ThreadPoolExecutor shoulderBatchThreadPool() {
        // 默认使用 5 个线程
        ThreadPoolExecutor executorService = new ThreadPoolExecutor(5, 5,
            60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(3000),
            new CustomizableThreadFactory("shoulder-batch"));
        Threads.setExecutorService(executorService);
        return executorService;
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
     * 进度缓存
     * 集群模式从缓存管理器中获取
     */
    @Bean
    @ConditionalOnCluster
    @ConditionalOnMissingBean
    public BatchProgressCache redisBatchProgressCache(CacheManager cacheManager) {
        return new DefaultBatchProgressCache(cacheManager.getCache(DefaultBatchProgressCache.CACHE_NAME));
    }


    @Configuration
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

    /**
     * csvImpl
     */
    @Bean
    @ConditionalOnMissingBean
    public CsvExporter csvExporter() {
        return new CsvExporter();
    }

}
