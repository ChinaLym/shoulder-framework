package org.shoulder.data.dal.sequence.old;

import org.shoulder.core.log.LoggerFactory;
import org.shoulder.data.dal.sequence.SequenceDao;
import org.shoulder.data.dal.sequence.SequenceRange;
import org.shoulder.data.dal.sequence.exceptions.SequenceException;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.sql.*;

/**
 * 序列DAO默认实现，JDBC方式
 *
 * @author lym
 *
 */
@Deprecated
public class DefaultSequenceDao implements SequenceDao {
    private static final Logger log                              = LoggerFactory.getLogger("SEQUENCE");

    private static final int    MIN_STEP                         = 1;
    private static final int    MAX_STEP                         = 100000;
    private static final int    DEFAULT_STEP                     = 1000;
    private static final int    DEFAULT_RETRY_TIMES              = 150;

    private static final String DEFAULT_TABLE_NAME               = "sequence";
    private static final String DEFAULT_NAME_COLUMN_NAME         = "name";
    private static final String DEFAULT_VALUE_COLUMN_NAME        = "value";
    private static final String DEFAULT_GMT_MODIFIED_COLUMN_NAME = "gmt_modified";

    /** sequence的最大值=Long.MAX_VALUE-DELTA，超过这个值就说明sequence溢出了. */
    private static final long   DELTA                            = 100000000L;

    private DataSource          dataSource;

    /**
     * 重试次数
     */
    private int                 retryTimes                       = DEFAULT_RETRY_TIMES;

    /**
     * 步长
     */
    private int                 step                             = DEFAULT_STEP;

    /**
     * 序列所在的表名
     */
    private String              tableName                        = DEFAULT_TABLE_NAME;

    /**
     * 存储序列名称的列名
     */
    private String              nameColumnName                   = DEFAULT_NAME_COLUMN_NAME;

    /**
     * 存储序列值的列名
     */
    private String              valueColumnName                  = DEFAULT_VALUE_COLUMN_NAME;

    /**
     * 存储序列最后更新时间的列名
     */
    private String              gmtModifiedColumnName            = DEFAULT_GMT_MODIFIED_COLUMN_NAME;

    private volatile String     selectSql;
    private volatile String     updateSql;

    public SequenceRange nextRange(String name) throws SequenceException {
        if (name == null) {
            throw new IllegalArgumentException("序列名称不能为空");
        }

        long oldValue;
        long newValue;

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        for (int i = 0; i < retryTimes + 1; ++i) {
            try {
                conn = dataSource.getConnection();
                stmt = conn.prepareStatement(getSelectSql());
                stmt.setString(1, name);
                rs = stmt.executeQuery();
                rs.next();
                oldValue = rs.getLong(1);

                if (oldValue < 0) {
                    StringBuilder message = new StringBuilder();
                    message.append("Sequence value cannot be less than zero, value = ").append(
                        oldValue);
                    message.append(", please check table ").append(getTableName());
                    throw new SequenceException(message.toString());
                }

                if (oldValue > Long.MAX_VALUE - DELTA) {//判断原来的区间中，起始值是否已经溢出.
                    StringBuilder message = new StringBuilder();
                    message.append("Sequence value overflow, value = ").append(oldValue);
                    message.append(", please check table ").append(getTableName());
                    throw new SequenceException(message.toString());
                }

                newValue = oldValue + getStep();
            } catch (SQLException e) {
                throw new SequenceException(e);
            } finally {
                try {
                    if (rs != null) {
                        rs.close();
                        rs = null;
                    }
                    if (stmt != null) {
                        stmt.close();
                        stmt = null;
                    }
                    if (conn != null) {
                        conn.close();
                        conn = null;
                    }
                } catch (Exception e) {
                    log.error("ERROR ## close resources has an error", e);
                }
            }

            try {
                conn = dataSource.getConnection();
                stmt = conn.prepareStatement(getUpdateSql());
                stmt.setLong(1, newValue);
                stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
                stmt.setString(3, name);
                stmt.setLong(4, oldValue);
                int affectedRows = stmt.executeUpdate();
                if (affectedRows <= 0) {//乐观锁更新，直到更新成功.
                    // retry
                    continue;
                }
                return new SequenceRange(oldValue + 1, newValue);
            } catch (SQLException e) {
                throw new SequenceException(e);
            } finally {
                try {
                    if (stmt != null) {
                        stmt.close();
                        stmt = null;
                    }
                    if (conn != null) {
                        conn.close();
                        conn = null;
                    }
                } catch (Exception e) {
                    log.error("ERROR ## close resources has an error", e);
                }
            }
        }

        throw new SequenceException("Retried too many times, retryTimes = " + retryTimes);
    }

    private String getSelectSql() {
        if (selectSql == null) {
            synchronized (this) {
                if (selectSql == null) {
                    StringBuilder buffer = new StringBuilder();
                    buffer.append("select ").append(getValueColumnName());
                    buffer.append(" from ").append(getTableName());
                    buffer.append(" where ").append(getNameColumnName()).append(" = ?");

                    selectSql = buffer.toString();
                }
            }
        }

        return selectSql;
    }

    private String getUpdateSql() {
        if (updateSql == null) {
            synchronized (this) {
                if (updateSql == null) {
                    StringBuilder buffer = new StringBuilder();
                    buffer.append("update ").append(getTableName());
                    buffer.append(" set ").append(getValueColumnName()).append(" = ?, ");
                    buffer.append(getGmtModifiedColumnName()).append(" = ? where ");
                    buffer.append(getNameColumnName()).append(" = ? and ");
                    buffer.append(getValueColumnName()).append(" = ?");

                    updateSql = buffer.toString();
                }
            }
        }

        return updateSql;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(int retryTimes) {
        if (retryTimes < 0) {
            throw new IllegalArgumentException(
                "Property retryTimes cannot be less than zero, retryTimes = " + retryTimes);
        }

        this.retryTimes = retryTimes;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        if (step < MIN_STEP || step > MAX_STEP) {
            StringBuilder message = new StringBuilder();
            message.append("Property step out of range [").append(MIN_STEP);
            message.append(",").append(MAX_STEP).append("], step = ").append(step);

            throw new IllegalArgumentException(message.toString());
        }

        this.step = step;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getNameColumnName() {
        return nameColumnName;
    }

    public void setNameColumnName(String nameColumnName) {
        this.nameColumnName = nameColumnName;
    }

    public String getValueColumnName() {
        return valueColumnName;
    }

    public void setValueColumnName(String valueColumnName) {
        this.valueColumnName = valueColumnName;
    }

    public String getGmtModifiedColumnName() {
        return gmtModifiedColumnName;
    }

    public void setGmtModifiedColumnName(String gmtModifiedColumnName) {
        this.gmtModifiedColumnName = gmtModifiedColumnName;
    }
}
