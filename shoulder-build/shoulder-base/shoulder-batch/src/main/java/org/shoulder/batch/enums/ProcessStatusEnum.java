package org.shoulder.batch.enums;

import lombok.Getter;
import org.shoulder.core.exception.BaseRuntimeException;
import org.shoulder.core.dictionary.model.IntDictionaryItemEnum;

import java.util.Arrays;

/**
 * 处理结果
 *
 * @author lym
 */
@Getter public enum ProcessStatusEnum implements IntDictionaryItemEnum<ProcessStatusEnum> {


    /**
     * 等待开始
     */
    WAITING(0, BatchI18nEnum.RESULT_UNKNOWN.getCode()),

    /**
     * 处理中
     */
    PROCESSING(1, BatchI18nEnum.RESULT_UNKNOWN.getCode()),
    /**
     * 成功 / 通过
     */
    SUCCESS(10, BatchI18nEnum.RESULT_UNKNOWN.getCode()),
    /**
     * 成功 - 重复（数据已存在）并更新旧值
     */
    UPDATE_REPEAT(11, BatchI18nEnum.RESULT_IMPORT_UPDATE.getCode()),

    /**
     * 跳过 - 数据重复
     */
    SKIP_FOR_REPEAT(20, BatchI18nEnum.RESULT_IMPORT_SKIP.getCode()),

    /**
     * 跳过 - 校验不通过（未处理）
     */
    SKIP_FOR_INVALID(21, BatchI18nEnum.RESULT_VAIDATE_FAILED.getCode()),

    /**
     * 失败
     */
    FAILED(30, BatchI18nEnum.RESULT_IMPORT_FAILED.getCode()),

    /**
     * 失败 - 校验不通过（未处理）
     */
    FAILED_FOR_INVALID(31, BatchI18nEnum.RESULT_VAIDATE_FAILED.getCode()),

    /**
     * 失败 - 数据重复
     */
    FAILED_FOR_REPEAT(32, BatchI18nEnum.RESULT_IMPORT_SKIP.getCode()),
    ;



    final Integer code;

    final String tip;

    ProcessStatusEnum(int code, String tip) {
        this.code = code;
        this.tip = tip;
    }

    public static ProcessStatusEnum of(Integer code) {
        return Arrays.stream(ProcessStatusEnum.values())
                .filter(e -> e.code.equals(code))
                // 非法状态码，除恶意调用，否则不会发生
                .findFirst().orElseThrow(() -> new BaseRuntimeException("invalid resultCode"));
    }

    @Override public Integer getItemId() {
        return code;
    }
    @Override public String getDisplayName() {
        return tip;
    }

    @Override public String getDescription() {
        return tip;
    }

}
