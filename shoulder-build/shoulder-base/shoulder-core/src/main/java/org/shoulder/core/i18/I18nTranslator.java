package org.shoulder.core.i18;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;

/**
 * 国际化(翻译)工具类
 * 默认以 HTTP header 的 Accept-Language 作为标记语言。
 * 翻译场景推荐 注：Thymeleaf、FreeMark 等动态页面由后端翻译，html静态页面或前后分离时推荐由前端翻译
 *
 * @author lym
 */
public interface I18nTranslator extends MessageSource {

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
