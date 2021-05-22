package org.shoulder.core.i18;

import org.shoulder.core.context.AppInfo;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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

    private final ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

    /**
     * 解决翻译报错：找不到文件 language.xml / language.properties —— 需要复写 calculateAllFilenames（目前考虑到无法调用它的缓存暂缓）
     */
    private final ConcurrentMap<String, Map<Locale, List<String>>> cachedFilenames = new ConcurrentHashMap();

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
     * 仅用以解决翻译报错
     */
    @Override
    @Nonnull
    protected List<String> calculateAllFilenames(@Nonnull String basename, @Nonnull Locale locale) {
        Map<Locale, List<String>> localeMap = this.cachedFilenames.get(basename);
        if (localeMap != null) {
            List<String> filenames = localeMap.get(locale);
            if (filenames != null) {
                return filenames;
            }
        }

        // Filenames for given Locale
        List<String> filenames = new ArrayList<>(7);
        filenames.addAll(calculateFilenamesForLocale(basename, locale));

        // Filenames for default Locale, if any
        Locale defaultLocale = getDefaultLocale();
        if (defaultLocale != null && !defaultLocale.equals(locale)) {
            List<String> fallbackFilenames = calculateFilenamesForLocale(basename, defaultLocale);
            for (String fallbackFilename : fallbackFilenames) {
                if (!filenames.contains(fallbackFilename)) {
                    // Entry for fallback locale that isn't already in filenames list.
                    filenames.add(fallbackFilename);
                }
            }
        }

        // 与 spring 源码不同点：忽略 baseName [即不包含默认值]
        //filenames.add(basename);
        List<String> availableFileNames = Arrays.stream(new String[]{".xml", ".properties"})
                .map(suffix -> extracted(basename, suffix))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        filenames.addAll(availableFileNames);

        if (localeMap == null) {
            localeMap = new ConcurrentHashMap<>();
            Map<Locale, List<String>> existing = this.cachedFilenames.putIfAbsent(basename, localeMap);
            if (existing != null) {
                localeMap = existing;
            }
        }
        localeMap.put(locale, filenames);

        // 与 spring 源码不同点，添加过滤（因 shoulder 增加支持 * 引入导致）
        return filenames.stream().filter(name -> !name.contains("*")).collect(Collectors.toList());
    }

    @Nullable
    private String extracted(String basename, String suffix) {
        try {
            if (!basename.endsWith(suffix)) {
                String xml = basename + suffix;
                Resource xmlResource = resourcePatternResolver.getResource(xml);
                String uri = xmlResource.getURI().toString();
                if (withResolveSuffix(uri)) {
                    return removeSuffix(uri);
                }
            }
        } catch (Exception ignore) {
            // ignore
        }
        return null;
    }

    /**
     * 加载特定语言对应的资源文件。在这里解析通配符
     *
     * @return 多语言资源路径
     */
    @Nonnull
    @Override
    protected List<String> calculateFilenamesForLocale(@Nonnull String basename, @Nonnull Locale locale) {
        // 先放入 super 的，优先级最低（这里是用于兼容 jdk / spring 约定的翻译文件路径）
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
     * @return 举例 language/zh_CN、language/en_US，不包含 baseName 自身
     */
    @Nonnull
    private List<String> listLanguageSourceDir(String basename) {
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
                .map(res -> {
                    try {
                        return res.getURI().toString();
                    } catch (IOException e) {
                        // 如果异常，说明无法读取，返回空即可
                        return "";
                    }
                })
                // 只取 .properties .xml
                .filter(this::withResolveSuffix)
                .map(this::removeSuffix)
                .collect(Collectors.toList());
    }

    /**
     * 是否包含可解析的文件后缀名
     *
     * @param fileName 文件名 如 xxx.properties
     * @return 是否可以解析（properties、xml）
     */
    private boolean withResolveSuffix(@Nonnull String fileName) {
        return fileName.endsWith(".properties") || fileName.endsWith(".xml");
    }

    private String removeSuffix(@Nonnull String uri) {
        return uri.replace(".properties", "").replace(".xml", "");
    }


    @Override
    protected Locale getDefaultLocale() {
        return AppInfo.defaultLocale();
    }

    @Override
    protected String getDefaultEncoding() {
        return AppInfo.charset().name();
    }
}
