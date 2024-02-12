package org.shoulder.autoconfigure.batch;

import com.univocity.parsers.csv.CsvWriter;
import org.shoulder.autoconfigure.condition.ConditionalOnCluster;
import org.shoulder.autoconfigure.core.I18nAutoConfiguration;
import org.shoulder.batch.cache.BatchProgressCache;
import org.shoulder.batch.cache.DefaultBatchProgressCache;
import org.shoulder.batch.config.DefaultExportConfigManager;
import org.shoulder.batch.config.ExportConfigInitializer;
import org.shoulder.batch.config.ExportConfigManager;
import org.shoulder.batch.constant.BatchConstants;
import org.shoulder.batch.endpoint.ImportController;
import org.shoulder.batch.endpoint.ImportRestfulApi;
import org.shoulder.batch.model.BatchData;
import org.shoulder.batch.repository.BatchRecordDetailPersistentService;
import org.shoulder.batch.repository.BatchRecordPersistentService;
import org.shoulder.batch.repository.CacheBatchRecordDetailPersistentService;
import org.shoulder.batch.repository.CacheBatchRecordPersistentService;
import org.shoulder.batch.repository.JdbcBatchRecordDetailPersistentService;
import org.shoulder.batch.repository.JdbcBatchRecordPersistentService;
import org.shoulder.batch.service.BatchAndExportService;
import org.shoulder.batch.service.BatchService;
import org.shoulder.batch.service.ExportService;
import org.shoulder.batch.service.RecordService;
import org.shoulder.batch.service.impl.CsvExporter;
import org.shoulder.batch.service.impl.DataExporter;
import org.shoulder.batch.service.impl.DefaultBatchExportService;
import org.shoulder.core.i18.Translator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.List;
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
@AutoConfiguration(after = I18nAutoConfiguration.class)
@EnableConfigurationProperties(BatchProperties.class)
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
     * csvImpl
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(ExportConfigManager.class)
    public DefaultExportConfigManager exportConfigManager() {
        return new DefaultExportConfigManager();
    }

    /**
     * csvImpl
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(ExportConfigInitializer.class)
    public ExportConfigInitializer exportConfigInitializer(BatchProperties batchProperties, ExportConfigManager exportConfigManager) {
        return new ExportConfigInitializer(batchProperties, exportConfigManager);
    }

    /**
     * service
     */
    @Bean
    @ConditionalOnMissingBean(ImportRestfulApi.class)
    public ImportController ImportRestfulApi(
        BatchService batchService, ExportService exportService, RecordService recordService
    ) {
        return new ImportController(batchService, exportService, recordService);
    }

    @Bean
    @ConditionalOnBean(DataExporter.class)
    @ConditionalOnMissingBean
    public BatchAndExportService batchAndExportService(
        @Qualifier(BatchConstants.BATCH_THREAD_POOL_NAME)
        ThreadPoolExecutor batchThreadPool, Translator translator, List<DataExporter> dataExporterList,
        BatchRecordPersistentService batchRecordPersistentService, BatchRecordDetailPersistentService batchRecordDetailPersistentService,
        BatchProgressCache batchProgressCache, ExportConfigManager exportConfigManager
    ) {
        return new DefaultBatchExportService(batchThreadPool, translator, dataExporterList,
            batchRecordPersistentService, batchRecordDetailPersistentService, batchProgressCache,
            exportConfigManager);
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

    @AutoConfiguration(
        after = { JdbcBatchRecordAutoConfiguration.class, org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration.class })
    @ConditionalOnClass(CacheManager.class)
    @ConditionalOnBean({ CacheManager.class })
    @ConditionalOnProperty(name = "shoulder.batch.storage.type", havingValue = "memory", matchIfMissing = true)
    public static class SpringCacheBatchRecordAutoConfiguration {
        @Bean
        @ConditionalOnMissingBean
        public CacheBatchRecordPersistentService batchRecordPersistentService(CacheManager cacheManager) {
            return new CacheBatchRecordPersistentService(cacheManager.getCache(DefaultBatchProgressCache.CACHE_NAME));
        }

        @Bean
        @ConditionalOnMissingBean
        public CacheBatchRecordDetailPersistentService batchRecordDetailPersistentService(CacheManager cacheManager) {
            return new CacheBatchRecordDetailPersistentService(cacheManager.getCache(DefaultBatchProgressCache.CACHE_NAME));
        }
    }

    @AutoConfiguration(
        after = { JdbcBatchRecordAutoConfiguration.class, SpringCacheBatchRecordAutoConfiguration.class })
    @ConditionalOnMissingBean({ CacheManager.class })
    public static class DefaultMemoryBatchRecordAutoConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public CacheBatchRecordPersistentService memoryBatchRecordPersistentService() {
            return new CacheBatchRecordPersistentService(new ConcurrentMapCache(DefaultBatchProgressCache.CACHE_NAME));
        }

        @Bean
        @ConditionalOnMissingBean
        public CacheBatchRecordDetailPersistentService batchRecordDetailPersistentService() {
            return new CacheBatchRecordDetailPersistentService(new ConcurrentMapCache(DefaultBatchProgressCache.CACHE_NAME));
        }
    }

    @AutoConfiguration
    @ConditionalOnClass(DataSource.class)
    @ConditionalOnProperty(name = "shoulder.batch.storage.type", havingValue = "jdbc", matchIfMissing = false)
    public static class JdbcBatchRecordAutoConfiguration {

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
