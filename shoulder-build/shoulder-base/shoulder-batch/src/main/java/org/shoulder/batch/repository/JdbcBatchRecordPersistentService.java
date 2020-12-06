package org.shoulder.batch.repository;

import org.shoulder.batch.model.BatchRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.annotation.Nonnull;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * 批量处理记录mapper
 *
 * @author lym
 */
public class JdbcBatchRecordPersistentService implements BatchRecordPersistentService {


    private static String ALL_COLUMNS = "id, data_type, operation, total_num ,success_num ,fail_num, creator, " +
        "create_time";


    private static String INSERT = "INSERT INTO batch_record (" + ALL_COLUMNS + ") " +
        "VALUES (?,?,?,?,?,?,?,?)";


    private static String QUERY_BY_ID = "SELECT " + ALL_COLUMNS + " FROM batch_record where id=?";

    private static String QUERY_BY_USER =
        "SELECT " + ALL_COLUMNS + " FROM batch_record where data_type=? and creator=?";

    private static String QUERY_FIND_ALL = "SELECT " + ALL_COLUMNS +
        " FROM batch_record_detail WHERE recordId=?, AND status in (?)";


    private final JdbcTemplate jdbc;

    private RowMapper<BatchRecord> mapper = new BatchRecordRowMapper();

    public JdbcBatchRecordPersistentService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public JdbcBatchRecordPersistentService(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }


    /**
     * 单条插入
     *
     * @param record 批量处理记录
     */
    @Override
    public void insert(BatchRecord record) {
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
     * 根据 任务标识 获取批处理记录
     *
     * @param importId 主键
     * @return 记录
     */
    @Override
    public BatchRecord findById(String importId) {
        return jdbc.queryForObject(QUERY_BY_ID, mapper, importId);
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
    @Override
    public List<BatchRecord> findByPage(String dataType, Integer pageNum, Integer pageSize,
                                        String currentUserCode) {
        return jdbc.queryForList(QUERY_BY_USER + " limit ?,?", BatchRecord.class, dataType, currentUserCode, pageNum - 1,
            pageSize);
    }

    /**
     * 根据用户编码查询最近批处理的记录
     *
     * @return 上次批量处理记录
     */
    @Override
    public BatchRecord findLast(String dataType, String currentUserCode) {
        return jdbc.queryForObject(QUERY_BY_USER + " limit 1", mapper, dataType, currentUserCode);
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
