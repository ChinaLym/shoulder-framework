package org.shoulder.log.operation.format.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.shoulder.log.operation.entity.OpLogParam;
import org.shoulder.log.operation.entity.OperationLogEntity;
import org.shoulder.log.operation.format.OperationLogValidator;
import org.springframework.util.Assert;

import java.util.StringJoiner;

/**
 * 操作日志格式默认校验类
 *
 * @author lym
 * @implNote  该类是 shoulder 规范中推荐的格式，可能并不是所有系统都希望的
 */
public class ShoulderOperationLogValidator implements OperationLogValidator {

    /**校验失败将抛出 runtimeException */
    @Override
    public void validate(OperationLogEntity log) {
        Assert.notNull(log, "log is null.");
        // 必填项校验
        validateRequiredFields(log);
        // 字段最大长度限制
        validateLengthLimit(log);

        // 由于该字段校验较浪费资源，运行时不校验（因为即便校验也无默认补救措施）
        if(CollectionUtils.isNotEmpty(log.getParams())
                && "true".equals(System.getProperty("intellij.debug.agent"))){
            // 必填项校验
            log.getParams().forEach(this::validate);
            // 校验参数拼接后的长度小于 maxParamLength
            StringJoiner sj = new StringJoiner(",", "[", "]");
            log.getParams().stream().map(param -> ShoulderOpLogFormatter.formatParam(log.getOperation(), param)).forEach(sj::add);
            int maxParamLength = 2048;
            assertSmallerLimit(sj.length(), maxParamLength, "opLogParam");
        }
    }


    /**
     * 必填项校验
     *  供使用者填写的优先校验
     * @param log 待校验日志实体
     */
    protected void validateRequiredFields(OperationLogEntity log){
        Assert.hasText(log.getUserId(), "userId is blank.");
        Assert.notNull(log.getTerminalType(), "terminalType is null.");
        Assert.hasText(log.getOperation(), "operation is blank.");
        Assert.notNull(log.getOperationTime(), "operationTime is null.");
        Assert.notNull(log.getResult(), "result is null.");
        Assert.hasText(log.getAppId(), "appId is blank.");
    }

    /**
     * 字段长度校验
     * @param log 日志实体
     */
    protected void validateLengthLimit(OperationLogEntity log) {
        // operationTime 长度由 format 保证
        assertLengthLimit(log.getAppId(), 128, "appId");
        assertLengthLimit(log.getUserOrgId(), 128, "userOrgId");
        assertLengthLimit(log.getUserId(), 128, "userId");
        assertLengthLimit(log.getUserName(), 128, "userName");
        assertLengthLimit(log.getIp(), 255, "ip");
        assertLengthLimit(log.getTerminalId(), 128, "terminalId");
        assertLengthLimit(log.getObjectType(), 128, "objectType");
        assertLengthLimit(log.getObjectId(), 128, "objectId");
        assertLengthLimit(log.getObjectName(), 255, "objectName");
        assertLengthLimit(log.getOperation(), 255, "operation");
        assertLengthLimit(log.getDetailKey(), 128, "detailKey");
        if(log.getDetailItems() != null && log.getDetailItems().isEmpty()){
            assertSmallerLimit(log.getDetailItems().stream().map(String::length).reduce(Integer::sum).orElse(0), 4096, "detail");
        }
    }

    /**
     * 校验参数
     */
    protected void validate(OpLogParam param) {
        if(param != null){
            // 校验必填项
            Assert.hasText(param.getName(), "opLogParam.name is blank.");
        }
    }

    // ---------------------- base validate ----------------------

    /** 长度不超过 */
    protected void assertLengthLimit(String str, int limit, String name) {
        if (str != null) {
            assertSmallerLimit(str.length(), limit, name);
        }
    }

    /** 长度不超过 */
    protected void assertSmallerLimit(int num, int limit, String name) {
        if (num  > limit) {
            throw new IllegalArgumentException(name + " over maximum size. yours length: " + num +
                    ". It should shorter than " + limit);
        }
    }


}
