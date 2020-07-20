package org.shoulder.log.operation.format.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.shoulder.core.util.StringUtils;
import org.shoulder.log.operation.constants.OpLogI18nPrefix;
import org.shoulder.log.operation.entity.OperationLogEntity;
import org.shoulder.log.operation.format.OperationLogFormatter;

import java.util.Objects;
import java.util.StringJoiner;

/**
 * 默认日志格式化（逗号分隔的键值对）
 * "key1":"v1","k2":"v2"
 *
 * @author lym
 * @implNote  该类是 shoulder 规范中推荐的格式，可能并不是所有系统都希望的
 */
public class ShoulderOpLogFormatter implements OperationLogFormatter {

    /**
     * 日期格式化:高性能线程安全
     */
    private static FastDateFormat fastDateFormat = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    @Override
    public String format(OperationLogEntity opLog){
        StringBuilder logString = new StringBuilder();

        logString.append("userId:\"").append(opLog.getUserId()).append("\",");
        if (StringUtils.isNotEmpty(opLog.getUserName())) {
            logString.append("userName:\"").append(opLog.getUserName()).append("\",");
        }
        if (StringUtils.isNotEmpty(opLog.getPersonId())) {
            logString.append("personId:\"").append(opLog.getPersonId()).append("\",");
        }
        if (StringUtils.isNotEmpty(opLog.getUserOrgId())) {
            logString.append("userOrgId:\"").append(opLog.getUserOrgId()).append("\",");
        }
        if (StringUtils.isNotEmpty(opLog.getUserOrgName())) {
            logString.append("userOrgName:\"").append(opLog.getUserOrgName()).append("\",");
        }
        logString.append("terminalType:\"").append(opLog.getTerminalType().getType()).append("\",");
        logString.append("terminalId:\"").append(opLog.getTerminalId()).append("\",");
        logString.append("terminalInfo:\"").append(opLog.getTerminalInfo()).append("\",");
        if (StringUtils.isNotEmpty(opLog.getIp())) {
            logString.append("ip:\"").append(opLog.getIp()).append("\",");
        }

        String operation = opLog.getOperation();
        boolean hasOperationPrefix = operation.startsWith(OpLogI18nPrefix.OPERATION);
        logString.append("operation:\"");
        if (hasOperationPrefix) {
            logString.append(opLog.getOperation());
        } else {
            logString.append(OpLogI18nPrefix.OPERATION)
                .append(opLog.getOperation());
        }
        logString.append("\",");

        if (StringUtils.isNotEmpty(opLog.getDetailKey())) {
            logString.append("detailKey:\"");
            boolean hasMsgIdIdPrefix = opLog.getDetailKey().startsWith(OpLogI18nPrefix.DETAIL);
            if (hasMsgIdIdPrefix) {
                logString.append(opLog.getDetailKey());
            } else {
                logString.append(OpLogI18nPrefix.DETAIL)
                    .append(opLog.getDetailKey());
            }
            logString.append("\",");
        }
        if (CollectionUtils.isNotEmpty(opLog.getDetailItems())) {
            StringJoiner detailsStr = new StringJoiner(",");
            opLog.getDetailItems().stream().filter(Objects::nonNull).forEach(detailsStr::add);
            logString.append("detail:\"")
                .append(detailsStr.toString())
                .append("\",");
        }
        if (StringUtils.isNotEmpty(opLog.getDetail())) {
            logString.append("detail:\"").append(opLog.getDetail()).append("\",");
        }

        // objectType
        if (StringUtils.isNotEmpty(opLog.getObjectType())) {
            boolean hasObjectTypePrefix = opLog.getObjectType().startsWith(OpLogI18nPrefix.OBJECT_TYPE);
            logString.append("objectType:\"");
            if (hasObjectTypePrefix) {
                logString.append(opLog.getObjectType());
            } else {
                logString.append(OpLogI18nPrefix.OBJECT_TYPE)
                    .append(opLog.getObjectType());
            }
            logString.append("\",");
        }
        if (StringUtils.isNotEmpty(opLog.getObjectId())) {
            logString.append("objectId:\"").append(opLog.getObjectId()).append("\",");
        }
        if (StringUtils.isNotEmpty(opLog.getObjectName())) {
            logString.append("objectName:\"").append(opLog.getObjectName()).append("\",");
        }



        logString.append("operationTime:\"").append(fastDateFormat.format(opLog.getOperationTime())).append("\",");
        logString.append("result:\"").append(opLog.getResult().getCode()).append("\",");
        if (StringUtils.isNotEmpty(opLog.getErrorCode())) {
            logString.append("errorCode:\"").append(opLog.getErrorCode()).append("\",");
        }
        if (CollectionUtils.isNotEmpty(opLog.getParams())) {
            StringJoiner sj = new StringJoiner(",", "[", "]");
            opLog.getParams().stream().map(param -> param.format(operation)).filter(Objects::nonNull).forEach(sj::add);
            logString
                .append("params:\"")
                .append(sj)
                .append("\",");
        }

        logString.append("serviceId:\"").append(opLog.getServiceId()).append("\",");
        if (StringUtils.isNotEmpty(opLog.getTraceId())) {
            logString.append("traceId:\"").append(opLog.getTraceId()).append("\",");
        }
        if (StringUtils.isNotEmpty(opLog.getBusinessId())) {
            logString.append("relationId:\"").append(opLog.getBusinessId()).append("\",");
        }
        if(MapUtils.isNotEmpty(opLog.getExtFields())){
            opLog.getExtFields().forEach((k,v) -> {
                logString.append(k).append(":\"").append(v).append("\",");
            });
        }

        logString.setLength(logString.length() - 1);
        return logString.toString();

    }
}
