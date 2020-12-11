package org.shoulder.log.operation.logger.impl;

import org.shoulder.core.util.JsonUtils;
import org.shoulder.log.operation.dto.OperationLogDTO;
import org.shoulder.log.operation.logger.AbstractOperationLogger;
import org.shoulder.log.operation.logger.OperationLogger;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.Nonnull;
import javax.sql.DataSource;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 以 jdbc 记录操作日志记录，直接保存至数据库。适合微小型项目中，日志中心与业务组件同数据库。
 * <p>
 * 注意：使用前需要保证对应的数据库表存在。
 *
 * @author lym
 */
public class JdbcOperationLogger extends AbstractOperationLogger implements OperationLogger {

    private final JdbcTemplate jdbcTemplate;

    public JdbcOperationLogger(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private static final String ALL_INSERT_COLUMNS = "component_id, instance_id, " +
        "user_id, user_name, user_real_name, user_org_id, user_org_name, ip, terminal_id, terminal_type, terminal_info, " +
        "object_type, object_id, object_name, action_param, operation, detail, detail_key, detail_item, " +
        "result, error_code, operation_time, end_time, last_time, trace_id, relation_id, tenant_code, extended_field0";

    private static final String VALUES = "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static String BATCH_INSERT = "INSERT INTO log_operation (" + ALL_INSERT_COLUMNS + ") " + VALUES;

    {
        // 能对应上，不少
        assert ALL_INSERT_COLUMNS.split(",").length == VALUES.split(",").length;
    }


    private static final int FIELD_NUM = ALL_INSERT_COLUMNS.split(",").length;

    @Override
    public void log(@Nonnull Collection<? extends OperationLogDTO> opLogList) {
        // 如果过多，需要考虑多线程/分片，默认使用批量插入
        jdbcTemplate.batchUpdate(BATCH_INSERT, flatFieldsToArray(opLogList));
    }

    @Override
    protected void doLog(OperationLogDTO opLog) {
        log(Collections.singletonList(opLog));
    }


    private List<Object[]> flatFieldsToArray(Collection<? extends OperationLogDTO> opLogList) {
        return opLogList.stream().map(this::flatFieldsToArray).collect(Collectors.toList());
    }

    private <T extends OperationLogDTO> Object[] flatFieldsToArray(T opLog) {
        Object[] fields = new Object[FIELD_NUM];
        fields[0] = opLog.getAppId();
        fields[1] = null; // todo instanceId
        fields[2] = opLog.getUserId();
        fields[3] = opLog.getUserName();
        fields[4] = opLog.getUserRealName();
        fields[5] = opLog.getUserOrgId();
        fields[6] = opLog.getUserOrgName();
        fields[7] = opLog.getRemoteAddress();
        fields[8] = opLog.getTerminalId();
        fields[9] = opLog.getTerminalType().getCode();
        fields[10] = opLog.getTerminalInfo();

        fields[11] = opLog.getObjectType();
        fields[12] = opLog.getObjectId();
        fields[13] = opLog.getObjectName();
        fields[14] = JsonUtils.toJson(opLog.getDetailItems());//todo JsonUtils.toJson(opLog.getParams())
        fields[15] = opLog.getOperation();
        fields[16] = opLog.getDetail();
        fields[17] = opLog.getDetailKey();
        fields[18] = JsonUtils.toJson(opLog.getDetailItems());

        fields[19] = opLog.getResult().getCode();
        fields[20] = opLog.getErrorCode();
        fields[21] = Timestamp.from(opLog.getOperationTime());
        fields[22] = Timestamp.from(opLog.getEndTime());
        fields[23] = null;//Duration.between(opLog.getOperationTime(), opLog.getEndTime());
        fields[24] = opLog.getTraceId();
        fields[25] = null;// relation_id
        fields[26] = null;// tenant_code

        fields[27] = JsonUtils.toJson(opLog.getExtFields());

        return fields;
    }

}
