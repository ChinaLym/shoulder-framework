package org.shoulder.core.i18;

import org.springframework.context.MessageSourceResolvable;

/**
 * 可被翻译的（推荐用枚举类实现）
 * 对应 Spring 中的接口 {@link MessageSourceResolvable}
 *
 * @author lym
 */
public interface Translatable {
    /**
     * 对应的语言标识
     * @return 语言标识
     */
    String getLanguageKey();
}
