package org.shoulder.autoconfigure.batch;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvWriter;
import org.shoulder.autoconfigure.condition.ConditionalOnCluster;
import org.shoulder.autoconfigure.core.I18nAutoConfiguration;
import org.shoulder.autoconfigure.lock.LockAutoConfiguration;
import org.shoulder.batch.config.DefaultExportConfigManager;
import org.shoulder.batch.config.ExportConfigInitializer;
import org.shoulder.batch.config.ExportConfigManager;
import org.shoulder.batch.constant.BatchConstants;
import org.shoulder.batch.endpoint.ImportController;
import org.shoulder.batch.endpoint.ImportRestfulApi;
import org.shoulder.batch.model.BatchData;
import org.shoulder.batch.model.convert.BatchProgressRecordDomain2DTOConverter;
import org.shoulder.batch.model.convert.BatchRecordDetailDomain2DTOConverter;
import org.shoulder.batch.model.convert.BatchRecordDomain2DTOConverter;
import org.shoulder.batch.progress.BatchProgressCache;
import org.shoulder.batch.progress.DefaultBatchProgressCache;
import org.shoulder.batch.repository.BatchRecordDetailPersistentService;
import org.shoulder.batch.repository.BatchRecordPersistentService;
import org.shoulder.batch.repository.CacheBatchRecordDetailPersistentServiceImpl;
import org.shoulder.batch.repository.CacheBatchRecordPersistentServiceImpl;
import org.shoulder.batch.repository.JdbcBatchRecordDetailPersistentServiceImpl;
import org.shoulder.batch.repository.JdbcBatchRecordPersistentServiceImpl;
import org.shoulder.batch.service.BatchAndExportService;
import org.shoulder.batch.service.BatchService;
import org.shoulder.batch.service.ExportService;
import org.shoulder.batch.service.RecordService;
import org.shoulder.batch.service.impl.DefaultBatchExportService;
import org.shoulder.batch.spi.DataExporter;
import org.shoulder.batch.spi.DefaultTaskSplitHandler;
import org.shoulder.batch.spi.ExportDataQueryFactory;
import org.shoulder.batch.spi.ImportTaskSplitHandler;
import org.shoulder.batch.spi.csv.CsvExporter;
import org.shoulder.batch.spi.csv.DataItemConvertFactory;
import org.shoulder.batch.spi.csv.DefaultDataItemConvertFactory;
import org.shoulder.core.converter.ShoulderConversionService;
import org.shoulder.core.i18.Translator;
import org.shoulder.core.lock.ServerLock;
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
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.Nullable;
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
@AutoConfiguration(after = {I18nAutoConfiguration.class, LockAutoConfiguration.class})
@EnableConfigurationProperties(BatchProperties.class)
public class ShoulderBatchAutoConfiguration {

    public ShoulderBatchAutoConfiguration() {
        // just for debug
    }

    @Bean
    @ConditionalOnMissingBean
    public BatchProgressRecordDomain2DTOConverter batchProgressRecordDomain2DTOConverter() {
        return BatchProgressRecordDomain2DTOConverter.INSTANCE;
    }

    @Bean
    @ConditionalOnMissingBean
    public BatchRecordDetailDomain2DTOConverter batchRecordDetailDomain2DTOConverter(@Nullable Translator translator) {
        BatchRecordDetailDomain2DTOConverter.INSTANCE = new BatchRecordDetailDomain2DTOConverter(translator);
        return BatchRecordDetailDomain2DTOConverter.INSTANCE;
    }

    @Bean
    @ConditionalOnMissingBean
    public BatchRecordDomain2DTOConverter batchRecordDomain2DTOConverter() {
        return BatchRecordDomain2DTOConverter.INSTANCE;
    }

    @Order(Ordered.LOWEST_PRECEDENCE)
    @Bean
    public DefaultTaskSplitHandler defaultTaskSplitHandler() {
        return new DefaultTaskSplitHandler(200);
    }

    @Order(Ordered.LOWEST_PRECEDENCE - 1)
    @Bean
    public ImportTaskSplitHandler importTaskSplitHandler() {
        return new ImportTaskSplitHandler();
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
    @ConditionalOnClass(CsvParser.class)
    @ConditionalOnMissingBean(ImportRestfulApi.class)
    public ImportController importRestfulApi(
            ServerLock serverLock, BatchService batchService, ExportService exportService, RecordService recordService,
            @Nullable DataItemConvertFactory dataItemConvertFactory, ShoulderConversionService conversionService,
            @Nullable List<ExportDataQueryFactory> exportDataQueryFactoryList
    ) {
        return new ImportController(serverLock, batchService, exportService, recordService,
                dataItemConvertFactory, conversionService, exportDataQueryFactoryList);
    }

    /**
     * service
     */
    @Bean
    @ConditionalOnMissingBean(DataItemConvertFactory.class)
    public DefaultDataItemConvertFactory defaultDataItemConvertFactory() {
        return new DefaultDataItemConvertFactory();
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
            after = {JdbcBatchRecordAutoConfiguration.class, org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration.class})
    @ConditionalOnClass(CacheManager.class)
    @ConditionalOnBean({CacheManager.class})
    @ConditionalOnProperty(name = "shoulder.batch.storage.type", havingValue = "memory", matchIfMissing = true)
    public static class SpringCacheBatchRecordAutoConfiguration {
        @Bean
        @ConditionalOnMissingBean(BatchRecordPersistentService.class)
        public CacheBatchRecordPersistentServiceImpl batchRecordPersistentService(CacheManager cacheManager) {
            return new CacheBatchRecordPersistentServiceImpl(cacheManager.getCache(DefaultBatchProgressCache.CACHE_NAME));
        }

        @Bean
        @ConditionalOnMissingBean(BatchRecordDetailPersistentService.class)
        public CacheBatchRecordDetailPersistentServiceImpl batchRecordDetailPersistentService(CacheManager cacheManager) {
            return new CacheBatchRecordDetailPersistentServiceImpl(cacheManager.getCache(DefaultBatchProgressCache.CACHE_NAME));
        }
    }

    @AutoConfiguration(
            after = {JdbcBatchRecordAutoConfiguration.class, SpringCacheBatchRecordAutoConfiguration.class})
    @ConditionalOnMissingBean({CacheManager.class})
    public static class DefaultMemoryBatchRecordAutoConfiguration {

        @Bean
        @ConditionalOnMissingBean(BatchRecordPersistentService.class)
        public CacheBatchRecordPersistentServiceImpl memoryBatchRecordPersistentService() {
            return new CacheBatchRecordPersistentServiceImpl(new ConcurrentMapCache(DefaultBatchProgressCache.CACHE_NAME));
        }

        @Bean
        @ConditionalOnMissingBean(BatchRecordDetailPersistentService.class)
        public CacheBatchRecordDetailPersistentServiceImpl batchRecordDetailPersistentService() {
            return new CacheBatchRecordDetailPersistentServiceImpl(new ConcurrentMapCache(DefaultBatchProgressCache.CACHE_NAME));
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
        @ConditionalOnMissingBean(BatchRecordPersistentService.class)
        public BatchRecordPersistentService batchRecordPersistentService(DataSource dataSource) {
            return new JdbcBatchRecordPersistentServiceImpl(dataSource);
        }

        /**
         * jdbc
         */
        @Bean
        @ConditionalOnBean(DataSource.class)
        @ConditionalOnMissingBean(BatchRecordDetailPersistentService.class)
        public BatchRecordDetailPersistentService batchRecordDetailPersistentService(DataSource dataSource) {
            return new JdbcBatchRecordDetailPersistentServiceImpl(dataSource);
        }
    }

}
