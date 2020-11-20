package org.shoulder.core.i18;

import org.shoulder.core.context.AppInfo;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Spring 多语言/翻译 接口实现
 * <p>
 * 主要特点：在 Spring 的基础上增加了按 语种文件夹 加载多语言文件的方式
 * <p>
 * 额外支持了基于语种文件夹的加载方式： 多语言资源文件路径/{语言标识}_{地区标识}/xxx.properties(.xml)
 * 【翻译文件按照语言分文件夹存放，支持按模块管理多语言文件，方便维护与扩展】
 * <p>
 * 若要清空多语言缓存，通过父类方法 {@link ReloadableResourceBundleMessageSource#clearCacheIncludingAncestors}
 *
 * @author lym
 */
public class ReloadableLocaleDirectoryMessageSource extends ReloadableResourceBundleMessageSource implements Translator {

    private ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

    public ReloadableLocaleDirectoryMessageSource() {
        // 默认会加载 classpath*:language 中的多语言（便于自定义jar包中扩充，优先级较低，优先使用用户的）
        super.addBasenames("classpath*:language");
    }

    /**
     * 通过 MessageSourceAccessor 简化使用
     */
    public static MessageSourceAccessor getAccessor() {
        return new MessageSourceAccessor(new ReloadableLocaleDirectoryMessageSource(), AppInfo.defaultLocale());
    }

    public static MessageSourceAccessor getAccessor(Locale defaultLocale) {
        return new MessageSourceAccessor(new ReloadableLocaleDirectoryMessageSource(), defaultLocale);
    }

    /**
     * 获取当前多语言环境信息
     *
     * @return 位置和语言信息
     */
    @Override
    public Locale currentLocale() {
        // 尝试从上下问（当前请求/用户）中获取，然后取 AppInfo 中的，然后取配置的
        Locale currentLocale;
        return (currentLocale = Translator.super.currentLocale()) != null ? currentLocale : getDefaultLocale();
    }

    /**
     * 加载特定语言对应的资源文件。在这里解析通配符
     *
     * @return 多语言资源路径
     */
    @Nonnull
    @Override
    protected List<String> calculateFilenamesForLocale(String basename, Locale locale) {
        // 先放入 super 的，优先级最低
        List<String> result = new LinkedList<>(super.calculateFilenamesForLocale(basename, locale));
        String language = locale.getLanguage();
        String country = locale.getCountry();
        String variant = locale.getVariant();
        StringBuilder temp = new StringBuilder(basename);
        temp.append('/');
        boolean hasLanguage = language.length() > 0;
        if (hasLanguage) {
            temp.append(language);
            result.addAll(0, listLanguageSourceDir(temp.toString()));
        }

        temp.append('_');
        boolean hasCountry = country.length() > 0;
        if (hasCountry) {
            temp.append(country);
            result.addAll(0, listLanguageSourceDir(temp.toString()));
        }

        if (variant.length() > 0 && (hasLanguage || hasCountry)) {
            temp.append('_').append(variant);
            result.addAll(0, listLanguageSourceDir(temp.toString()));
        }
        return result;
    }

    /**
     * 列出 basename 目录下所有要加载多语言文件（支持 jar/文件系统）
     *
     * @param basename 多语言路径，如 classpath*:language
     * @return 举例 language/zh_CN、language/en_US
     */
    @Nonnull
    private List<String> listLanguageSourceDir(String basename) {
        /*if(!basename.startsWith("classpath")){
            return Collections.emptyList();
        }*/
        Resource[] resources;
        try {
            // 扫描可能会很慢
            resources = resourcePatternResolver.getResources(basename + "/*");
        } catch (IOException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Could resolve resourcePath [" + basename + "]", e);
            }
            return Collections.emptyList();
        }
        if (resources.length == 0) {
            return Collections.emptyList();
        }
        return Arrays.stream(resources)
            .map(r -> {
                try {
                    return r.getURI().toString();
                } catch (IOException e) {
                    return "";
                }
            })
            // 只取 .properties .xml
            .filter(this::withResolveSuffix)
            .map(name -> name.replace(".properties", "").replace(".xml", ""))
            .collect(Collectors.toList());
    }

    /**
     * 是否包含可解析的文件后缀名
     *
     * @param fileName 文件名 如 xxx.properties
     * @return 是否可以解析（properties、xml）
     */
    private boolean withResolveSuffix(String fileName) {
        return fileName.endsWith(".properties") || fileName.endsWith(".xml");
    }

}
