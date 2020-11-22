package org.shoulder.batch.enums;

import org.shoulder.core.exception.BaseRuntimeException;

import java.util.Arrays;

/**
 * 处理结果
 *
 * @author lym
 */
public enum BatchResultEnum {


    // ==================== 准备批处理 =================

    /**
     * 待校验
     */
    WAIT_VALIDATE(1, BatchI18nEnum.RESULT_UNKNOWN.getCode()),
    /**
     * 校验通过
     */
    VALIDATE_SUCCESS(2, BatchI18nEnum.RESULT_UNKNOWN.getCode()),
    /**
     * 校验-数据重复
     */
    VALIDATE_REPEAT(3, BatchI18nEnum.RESULT_UNKNOWN.getCode()),


    // ==================== 批处理结束状态 =================

    /**
     * 处理成功
     */
    IMPORT_SUCCESS(10, BatchI18nEnum.RESULT_IMPORT_SUCCESS.getCode()),
    /**
     * 处理失败
     */
    IMPORT_FAILED(11, BatchI18nEnum.RESULT_IMPORT_FAILED.getCode()),
    /**
     * 因校验不通过（未处理）
     */
    VALIDATE_FAILED(12, BatchI18nEnum.RESULT_VAIDATE_FAILED.getCode()),
    /**
     * 重复（已存在）并跳过
     */
    SKIP_REPEAT(13, BatchI18nEnum.RESULT_IMPORT_SKIP.getCode()),
    /**
     * 重复（已存在）并更新旧值
     */
    UPDATE_REPEAT(14, BatchI18nEnum.RESULT_IMPORT_UPDATE.getCode()),
    ;



    Integer code;

    String tip;

    BatchResultEnum(int code, String tip) {
        this.code = code;
        this.tip = tip;
    }

    public int getCode() {
        return code;
    }

    public String getTip() {
        return tip;
    }

    public static BatchResultEnum of(Integer code) {
        return Arrays.stream(BatchResultEnum.values())
            .filter(e -> e.code.equals(code))
            .findFirst().orElseThrow(() -> {
                // 非法状态码，除恶意调用，否则不会发生
                throw new BaseRuntimeException("invalid resultCode");
            });
    }

}
