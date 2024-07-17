package org.shoulder.autoconfigure.db.sequence;

import org.shoulder.autoconfigure.db.DatabaseProperties;
import org.shoulder.data.sequence.DefaultSequenceGenerator;
import org.shoulder.data.sequence.SequenceGenerator;
import org.shoulder.data.sequence.dao.JdbcSequenceDAO;
import org.shoulder.data.sequence.dao.SequenceDao;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

/**
 * sequence 相关bean
 *
 * @author lym
 */
@AutoConfiguration(after = DataSourceAutoConfiguration.class)
@ConditionalOnClass(SequenceDao.class)
@EnableConfigurationProperties(DatabaseProperties.class)
public class ShoulderSequenceAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(SequenceDao.class)
    @ConditionalOnProperty(name = "shoulder.db.sequence.enable", havingValue = "true", matchIfMissing = true)
    public JdbcSequenceDAO jdbcSequenceDAO(DataSource dataSource,
                                           @Value("${shoulder.db.sequence.table_name:tb_sequence}") String tableName) {
        JdbcSequenceDAO sequenceDAO = new JdbcSequenceDAO();
        sequenceDAO.setDataSource(dataSource);
        sequenceDAO.setSequenceTableName(tableName);
        return sequenceDAO;
    }

    @Bean
    @ConditionalOnMissingBean(SequenceGenerator.class)
    public DefaultSequenceGenerator defaultSequenceGenerator(SequenceDao sequenceDao) {
        return new DefaultSequenceGenerator(sequenceDao);
    }
}
