package org.shoulder.autoconfigure.db.sequence;

import org.shoulder.autoconfigure.db.DatabaseProperties;
import org.shoulder.data.sequence.DefaultSequenceGenerator;
import org.shoulder.data.sequence.SequenceGenerator;
import org.shoulder.data.sequence.dao.JdbcSequenceDAO;
import org.shoulder.data.sequence.dao.SequenceDao;
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
@ConditionalOnProperty(name = "shoulder.db.sequence.enable", havingValue = "true", matchIfMissing = true)
public class ShoulderSequenceAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(SequenceDao.class)
    public JdbcSequenceDAO jdbcSequenceDAO(DataSource dataSource, DatabaseProperties databaseProperties) {
        JdbcSequenceDAO sequenceDAO = new JdbcSequenceDAO();
        sequenceDAO.setDataSource(dataSource);
        sequenceDAO.setSequenceTableName(databaseProperties.getSequence().getTableName());
        return sequenceDAO;
    }

    @Bean
    @ConditionalOnMissingBean(SequenceGenerator.class)
    public SequenceGenerator defaultSequenceGenerator(SequenceDao sequenceDao) {
        return new DefaultSequenceGenerator(sequenceDao);
    }
}
