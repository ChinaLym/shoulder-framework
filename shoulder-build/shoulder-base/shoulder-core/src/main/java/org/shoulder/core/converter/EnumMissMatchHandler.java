package org.shoulder.core.converter;

import org.springframework.lang.NonNull;

/**
 * String 转枚举失败时处理器
 * @author lym
 */
public interface EnumMissMatchHandler {

    /**
     * 处理输入为空
     *
     * @param enumType 枚举类型
     * @param <T> 任意枚举类型
     * @return 枚举
     */
    <T> T handleNullSource(@NonNull Class<? extends Enum> enumType);

    /**
     * 处理转换失败
     *
     * @param enumType 枚举类型
     * @param source 字符串
     * @param <T> 任意枚举类型
     * @return 枚举
     */
    <T> T handleMissMatch(@NonNull Class<? extends Enum> enumType, @NonNull String source);
}
