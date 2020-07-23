package org.shoulder.log.operation.dto.sample;

import org.shoulder.log.operation.dto.Operable;
import org.shoulder.log.operation.dto.OperateRecord;

/**
 * 临时保存一次操作结果 DTO，常用于批量场景
 *
 * @author lym
 */
public class OperateRecordDto extends OperableObject implements OperateRecord {

    protected boolean success;

    public OperateRecordDto() {
    }

    public OperateRecordDto(Operable operable) {
        super(operable);
    }

    @Override
    public boolean success() {
        return success;
    }

    public boolean isSuccess() {
        return success;
    }

    public OperateRecordDto setSuccess(boolean success) {
        this.success = success;
        return this;
    }
}
