package org.shoulder.batch.enums;

import lombok.Getter;
import org.shoulder.core.dictionary.model.NameAsIdDictionaryItemEnum;

/**
 * 操作日志相关
 *
 * @author lym
 */
public enum BatchOperationEnum implements NameAsIdDictionaryItemEnum<BatchOperationEnum> {

    IMPORT_VALIDATE("i18n.operation.shoulder.batch.import_validate"),
    IMPORT("i18n.operation.shoulder.batch.import"),
    EXPORT("i18n.operation.shoulder.batch.export"),

    ;
    @Getter private final String i18n;

    BatchOperationEnum(String i18n) {this.i18n = i18n;}

    @Override public String getDisplayName() {
        return i18n;
    }
}
