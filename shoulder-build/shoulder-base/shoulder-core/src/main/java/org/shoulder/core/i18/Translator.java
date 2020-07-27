package org.shoulder.core.i18;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;

/**
 * 国际化(翻译)工具类
 *
 * @author lym
 */
public interface Translator extends MessageSource {

    /**
     * 多语言自动翻译
     * @param languageKey   待翻译的多语言 key
     * @param args          填充参数（支持嵌套翻译，如参数为 MessageSourceResolvable）
     * @return  翻译后的
     */
    String getMessage(String languageKey, Object... args);

    /**
     * 多语言自动翻译
     * @param translatable 可翻译的目标
     * @return 翻译后的
     */
    String getMessage(MessageSourceResolvable translatable);

}
