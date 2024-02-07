package org.shoulder.core.i18;

import org.shoulder.core.context.AppContext;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.MessageSourceAccessor;

import java.util.Locale;

/**
 * 国际化(翻译)工具类
 * 在 spring 翻译的核心接口 {@link MessageSource} 基础上额外提供了两个使用的更简单方法
 * getMessage 时自动适配上下文，不再需要传语言标识，简化使用（默认语言标识为 {@link AppContext#getLocale}）
 * 语种取值顺序：从当前用户或请求头中获取语言标识、其次设置的默认语言、其次系统语言
 *
 * <a href="https://www.cnblogs.com/fsjohnhuang/p/4094777.html">String.format详解</a>
 * <a href="https://blog.csdn.net/jeamking/article/details/7226656">大括号以及单引号问题</a>
 *
 * @author lym
 * @see MessageSourceAccessor Spring 的该类也有类似的功能
 */
public interface Translator extends MessageSource {

    /**
     * 多语言自动翻译
     *
     * @param i18nKey 待翻译的多语言 key
     * @param args        填充参数（支持嵌套翻译，如参数为 MessageSourceResolvable）
     * @return 翻译后的
     * @throws NoSuchMessageException 未找到 i18nKey 对应的翻译
     */
    default String getMessage(String i18nKey, Object... args) throws NoSuchMessageException {
        return getMessage(i18nKey, args, currentLocale());
    }

    /**
     * 多语言自动翻译
     *
     * @param translatable 可翻译的目标
     * @return 翻译后的
     * @throws NoSuchMessageException 未找到 languageKey 对应的翻译
     */
    default String getMessage(MessageSourceResolvable translatable) throws NoSuchMessageException {
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
        return AppContext.getLocaleOrDefault();
    }

}
