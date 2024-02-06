package org.shoulder.log.operation.format.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.shoulder.core.context.AppInfo;
import org.shoulder.core.exception.BaseRuntimeException;
import org.shoulder.log.operation.format.OperationLogFormatter;
import org.shoulder.log.operation.model.OpLogParam;
import org.shoulder.log.operation.model.OperationLogDTO;

import java.lang.reflect.Field;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * 默认日志格式化（逗号分隔的键值对）
 * key1:"v1",k2:"v2"
 *
 * @author lym
 * @implNote 该类是 shoulder 规范中推荐的格式，可能并不是所有系统都希望的
 */
public class ShoulderOpLogFormatter implements OperationLogFormatter {

    /**
     * 日期格式化:高性能线程安全
     */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(AppInfo.dateTimeFormat(), AppInfo.defaultLocale());

    /**
     * {@link OperationLogDTO} 类中所有 String 类型的字段
     */
    private static final List<Field> opLogStrFields;

    static {
        // 反射获取所有文本字段并设置可访问
        opLogStrFields = Arrays.stream(OperationLogDTO.class.getDeclaredFields())
            // CharSequence
            .filter(field -> CharSequence.class.isAssignableFrom(field.getType()))
            .peek(field -> field.setAccessible(true))
            .collect(Collectors.toList());
        // 包含 String、枚举、List、Map、OpLogParam
    }

    /**
     * @param operation 操作标识
     * @param param     参数
     */
    public static String formatParam(String operation, OpLogParam param) {
        if (CollectionUtils.isEmpty(param.getValue())) {
            throw new IllegalStateException("operationParam.values is empty!");
        }

        String name = operation + "." + param.getName();
        // 一般 value 只有一个，出现多个直接使用 json / 逗号分隔
        StringJoiner valueJoiner = new StringJoiner(",");
        param.getValue().stream()
            .filter(StringUtils::isEmpty)
            .map(v -> name + v)
            .forEach(valueJoiner::add);

        return "{\"name\"=\"" + name + '\"' + ", \"value\"=\"" + valueJoiner + "\"}";
    }

    @Override
    public String format(OperationLogDTO opLog) {

        KeyValueContextBuilder builder = new KeyValueContextBuilder();
        // 反射拼接所有 String 类型
        opLogStrFields.forEach(field -> {
            try {
                Object value = field.get(opLog);
                if (value == null) {
                    return;
                }
                builder.add(field.getName(), (String) value);
            } catch (IllegalAccessException e) {
                throw new BaseRuntimeException("format opLog fail", e);
            }
        });

        // 拼接特殊类型
        builder
                .add("terminalType", String.valueOf(opLog.getTerminalType().getCode()))
                .add("result", String.valueOf(opLog.getResult().getCode()))
                .add("operationTime", DATE_TIME_FORMATTER.format(ZonedDateTime.ofInstant(opLog.getOperationTime(), AppInfo.timeZone().toZoneId())));
        if (opLog.getEndTime() != null) {
            builder.add("endTime", DATE_TIME_FORMATTER.format(ZonedDateTime.ofInstant(opLog.getEndTime(), AppInfo.timeZone().toZoneId())));
        }

        // 拼接 List 类型（json格式）
        if (CollectionUtils.isNotEmpty(opLog.getDetailI18nItems())) {
            StringJoiner detailsStr = new StringJoiner(",", "[", "]");
            opLog.getDetailI18nItems()
                .stream().
                filter(Objects::nonNull)
                .forEach(detailsStr::add);
            builder.add("detailItems", detailsStr.toString());
        }
        if (CollectionUtils.isNotEmpty(opLog.getParams())) {
            StringJoiner sj = new StringJoiner(",", "[", "]");
            opLog.getParams().stream()
                .map(param -> formatParam(opLog.getOperation(), param))
                .forEach(sj::add);
            builder.add("params", sj.toString());
        }

        // 拼接扩展字段
        if (MapUtils.isNotEmpty(opLog.getExtFields())) {
            opLog.getExtFields().forEach(builder::add);
        }

        return builder.formatResult();

    }

}
