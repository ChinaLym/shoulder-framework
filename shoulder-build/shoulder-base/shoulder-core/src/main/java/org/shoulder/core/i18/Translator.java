package org.shoulder.core.i18;

import org.shoulder.core.context.AppContext;
import org.shoulder.core.context.AppInfo;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;

import java.util.Locale;

/**
 * 国际化(翻译)工具类
 * 在 spring 翻译的核心接口 {@link MessageSource} 基础上额外提供了两个使用的更简单方法
 * getMessage 时不再需要传语言标识，简化使用（默认语言标识为 {@link AppContext#getLocale}）
 *
 * @author lym
 */
public interface Translator extends MessageSource {

    /**
     * 多语言自动翻译
     *
     * @param languageKey 待翻译的多语言 key
     * @param args        填充参数（支持嵌套翻译，如参数为 MessageSourceResolvable）
     * @return 翻译后的
     */
    default String getMessage(String languageKey, Object... args) {
        return getMessage(languageKey, args, currentLocale());
    }

    /**
     * 多语言自动翻译
     *
     * @param translatable 可翻译的目标
     * @return 翻译后的
     */
    default String getMessage(MessageSourceResolvable translatable) {
        return getMessage(translatable, currentLocale());
    }

    /**
     * 获取当前多语言环境信息
     * 默认先尝试从上下问（当前请求/用户）中获取，然后取 AppInfo 中的，然后取配置的
     *
     * @return 位置和语言信息
     */
    default Locale currentLocale() {
        // 尝试从上下（当前用户）中获取，然后取 AppInfo 中的，然后取配置的
        Locale currentLocale;
        return (currentLocale = AppContext.getLocale()) != null ? currentLocale : AppInfo.defaultLocale();
    }

}
