package org.shoulder.core.i18;

import com.alibaba.ttl.TransmittableThreadLocal;
import lombok.extern.slf4j.Slf4j;
import org.shoulder.core.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * 国际化工具类
 *
 * 能力：
 * <ul>
 *     <li> 自动适配上下文，从当前用户或请求头中获取语言标识
 *     <li> 在子线程中仍然可以正常使用
 *     <li> 翻译找不到对应语言的翻译值时，可配置miss策略（直接返回传入的languageKey、返回 null、返回特定语言翻译、产生异常等）
 * </ul>
 *
 * @author lym
 */
@Slf4j
@Service
public class DefaultI18Translator implements I18nTranslator {

    private final I18nTranslator i18nTranslator;

    /**
     * 在线程池中也可自动继承，
     * 使用时注意该变量应该为稳定的，因为操作该变量变更会导致父子线程变更
     */
    private ThreadLocal<Locale> threadLanguageLocale = new TransmittableThreadLocal<>();

    @Autowired
    public DefaultI18Translator(I18nTranslator i18nTranslator) {
        this.i18nTranslator = i18nTranslator;
    }

    /**
     * 获取区域信息
     *
     * @return 位置和语言信息
     */
    @Override
    public Locale getLocale() {
        // 尝试从上下文（当前用户）中获取
        String[] str = "zh_CN".split("_");
        String languageId = str[0];
        String country = "";
        if (str.length > 1) {
            country = str[1];
        }
        //获取不到语言时默认为中文?
        Locale locale = StringUtils.isBlank(languageId) ? new Locale("zh") :
                StringUtils.isBlank(country) ? new Locale(languageId) :
                        new Locale(languageId, country);
        this.threadLanguageLocale.set(locale);
        return locale;
    }

    /**
     * 多语言翻译,允许带参数
     *
     * @param languageKey 待翻译的多语言key
     * @return 翻译结果
     */
    @Override
    public String tr(String languageKey, String... params) {
        return trSpecial(getLocale(), languageKey, params);
    }


    /**
     * 多语言翻译
     *
     * @param translatable 可翻译的目标
     * @return 翻译结果
     */
    @Override
    public String tr(Translatable translatable, String... params) {
        return trSpecial(getLocale(), translatable.getLanguageKey(), params);
    }

    /**
     * 使用特定的语言翻译 languageKey
     * @param languageId    指定的翻译语言
     * @param languageKey   待翻译的多语言key
     * @return  翻译结果
     */
    @Override
    public String trSpecial(String languageId, String languageKey, String... params) {
        return this.translate(languageId, languageKey, params);
    }


    /**
     * 多语言翻译
     * @param locale 地区和多语言信息
     * @param languageKey 待翻译的多语言key
     * @return  翻译结果
     */
    @Override
    public String trSpecial(Locale locale, String languageKey, String... params) {
        String languageId = locale.getLanguage() + "_" + locale.getCountry();
        return this.translate(languageId , languageKey, params);
    }

    /**
     * 最底层翻译接口方法
     */
    private String translate(String languageId, String languageKey, String... params){
        // todo
        return null;
    }

    /**
     * 刷新清空多语言缓存
     */
    @Override
    public void refresh() {
        //TODO
    }

    /**
     * 清空线程变量
     */
    @Override
    public void clean() {
        threadLanguageLocale.remove();
    }
}
