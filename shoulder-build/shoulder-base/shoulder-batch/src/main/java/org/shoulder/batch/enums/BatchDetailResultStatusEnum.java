package org.shoulder.batch.enums;

import lombok.Getter;
import org.shoulder.core.dictionary.model.IntDictionaryItemEnum;
import org.shoulder.core.exception.BaseRuntimeException;

import java.util.Arrays;

/**
 * 处理结果详情-状态
 *
 * @author lym
 */
@Getter
public enum BatchDetailResultStatusEnum implements IntDictionaryItemEnum<BatchDetailResultStatusEnum> {


    /**
     * 等待开始
     */
//    WAITING(0, BatchI18nEnum.RESULT_UNKNOWN.getCode(), BatchDetailResultStatusEnum.TYPE_INIT),

    /**
     * 处理中
     */
//    PROCESSING(1, BatchI18nEnum.RESULT_UNKNOWN.getCode(), BatchDetailResultStatusEnum.TYPE_INIT),

    /**
     * 成功 / 通过
     */
    SUCCESS(10, BatchI18nEnum.RESULT_IMPORT_SUCCESS.getCode(), BatchDetailResultStatusEnum.TYPE_SUCCESS),

    /**
     * 成功 - 重复（数据已存在）并更新旧值
     */
    UPDATE_REPEAT(11, BatchI18nEnum.RESULT_IMPORT_UPDATE_REPEAT.getCode(), BatchDetailResultStatusEnum.TYPE_SUCCESS),

    /**
     * 跳过 - 数据重复
     */
    SKIP_FOR_REPEAT(20, BatchI18nEnum.RESULT_IMPORT_SKIP_INVALID.getCode(), BatchDetailResultStatusEnum.TYPE_SKIP),

    /**
     * 跳过 - 校验不通过（未处理）
     */
    SKIP_FOR_INVALID(21, BatchI18nEnum.RESULT_VALIDATE_FAILED.getCode(), BatchDetailResultStatusEnum.TYPE_SKIP),

    /**
     * 失败
     */
    FAILED(30, BatchI18nEnum.RESULT_IMPORT_FAILED.getCode(), BatchDetailResultStatusEnum.TYPE_FAIL),

    /**
     * 失败 - 校验不通过（未处理）
     */
    FAILED_FOR_INVALID(31, BatchI18nEnum.RESULT_VALIDATE_FAILED.getCode(), BatchDetailResultStatusEnum.TYPE_FAIL),

    /**
     * 失败 - 数据重复
     */
    FAILED_FOR_REPEAT(32, BatchI18nEnum.RESULT_IMPORT_SKIP_INVALID.getCode(), BatchDetailResultStatusEnum.TYPE_FAIL),
    ;

    /**
     * 状态分类
     */
    public static final String TYPE_INIT = "INIT";
    public static final String TYPE_SUCCESS = "SUCCESS";
    public static final String TYPE_FAIL = "FAIL";
    public static final String TYPE_SKIP = "SKIP";


    final Integer code;

    final String tip;

    final String type;

    BatchDetailResultStatusEnum(int code, String tip, String type) {
        this.code = code;
        this.tip = tip;
        this.type = type;
    }

    public static BatchDetailResultStatusEnum of(Integer code) {
        return Arrays.stream(BatchDetailResultStatusEnum.values())
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
