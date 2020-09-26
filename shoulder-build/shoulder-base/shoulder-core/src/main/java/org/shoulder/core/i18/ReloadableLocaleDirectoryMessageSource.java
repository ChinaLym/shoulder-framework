package org.shoulder.core.i18;

import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Spring 多语言/翻译 接口实现
 *
 * 在 Spring 默认实现类基础上增加了按语种文件名加载多语言文件的方式；Translator 实现：
 * <p>
 * 额外支持了基于路径的加载方式： 多语言资源文件路径/{语言标识}_{地区标识}/{资源名}.properties/.xml
 * 【使得资源文件名称只包含关键字，翻译文件按照语言分文件夹存放，容易维护与扩展】
 * <p>
 * 自动适配上下文，增加不需要传入语种参数，取值顺序：从当前用户或请求头中获取语言标识、其次设置的默认语言、其次系统语言
 * <p>
 * 约定：默认会加载 classpath*:language 中的多语言，作为 spi 便于自定义jar包中扩充，优先级较低，优先使用用户的
 * <p>
 * 多语言文件命名限制：由于 Spring 采用了 jdk {@link ResourceBundle} 的思想加载多语言文件，故对多语言资源文件命名有一定限制
 * 顺序：尝试加载传入语言： 资源名_语言_地区_变种 > 资源名_语言_地区 > 资源名_语言。再使用系统语言尝试加载
 * <p>
 * 翻译场景推荐 注：Thymeleaf、FreeMark 等动态页面由后端翻译，html静态页面或前后分离时推荐由前端翻译
 * 若有大量重复 message 映射时，如多租户，每个租户可以定制自己的界面和提示信息，可采用继承方式简化多语言管理
 * <p>
 * 若要清空多语言缓存，通过父类方法 {@link ReloadableResourceBundleMessageSource#clearCacheIncludingAncestors}
 *
 * @author lym
 */
public class ReloadableLocaleDirectoryMessageSource extends ReloadableResourceBundleMessageSource implements Translator {

    private ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

    public ReloadableLocaleDirectoryMessageSource() {
        // 默认约定，spi
        super.addBasenames("classpath*:language");
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
        return (currentLocale = Translator.super.currentLocale()) != null ? currentLocale :  getDefaultLocale();
    }

    /**
     * 加载特定语言对应的资源文件。在这里解析通配符
     *
     * @return 多语言资源路径
     */
    @NonNull
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
    @NonNull
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
        return fileName.endsWith(".properties") ||fileName.endsWith(".xml");
    }

}
