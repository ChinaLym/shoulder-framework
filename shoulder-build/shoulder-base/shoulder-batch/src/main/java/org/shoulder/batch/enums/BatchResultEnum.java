package org.shoulder.batch.enums;

import java.util.Arrays;

/**
 * 处理结果
 * todo 码值
 *
 * @author lym
 */
public enum BatchResultEnum {

    /**
     * 待校验
     */
    WAIT_VALIDATE("wait_validate", 0, BatchI18nEnum.RESULT_UNKNOWN.getCode()),
    /**
     * 校验通过
     */
    VALIDATE_SUCCESS("validate—success", 0, BatchI18nEnum.RESULT_UNKNOWN.getCode()),
    /**
     * 校验-数据重复
     */
    VALIDATE_REPEAT("validate—fail", 0, BatchI18nEnum.RESULT_UNKNOWN.getCode()),


    // ==================== 结束状态 =================

    /**
     * ALL
     */
    ALL("*", 0, BatchI18nEnum.RESULT_VAIDATE_FAILED.getCode()),

    /**
     * 校验不通过（无法处理）
     */
    VALIDATE_FAILED("*", 0, BatchI18nEnum.RESULT_VAIDATE_FAILED.getCode()),
    /**
     * 重复（已存在）并跳过
     */
    SKIP_REPEAT("process_skip", 1, BatchI18nEnum.RESULT_IMPORT_SKIP.getCode()),
    /**
     * 处理成功
     */
    IMPORT_SUCCESS("process_success", 3, BatchI18nEnum.RESULT_IMPORT_SUCCESS.getCode()),
    /**
     * 处理失败
     */
    IMPORT_FAILED("process_failed", 4, BatchI18nEnum.RESULT_IMPORT_FAILED.getCode()),
    /**
     * 重复（已存在）并更新旧值
     */
    UPDATE_REPEAT("process_update", 5, BatchI18nEnum.RESULT_IMPORT_UPDATE.getCode()),
    ;


    String type;

    Integer code;

    String tip;

    BatchResultEnum(String type, int code, String tip) {
        this.type = type;
        this.code = code;
        this.tip = tip;
    }

    public String getType() {
        return type;
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
            .findFirst().orElse(ALL);
    }

}
