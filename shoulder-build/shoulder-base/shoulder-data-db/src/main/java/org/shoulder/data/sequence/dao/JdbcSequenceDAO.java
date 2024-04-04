package org.shoulder.data.sequence.dao;

import org.shoulder.data.sequence.model.SequenceRange;
import org.shoulder.data.sequence.exceptions.CombinationSequenceException;
import org.springframework.jdbc.datasource.DataSourceUtils;

import java.sql.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 默认的 DAO（JDBC）
 *
 * @author lym
 */
public class JdbcSequenceDAO extends AbstractCacheAndRetryableSequenceDao {
    protected Map<String, SequenceSqlDialect> sequenceSqlDialectMap = new HashMap<>();

    private SequenceSqlDialect sequenceSqlDialect = DefaultSequenceSqlDialectEnum.MYSQL;

    /**
     * {@link this#initialize} will invoke this method once
     * <p>
     * To get rid of Spring Framework
     */
    @Override
    public void initialize() throws Exception {
        super.initialize();
        sequenceSqlDialect = DefaultSequenceSqlDialectEnum.MYSQL;
        sequenceSqlDialectMap.put("MYSQL", DefaultSequenceSqlDialectEnum.MYSQL);
    }

    @Override
    protected void insertSequenceRange(SequenceRange sequenceRange) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DataSourceUtils.getConnection(dataSource);
            ps = conn.prepareStatement(replaceTableName(sequenceSqlDialectMap.get(findDBType(sequenceRange)).insert()));
            ps.setString(1, sequenceRange.getName());
            ps.setLong(2, sequenceRange.getMin());
            ps.setLong(3, sequenceRange.getMax());
            ps.setLong(4, sequenceRange.getStep());
            ps.setLong(5, sequenceRange.getValue());
            setPartitionIdIfNecessary(ps, 6, sequenceRange);
            ps.executeUpdate();
        } catch (SQLException e) {
            String msg = "Fail to insertSequenceRange:" + sequenceRange;
            logger.error(msg, e);
            throw new CombinationSequenceException(msg, e);
        } finally {
            closeQuietly(null, ps, conn);
        }
    }

    @Override
    protected int updateSequenceRange(SequenceRange sequenceRange) {
        int affectedRows = 0;
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DataSourceUtils.getConnection(dataSource);
            ps = conn.prepareStatement(replaceTableName(sequenceSqlDialectMap.get(findDBType(sequenceRange)).update()));
            ps.setLong(1, sequenceRange.getLatestValue());
            ps.setString(2, sequenceRange.getName());
            ps.setLong(3, sequenceRange.getValue());
            setPartitionIdIfNecessary(ps, 4, sequenceRange);
            affectedRows = ps.executeUpdate();
        } catch (SQLException e) {
            String msg = "Fail to updateSequenceRange:" + sequenceRange;
            logger.error(msg, e);
            throw new CombinationSequenceException(msg, e);
        } finally {
            closeQuietly(null, ps, conn);
        }
        return affectedRows;
    }

    @Override
    protected SequenceRange selectSequenceRange(SequenceRange localSequenceRange) {
        SequenceRange sequenceRange = null;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DataSourceUtils.getConnection(dataSource);
            ps = conn.prepareStatement(replaceTableName(sequenceSqlDialectMap.get(findDBType(sequenceRange)).select()));
            ps.setString(1, localSequenceRange.getName());
            setPartitionIdIfNecessary(ps, 2, localSequenceRange);
            rs = ps.executeQuery();
            if (rs.next()) {
                sequenceRange = new SequenceRange();
                sequenceRange.setName(rs.getString(SequenceRange.NAME));
                sequenceRange.setValue(rs.getLong(SequenceRange.VALUE));
                sequenceRange.setMax(rs.getLong(SequenceRange.MAX_VALUE));
                sequenceRange.setMin(rs.getLong(SequenceRange.MIN_VALUE));
                sequenceRange.setStep(rs.getLong(SequenceRange.STEP));
                sequenceRange.setSystemDate(rs.getTimestamp(SequenceRange.GMT_MODIFIED));
                sequenceRange.setFetchDate(new Date());
                // 设置分区信息
            }
        } catch (SQLException e) {
            String msg = "Fail to selectSequenceRange:" + sequenceRange;
            logger.error(msg, e);
            throw new CombinationSequenceException(msg, e);
        } finally {
            closeQuietly(rs, ps, conn);
        }
        return sequenceRange;
    }

    private void setPartitionIdIfNecessary(PreparedStatement ps, int parameterIndex,
                                           SequenceRange sequenceRange) throws SQLException {
        String dbType = findDBType(sequenceRange);
        // set 分区
        // ps.setString(parameterIndex, sequenceRange.getPartitionId());
    }


    private void closeQuietly(ResultSet rs, Statement st, Connection conn) {
        if (DataSourceUtils.isConnectionTransactional(conn, dataSource)) {
            close(rs, st, null);
        } else {
            close(rs, st, conn);
        }
    }


    /**
     * 确定DB类型
     *
     * @param routeInfo sql 路由信息
     * @return dbType
     */
    protected String findDBType(SequenceRange routeInfo) {
        return sequenceSqlDialect.getDbType();
    }

    /**
     * jdbc.close connection
     */
    public static void close(ResultSet resultSet, Statement statement, Connection conn) {
        try {
            if (null != resultSet && !resultSet.isClosed()) {
                resultSet.close();
            }
        } catch (Exception e) {
            logger.error("Fail to close ResultSet", e);
        }
        try {
            if (null != statement && !statement.isClosed()) {
                statement.close();
            }
        } catch (Exception e) {
            logger.error("Fail to close Statement", e);
        }
        try {
            if (null != conn && !conn.isClosed()) {
                conn.close();
            }
        } catch (Exception e) {
            logger.error("Fail to close Connection", e);
        }
    }

}
