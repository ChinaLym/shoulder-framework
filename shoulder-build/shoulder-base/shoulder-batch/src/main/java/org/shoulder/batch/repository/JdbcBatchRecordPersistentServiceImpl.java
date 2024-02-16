package org.shoulder.batch.repository;

import jakarta.annotation.Nonnull;
import org.shoulder.batch.model.BatchRecord;
import org.shoulder.core.util.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

/**
 * 批量处理记录mapper
 *
 * @author lym
 */
public class JdbcBatchRecordPersistentServiceImpl implements BatchRecordPersistentService {

    private static String ALL_COLUMNS = "id, data_type, operation, total_num ,success_num ,fail_num, creator, " +
                                        "create_time";

    private static String INSERT = "INSERT INTO batch_record (" + ALL_COLUMNS + ") " +
                                   "VALUES (?,?,?,?,?,?,?,?)";

    private static String QUERY_BY_ID = "SELECT " + ALL_COLUMNS + " FROM batch_record where id=?";

    private static String QUERY_BY_DATA_TYPE =
        "SELECT " + ALL_COLUMNS + " FROM batch_record WHERE data_type=?";

    private static String QUERY_FIND_ALL = "SELECT " + ALL_COLUMNS +
                                           " FROM batch_record_detail WHERE recordId=?, AND status in (?)";

    private final JdbcTemplate jdbc;

    private final RowMapper<BatchRecord> recordRowMapper = new BatchRecordRowMapper();

    public JdbcBatchRecordPersistentServiceImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public JdbcBatchRecordPersistentServiceImpl(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    /**
     * 单条插入
     *
     * @param record 批量处理记录
     */
    @Override
    public void insert(@Nonnull BatchRecord record) {
        jdbc.update(INSERT, flatFieldsToArray(record));
    }

    private Object[] flatFieldsToArray(BatchRecord batchRecord) {
        Object[] fields = new Object[8];
        fields[0] = batchRecord.getId();
        fields[1] = batchRecord.getDataType();
        fields[2] = batchRecord.getOperation();
        fields[3] = batchRecord.getTotalNum();
        fields[4] = batchRecord.getSuccessNum();
        fields[5] = batchRecord.getFailNum();
        fields[6] = batchRecord.getCreator();
        fields[7] = batchRecord.getCreateTime();
        return fields;
    }

    /**
     * 根据 批处理任务id 获取批处理记录
     *
     * @param recordId 主键
     * @return 记录
     */
    @Override
    public BatchRecord findById(@Nonnull String recordId) {
        return jdbc.query(QUERY_BY_ID, recordRowMapper, recordId)
            .stream().findFirst().orElse(null);
    }

    /**
     * 根据条件分页查询批处理记录
     *
     * @param dataType        查询条件
     * @param pageNum         查询条件
     * @param pageSize        查询条件
     * @param currentUserCode 查询条件
     * @return 查询结果
     */
    @Nonnull @Override
    public List<BatchRecord> findByPage(@Nonnull String dataType, Integer pageNum, Integer pageSize,
                                        String currentUserCode) {
        List<Object> argList = new ArrayList<>(4);
        argList.add(dataType);
        String sql = QUERY_BY_DATA_TYPE;
        if (StringUtils.isNotBlank(currentUserCode)) {
            sql += " AND creator=?";
            argList.add(currentUserCode);
        }
        argList.add(pageNum - 1);
        argList.add(pageSize);

        return jdbc.query(sql + " limit ?,?", recordRowMapper, argList.toArray());
    }

    /**
     * 根据用户编码查询最近批处理的记录
     *
     * @return 上次批量处理记录
     */
    @Override
    public BatchRecord findLast(@Nonnull String dataType, String currentUserCode) {
        List<Object> argList = new ArrayList<>(2);
        argList.add(dataType);
        String sql = QUERY_BY_DATA_TYPE;
        if (StringUtils.isNotBlank(currentUserCode)) {
            sql += " AND creator=?";
            argList.add(currentUserCode);
        }
        return jdbc.query(sql + " limit 1", recordRowMapper, dataType, currentUserCode)
            .stream().findFirst().orElse(null);
    }

    /**
     * Row mapper for BatchRecord.
     *
     * @author lym
     */
    private static class BatchRecordRowMapper implements RowMapper<BatchRecord> {
        @Override
        public BatchRecord mapRow(@Nonnull ResultSet resultSet, int i) throws SQLException {
            return BatchRecord.builder()
                .id(resultSet.getString(1))
                .dataType(resultSet.getString(2))
                .operation(resultSet.getString(3))
                .totalNum(resultSet.getInt(4))
                .successNum(resultSet.getInt(5))
                .failNum(resultSet.getInt(6))
                .creator(resultSet.getLong(7))
                .createTime(resultSet.getDate(8))
                .build();
        }
    }

}
