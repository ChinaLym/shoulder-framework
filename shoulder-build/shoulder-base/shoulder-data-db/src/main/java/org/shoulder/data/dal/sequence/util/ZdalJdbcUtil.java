package org.shoulder.data.dal.sequence.util;

import org.shoulder.core.log.LoggerFactory;

import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * @author lym
 *
 */
public class ZdalJdbcUtil {

    private static final Logger logger = LoggerFactory.getLogger("MONITOR");

    public static void close(Object... objects) {
        if (null != objects) {
            for (Object o : objects) {

                if (o instanceof ResultSet) {
                    try {
                        ((ResultSet) o).close();
                    } catch (Exception e) {
                        logger.error("ZdalJdbcUtil Closing ResultSet failed, due to ", e);
                    }
                } else if (o instanceof Statement) {
                    try {
                        ((Statement) o).close();
                    } catch (Exception e) {
                        logger.error("ZdalJdbcUtil Closing Statement failed, due to ", e);
                    }
                } else if (o instanceof Connection) {
                    try {
                        ((Connection) o).close();
                    } catch (Exception e) {
                        logger.error("ZdalJdbcUtil Closing Connection failed, due to ", e);
                    }
                }

            }
        }
    }

    public static void close(ResultSet resultSet, Statement statement, Connection conn) {
        try {
            if (null != resultSet && !resultSet.isClosed()) {
                resultSet.close();
            }
        } catch (Exception e) {
            logger.error("ZdalJdbcUtil Closing ResultSet failed, due to ", e);
        }
        try {
            if (null != statement && !statement.isClosed()) {
                statement.close();
            }
        } catch (Exception e) {
            logger.error("ZdalJdbcUtil Closing Statement failed, due to ", e);
        }
        try {
            if (null != conn && !conn.isClosed()) {
                conn.close();
            }
        } catch (Exception e) {
            logger.error("ZdalJdbcUtil Closing Connection failed, due to ", e);
        }
    }
}
