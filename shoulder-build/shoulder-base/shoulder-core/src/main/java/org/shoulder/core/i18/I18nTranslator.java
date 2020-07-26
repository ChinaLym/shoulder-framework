package org.shoulder.core.i18;

import java.util.Locale;

/**
 * 国际化(翻译)工具类
 * 默认以 HTTP header 的 Accept-Language 作为标记语言。
 * 翻译场景推荐 注：Thymeleaf、FreeMark 等动态页面由后端翻译，html静态页面或前后分离时推荐由前端翻译
 *
 * @author lym
 */
public interface I18nTranslator {

    /**
     * 获取多语言区域信息
     *  包含语言和地区，同时会将返回值写入线程变量
     *
     * @return 位置和语言信息
     */
    Locale getLocale();

    /**
     * 多语言自动翻译
     * @param languageKey   待翻译的多语言 key
     * @param params        填充参数
     * @return  翻译后的
     */
    String tr(String languageKey, String... params);

    /**
     * 多语言自动翻译
     * @param translatable 可翻译的目标
     * @param params 填充参数
     * @return 翻译后的
     */
    String tr(Translatable translatable, String... params);

    /**
     * 使用指定的语言翻译 (直接依赖框架实现)
     * @param languageId    指定的翻译语言
     * @param languageKey   待翻译的多语言 key
     * @param params 填充参数
     * @return  翻译后的
     */
    String trSpecial(String languageId, String languageKey, String... params);

    /**
     * 使用指定的地区翻译 ，允许带参数 (直接依赖框架实现)
     * @param locale 地区和多语言信息
     * @param languageKey 待翻译的多语言key
     * @param params 填充参数
     * @return  翻译后的
     */
    String trSpecial(Locale locale, String languageKey, String... params);

    /**
     * 重置多语言缓存
     * 清空多语言缓存，重新读取多语言资源文件
     */
    void refresh();

    /**
     * 清空线程变量
     */
    void clean();
}
