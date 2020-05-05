package org.shoulder.log.operation.constants;

/**
 * 方法入参类型
 *
 * @author lym
 */
public enum ActionParamTypeEnum {

    /**
     * 不支持多语言
     * 默认值
     */
    STRING("string"),

    /**
     * 参数支持多语言
     */
    LOCALE("locale"),

    ;

    private final String type;

    ActionParamTypeEnum(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static ActionParamTypeEnum getDefault(){
        return STRING;
    }


}