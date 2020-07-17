package org.shoulder.core.converter;

/**
 * String 转枚举失败时处理器
 * @author lym
 */
public class DefaultMissMatchHandler implements MissMatchHandler {

    private final String defaultFieldName = "default";

    private final String defaultMethodName = "default";

    @Override
    public Enum<?> handleMissMatch() {
        return null;
    }

}
