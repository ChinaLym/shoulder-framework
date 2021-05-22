package org.shoulder.ext.config.domain.enums;

import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.ext.config.domain.ex.ConfigException;

/**
 * @author lym
 */
public enum ConfigOperationTypeEnum {

    /**
     * UI 界面创建
     */
    CREATE("CREATE"),

    /**
     * 迁移
     */
    MIGRATION("MIGRATION"),

    /**
     * 更新
     */
    UPDATE("UPDATE"),

    /**
     * 删除
     */
    DELETE("DELETE"),
    ;

    private final String operationType;

    ConfigOperationTypeEnum(String operationType) {
        this.operationType = operationType;
    }

    /**
     * operationType
     *
     * @return operationType
     */
    public String getOperationType() {
        return operationType;
    }

    /**
     * Get the customer belongs to by the name
     *
     * @param name the name
     * @return the customer belongs to
     */
    public static ConfigOperationTypeEnum getByName(String name) {
        for (ConfigOperationTypeEnum it : values()) {
            if (it.operationType.equals(name)) {
                return it;
            }
        }
        throw new ConfigException(CommonErrorCodeEnum.UNKNOWN);
    }
}
