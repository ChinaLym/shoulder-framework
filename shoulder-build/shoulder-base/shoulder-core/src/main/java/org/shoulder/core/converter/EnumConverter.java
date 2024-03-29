package org.shoulder.core.converter;

import jakarta.annotation.Nonnull;
import org.springframework.core.convert.converter.Converter;

/**
 * 接口入参，枚举接收字符串，通用转换类
 *
 * @author lym
 */
public class EnumConverter implements Converter<String, Enum<? extends Enum<?>>> {

    private final Class<? extends Enum<?>> enumType;

    private final EnumMissMatchHandler missMatchHandler;

    public EnumConverter(@Nonnull Class<? extends Enum<?>> enumType, EnumMissMatchHandler missMatchHandler) {
        this.enumType = enumType;
        this.missMatchHandler = missMatchHandler;
    }

    @Override
    public Enum<?> convert(@Nonnull String source) {
        // 处理空字符串
        if (source.isBlank()) {
            return missMatchHandler.handleNullSource(enumType);
        }
        // 尝试用名称匹配。大小写敏感
        Enum<?>[] enums = enumType.getEnumConstants();
        for (Enum<?> e : enums) {
            if (e.name().equals(source)) {
                return e;
            }
        }
        // 名称匹配失败时触发 MissMatch
        return missMatchHandler.handleMissMatch(enumType, source);
    }


}
