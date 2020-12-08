package org.shoulder.batch.repository;

import org.apache.commons.collections4.CollectionUtils;
import org.shoulder.batch.enums.BatchResultEnum;
import org.shoulder.batch.model.BatchRecordDetail;
import org.shoulder.batch.repository.po.BatchRecordDetailPO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.annotation.Nonnull;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 批处理记录持久化接口
 *
 * @author lym
 */
public class JdbcBatchRecordDetailPersistentService implements BatchRecordDetailPersistentService {


    private static String ALL_COLUMNS = "id, record_id, row_num, operation, status, fail_reason, source";

    private static String BATCH_INSERT = "INSERT INTO batch_record_detail (" + ALL_COLUMNS + ") " +
        "VALUES (?,?,?,?,?,?,?)";

    private static String QUERY_ALL_WITH_CONDITION = "SELECT " + ALL_COLUMNS +
        " FROM batch_record_detail WHERE record_id=?, AND status in (?)";

    private static String QUERY_ALL = "SELECT " + ALL_COLUMNS +
        " FROM batch_record_detail WHERE record_id=?";


    private final JdbcTemplate jdbc;

    private RowMapper<BatchRecordDetail> mapper = new BatchRecordDetailRowMapper();


    public JdbcBatchRecordDetailPersistentService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public JdbcBatchRecordDetailPersistentService(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    /**
     * 批量新增处理详情
     *
     * @param batchRecordDetailList 要插入的记录
     */
    @Override
    public void batchSave(String recordId, List<BatchRecordDetail> batchRecordDetailList) {
        jdbc.batchUpdate(BATCH_INSERT, flatFieldsToArray(batchRecordDetailList));
    }

    private List<Object[]> flatFieldsToArray(List<BatchRecordDetail> batchRecordDetailList) {
        return batchRecordDetailList.stream().map(this::flatFieldsToArray).collect(Collectors.toList());
    }

    private Object[] flatFieldsToArray(BatchRecordDetail batchRecordDetail) {
        Object[] fields = new Object[7];
        fields[0] = batchRecordDetail.getId();
        fields[1] = batchRecordDetail.getRecordId();
        fields[2] = batchRecordDetail.getRowNum();
        fields[3] = batchRecordDetail.getOperation();
        fields[4] = batchRecordDetail.getStatus();
        fields[5] = batchRecordDetail.getFailReason();
        fields[6] = batchRecordDetail.getSource();
        return fields;
    }

    /**
     * 查询所有的批量处理记录
     *
     * @param recordId   记录标识
     * @param resultList 结果状态
     * @return 所有的批量处理记录
     */
    @Override
    public List<BatchRecordDetail> findAllByResult(String recordId, List<BatchResultEnum> resultList) {
        return jdbc.queryForList(QUERY_ALL_WITH_CONDITION, BatchRecordDetailPO.class, recordId,
            CollectionUtils.emptyIfNull(resultList).stream()
                .map(BatchResultEnum::getCode)
                .collect(Collectors.toList())
        ).stream()
            .map(BatchRecordDetailPO::toModel)
            .collect(Collectors.toList());
    }

    @Override
    public List<BatchRecordDetail> findAllByResult(String recordId) {
        return jdbc.queryForList(QUERY_ALL, BatchRecordDetailPO.class, recordId).stream()
            .map(BatchRecordDetailPO::toModel)
            .collect(Collectors.toList());
    }


    /**
     * Row mapper for BatchRecordDetail.
     *
     * @author lym
     */
    private static class BatchRecordDetailRowMapper implements RowMapper<BatchRecordDetail> {

        @Override
        public BatchRecordDetail mapRow(@Nonnull ResultSet resultSet, int i) throws SQLException {
            return BatchRecordDetail.builder()
                .id(resultSet.getInt(1))
                .recordId(resultSet.getString(2))
                .rowNum(resultSet.getInt(3))
                .operation(resultSet.getString(4))
                .status(resultSet.getInt(5))
                .failReason(resultSet.getString(6))
                .source(resultSet.getString(7))
                .build();
        }
    }

}
