package org.shoulder.log.operation.logger.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.shoulder.log.operation.entity.OpLogParam;
import org.shoulder.log.operation.entity.OperationLogEntity;
import org.springframework.util.Assert;

import java.util.Objects;
import java.util.StringJoiner;

/**
 * 操作日志格式校验类
 *
 * @author lym
 */
class OperationLogValidator {

    /**校验失败将抛出 runtimeException */
    static void validate(OperationLogEntity log) {
        Assert.notNull(log, "log is null.");
        // 必填项校验
        validateRequiredFields(log);
        // 字段最大长度限制
        validateLengthLimit(log);

        // 由于该字段校验较浪费资源，运行时不校验（因为即便校验也无默认补救措施）
        if(CollectionUtils.isNotEmpty(log.getParams())
                && "true".equals(System.getProperty("intellij.debug.agent"))){
            StringJoiner sj = new StringJoiner(",", "[", "]");
            log.getParams().stream().map(param -> param.format(log.getOperation())).filter(Objects::nonNull).forEach(sj::add);
            assertSmallerLimit(sj.length(), 2048, "opLogParam");
            log.getParams().forEach(OperationLogValidator::validate);
        }
    }


    /**
     * 必填项校验
     *  供使用者填写的优先校验
     * @param log 待校验日志实体
     */
    private static void validateRequiredFields(OperationLogEntity log){
        Assert.notNull(log.getOperationTime(), "operationTime is null.");
        Assert.notNull(log.getResult(), "result is null.");
        Assert.notNull(log.getTerminalType(), "terminalType is null.");
        Assert.hasText(log.getUserId(), "userId is blank.");
        Assert.hasText(log.getOperation(), "operation is blank.");
        Assert.hasText(log.getServiceId(), "serviceId is blank.");
    }

    /**
     * 字段长度校验
     * @param log 日志实体
     */
    private static void validateLengthLimit(OperationLogEntity log) {
        // operationTime 长度由 format 保证
        assertLengthLimit(log.getServiceId(), 128, "serviceId");
        assertLengthLimit(log.getUserOrgId(), 128, "userOrgId");
        assertLengthLimit(log.getUserId(), 128, "userId");
        assertLengthLimit(log.getUserName(), 128, "userName");
        assertLengthLimit(log.getIp(), 255, "ip");
        assertLengthLimit(log.getTerminalId(), 128, "terminalId");
        assertLengthLimit(log.getObjectType(), 128, "objectType");
        assertLengthLimit(log.getObjectId(), 128, "objectId");
        assertLengthLimit(log.getObjectName(), 255, "objectName");
        assertLengthLimit(log.getOperation(), 255, "operation");
        assertLengthLimit(log.getDetailKey(), 128, "i18nKey");
        if(log.getDetailItems() != null && log.getDetailItems().isEmpty()){
            assertSmallerLimit(log.getDetailItems().stream().map(String::length).reduce(Integer::sum).orElse(0), 4096, "detail");
        }
    }

    private static void validate(OpLogParam param) {
        if(param != null){
            // 必填项校验（目前规范中未明确是否必填）
            validateRequiredFields(param);
        }
    }

    /**
     * 校验必填项
     */
    private static void validateRequiredFields(OpLogParam param){
        Assert.hasText(param.getName(), "OpLogParam.name is empty.");
    }

    // ---------------------- base validate ----------------------

    /** 长度不超过 */
    private static void assertLengthLimit(String str, int limit, String name) {
        if (str != null) {
            assertSmallerLimit(str.length(), limit, name);
        }
    }

    /** 长度不超过 */
    private static void assertSmallerLimit(int num, int limit, String name) {
        if (num  > limit) {
            throw new IllegalArgumentException(name + " over maximum size. yours length: " + num +
                    ". It should shorter than " + limit);
        }
    }

    /** 只允许枚举范围内 */
    @Deprecated
    private static void assertEnum(String enumStr, int limit, String name) {
        final int char0Ascii = 48;
        limit += char0Ascii;
        if (enumStr != null && (enumStr.length() != 1 || enumStr.toCharArray()[0] > limit)) {
            throw new IllegalArgumentException("'" + name + "' illegal value. yours is [" + enumStr + "]"+
                    ". It should in ['0','1'" + (limit == 1 ? "]." : ",'2','3']."));
        }
    }

}
