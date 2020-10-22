package org.shoulder.log.operation.logger.impl;

import org.shoulder.log.operation.dto.OperationLogDTO;
import org.shoulder.log.operation.logger.OperationLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 将操作日志保存到数据库中，以默认的表形式保存
 *
 * @author lym
 */
public class JdbcOperationLogger extends AbstractOperationLogger implements OperationLogger {

    private static final Logger log = LoggerFactory.getLogger(JdbcOperationLogger.class);

    /*private final JdbcTemplate jdbcTemplate;

    public JdbcOperationLogger(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
*/

    @Override
    protected void doLog(OperationLogDTO opLog) {
        // todo save to db
    }

}
