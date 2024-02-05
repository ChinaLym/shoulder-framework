package org.shoulder.log.operation.logger.impl;

import jakarta.annotation.Nonnull;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.shoulder.core.util.JsonUtils;
import org.shoulder.log.operation.logger.AbstractOperationLogger;
import org.shoulder.log.operation.logger.OperationLogger;
import org.shoulder.log.operation.model.OperationLogDTO;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 以 jdbc 记录操作日志记录，直接保存至数据库。适合微小型项目中，日志中心与业务组件同数据库。
 * <p>
 * 注意：使用前需要保证对应的数据库表存在。
 * 事务：由于认为该日志一般在异步线程中执行，shoulder 未显示控制事务
 *
 * @author lym
 */
public class JdbcOperationLogger extends AbstractOperationLogger implements OperationLogger {

    private final JdbcTemplate jdbcTemplate;

    public JdbcOperationLogger(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private static final String ALL_INSERT_COLUMNS = "app_id, instance_id, " +
            "user_id, user_name, user_real_name, user_org_id, user_org_name, terminal_type, terminal_address, terminal_id, terminal_info, " +
            "operation, object_type, object_id, object_name, detail, detail_i18n_key, detail_i18n_item, operation_param," +
            "result, error_code, operation_time, end_time, duration, trace_id, relation_id, tenant_code, extended_field0";

    private static final String VALUES = "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String BATCH_INSERT = "INSERT INTO log_operation (" + ALL_INSERT_COLUMNS + ") " + VALUES;

    {
        // 由于字段过多，这里断言能对应上，没有缺少字段
        assert ALL_INSERT_COLUMNS.split(",").length == VALUES.split(",").length;
    }


    private static final int FIELD_NUM = ALL_INSERT_COLUMNS.split(",").length;

    @Override
    public void log(@Nonnull Collection<? extends OperationLogDTO> opLogList) {
        // 如果过多，需要考虑多线程/分片，默认使用批量插入
        jdbcTemplate.batchUpdate(BATCH_INSERT, flatFieldsToArray(opLogList));
        log.debug("persistent {} opLogs with jdbc.", opLogList.size());
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
        fields[1] = opLog.getInstanceId();
        fields[2] = opLog.getUserId();
        fields[3] = opLog.getUserName();
        fields[4] = opLog.getUserRealName();
        fields[5] = opLog.getUserOrgId();
        fields[6] = opLog.getUserOrgName();
        fields[7] = opLog.getTerminalType().getCode();
        fields[8] = opLog.getRemoteAddress();
        fields[9] = opLog.getTerminalId();
        fields[10] = opLog.getTerminalInfo();

        fields[11] = opLog.getOperation();
        fields[12] = opLog.getObjectType();
        fields[13] = opLog.getObjectId();
        fields[14] = opLog.getObjectName();
        fields[15] = opLog.getDetail();
        fields[16] = opLog.getDetailI18nKey();
        fields[17] = CollectionUtils.isEmpty(opLog.getDetailI18nItems()) ? null : JsonUtils.toJson(opLog.getDetailI18nItems());
        fields[18] = CollectionUtils.isEmpty(opLog.getParams()) ? null : JsonUtils.toJson(opLog.getParams());

        fields[19] = opLog.getResult().getCode();
        fields[20] = opLog.getErrorCode();
        fields[21] = Timestamp.from(opLog.getOperationTime());
        fields[22] = Timestamp.from(opLog.getEndTime());
        // 持续时间 ms
        fields[23] = Duration.between(opLog.getOperationTime(), opLog.getEndTime()).toMillis();
        fields[24] = opLog.getTraceId();
        fields[25] = null;// relation_id
        fields[26] = opLog.getTenantCode();

        fields[27] = MapUtils.isEmpty(opLog.getExtFields()) ? null : JsonUtils.toJson(opLog.getExtFields());

        return fields;
    }

}
