package org.shoulder.batch.enums;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 导入结果
 *
 * @author lym
 */
public enum ImportResultEnum {

    /**
     * 所有信息
     */
    ALL("*", 0, BatchI18nEnum.RESULT_UNKNOWN.getCode()),
    /**
     * 重复（已存在）并跳过
     */
    SKIP_REPEAT("import_skip", 1, BatchI18nEnum.RESULT_IMPORT_SKIP.getCode()),
    /**
     * 校验不通过
     */
    VALIDATE_FAILED("validate_failed", 2, BatchI18nEnum.RESULT_VAIDATE_FAILED.getCode()),
    /**
     * 导入成功
     */
    IMPORT_SUCCESS("import_success", 3, BatchI18nEnum.RESULT_IMPORT_SUCCESS.getCode()),
    /**
     * 导入失败
     */
    IMPORT_FAILED("import_failed", 4, BatchI18nEnum.RESULT_IMPORT_FAILED.getCode()),
    /**
     * 重复（已存在）并更新旧值
     */
    UPDATE_REPEAT("import_update", 5, BatchI18nEnum.RESULT_IMPORT_UPDATE.getCode()),
    ;


    String type;

    Integer code;

    String tip;

    ImportResultEnum(String type, int code, String tip) {
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

    public boolean isFail() {
        return this == ImportResultEnum.IMPORT_FAILED
                || this == ImportResultEnum.VALIDATE_FAILED;
    }

    public static ImportResultEnum of(String type) {
        return Arrays.stream(ImportResultEnum.values())
                .filter(e -> e.type.equals(type))
                .findFirst().orElse(ALL);
    }

    public static List<ImportResultEnum> listOf(String type) {
        if (StringUtils.isEmpty(type)) {
            return Collections.emptyList();
        }
        String[] types = type.split(",");
        List<ImportResultEnum> list = new ArrayList<>(type.length());
        for (String t : types) {
            list.add(of(t));
        }
        return list;
    }

    public static ImportResultEnum of(Integer code) {
        return Arrays.stream(ImportResultEnum.values())
                .filter(e -> e.code.equals(code))
                .findFirst().orElse(ALL);
    }

}
