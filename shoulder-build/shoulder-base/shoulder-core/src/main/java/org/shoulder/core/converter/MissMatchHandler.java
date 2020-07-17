package org.shoulder.core.converter;

/**
 * String 转枚举失败时处理器
 * @author lym
 */
public interface MissMatchHandler {
    Enum<?> handleMissMatch();
}
