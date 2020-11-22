package org.shoulder.autoconfigure.batch;

import org.shoulder.batch.model.BatchData;
import org.shoulder.batch.repository.BatchRecordDetailPersistentService;
import org.shoulder.batch.repository.BatchRecordPersistentService;
import org.shoulder.batch.repository.JdbcBatchRecordDetailPersistentService;
import org.shoulder.batch.repository.JdbcBatchRecordPersistentService;
import org.shoulder.batch.service.BatchAndExportService;
import org.shoulder.batch.service.impl.CsvExporter;
import org.shoulder.batch.service.impl.DefaultBatchExportService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
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


    @Configuration
    @ConditionalOnClass(DataSource.class)
    @ConditionalOnProperty(name = "shoulder.batch.record.persistent.type", havingValue = "jdbc", matchIfMissing = true)
    static class JdbcLockAutoConfiguration {

        /**
         * jdbc
         */
        @Bean
        @ConditionalOnMissingBean
        public BatchRecordPersistentService batchRecordPersistentService(DataSource dataSource) {
            return new JdbcBatchRecordPersistentService(dataSource);
        }

        /**
         * jdbc
         */
        @Bean
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
