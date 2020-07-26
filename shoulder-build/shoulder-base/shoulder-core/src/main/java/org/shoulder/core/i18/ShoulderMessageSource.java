package org.shoulder.core.i18;

import org.shoulder.core.context.BaseContextHolder;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;
import org.shoulder.core.util.StringUtils;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * spring i18n 接口实现，与其默认实现类 {@link ReloadableResourceBundleMessageSource} 对比如下：
 *
 * <p>
 * 多语言文件命名限制：由于 Spring 采用了 jdk {@link ResourceBundle} 的思想加载多语言文件，故对多语言资源文件命名有一定限制
 * （优先 资源名_语言_地区_变种 全匹配，然后 资源名_语言_地区 匹配...3次系统语言对应文件名3次，传入的资源名）
 * 本类修改资源文件加载文件的方式，将其文件名约束改为文件路径约束。改为 多语言资源文件路径/{语言标识}_{地区标识}/{资源名}.properties
 * 其中扩展名默认支持 properties。【目的：使得资源文件名称只包含关键字，每种语言按文件夹存放，容易维护】
 * 本类只加载上述特定语言文件夹下的，而不加载系统语言对应的默认资源
 * 本类除了允许 Spring 的定时重新加载资源文件外（默认关），额外引入手动刷新机制，允许使用者监听到某些事件后进行刷新
 *
 * <p>能力：
 * <ul>
 *  <li> 自动适配上下文，从当前用户或请求头中获取语言标识
 *  <li> todo 找不到对应语言的翻译值时，Spring 会抛异常，本类可配置miss策略（直接返回传入的languageKey、返回 null、返回特定语言翻译、产生异常等）
 * </ul>
 *
 * @author lym
 */
public class ShoulderMessageSource extends ReloadableResourceBundleMessageSource implements I18nTranslator {

    private static final Logger log = LoggerFactory.getLogger(ShoulderMessageSource.class);

    private final ConcurrentMap<String, List<String>> cachedFilenamesMap = new ConcurrentHashMap<>();

    private final Locale defaultLocale;

    private final String i18nResourceDir;

    private ResourceLoader resourceLoader = new DefaultResourceLoader();

    public ShoulderMessageSource(String directoryName, String charset, String defaultLocale, String i18nResourceDir) {
        super.setBasename(directoryName);
        super.setDefaultEncoding(charset);
        this.i18nResourceDir = i18nResourceDir;
        if (StringUtils.isEmpty(defaultLocale)) {
            this.defaultLocale = Locale.getDefault();
        } else {
            String[] parts = defaultLocale.split("_");
            if (parts.length == 1) {
                this.defaultLocale = new Locale(parts[0]);
            } else if (parts.length == 2) {
                this.defaultLocale = new Locale(parts[0], parts[1]);
            } else {
                this.defaultLocale = new Locale(parts[0], parts[1], parts[2]);
            }
        }
    }

    @Override
    public void setResourceLoader(@Nullable ResourceLoader resourceLoader) {
        super.setResourceLoader(resourceLoader);
        this.resourceLoader = (resourceLoader != null ? resourceLoader : new DefaultResourceLoader());
    }

    /**
     * 多语言翻译,允许带参数
     *
     * @param languageKey 待翻译的多语言key
     * @return 翻译结果
     */
    @Override
    public String getMessage(String languageKey, Object... params) {
        return getMessage(languageKey, params, currentLocale());
    }

    /**
     * 多语言翻译
     *
     * @param translatable 可翻译的目标
     * @return 翻译结果
     */
    @Override
    public String getMessage(MessageSourceResolvable translatable) {
        return getMessage(translatable, currentLocale());
    }


    /**
     * 获取当前多语言环境信息
     *
     * @return 位置和语言信息
     */
    private Locale currentLocale() {
        // 尝试从上下（当前用户）中获取
        String locale = BaseContextHolder.getLocale();
        if (StringUtils.isNotEmpty(locale)) {
            String[] str = BaseContextHolder.getLocale().split("_");
            if (str.length == 1) {
                return new Locale(locale);
            } else {
                return new Locale(str[1], str[2]);
            }
        }
        return defaultLocale;
    }


    /**
     * 清空多语言缓存 {@link super#clearCacheIncludingAncestors}
     */

    @Override
    @NonNull
    protected List<String> calculateAllFilenames(@Nullable String basename, @NonNull Locale locale) {

        String language = locale.getLanguage();
        String country = locale.getCountry();
        String cacheFilenamesKey = language + "_" + country;
        List<String> cachedFilenames = this.cachedFilenamesMap.get(cacheFilenamesKey);
        if (cachedFilenames != null) {
            return cachedFilenames;
        }


        List<String> filenames = new LinkedList<>();
        try {
            File[] languageSourceDirs = listAllLanguageSourceDirs();
            Arrays.stream(languageSourceDirs).forEach(specialLanguageDir -> {
                // 当且仅当语言和地区与期望值完全匹配时才选择
                String specialLanguageDirName = specialLanguageDir.getName();
                String[] parts = specialLanguageDirName.split("_");
                if (parts.length < 2) {
                    log.warn( "SpecialLanguageDir naming(" + specialLanguageDirName + ") illegal! " +
                            "It should be <LanguageCode>_<CountryCode> like 'zh_CN'.");
                }
                Locale localeFromDirNam = new Locale(parts[0], parts[1]);
                String resourceLanguage = parts[0];
                String resourceCountry = parts.length > 1 ? parts[1] : "";
                if (resourceLanguage.equals(language)) {
                    // 语言匹配就使用
                    filenames.addAll(0, getLocaleFilenames(specialLanguageDir, localeFromDirNam));
                    if (StringUtils.isNotEmpty(country) && country.equals(resourceCountry)) {
                        // 地区也匹配优先级更高
                        filenames.addAll(0, getLocaleFilenames(specialLanguageDir, localeFromDirNam));
                    }
                }
                // 不加载默认语言资源
            });
        } catch (IOException e) {
            logger.warn("load i18n file error:", e);
        }
        cachedFilenamesMap.put(cacheFilenamesKey, filenames);
        return filenames;
    }

    private List<String> getLocaleFilenames(File specialLanguageDir, Locale locale) {
        List<String> filenames = new LinkedList<>();
        File[] specialLanguageResource = specialLanguageDir.listFiles();
        if (specialLanguageResource == null) {
            // 不应该出现的
            return Collections.emptyList();
        }
        for (File languageFile : specialLanguageResource) {
            // 把该文件下的所有文件全都加载
            String realBasename = languageFile.toURI().toString().replace(".properties", "");
            List<String> calculatedFileNames = super.calculateAllFilenames(realBasename, locale);
            filenames.addAll(calculatedFileNames);
        }
        return filenames;
    }


    @NonNull
    private File[] listAllLanguageSourceDirs() throws IOException {
        Resource resource = resourceLoader.getResource(i18nResourceDir);
        File[] languageDirs;
        if (!resource.exists()) {
            throw new FileNotFoundException("resource(" + resource.getURI() + ") not found");
        } else if (!resource.getFile().isDirectory()) {
            throw new NotDirectoryException("resource(" + resource.getURI() + ") not a directory");
        } else if ((languageDirs = resource.getFile().listFiles()) == null) {
            throw new NoSuchFileException("resource(" + resource.getURI() + ") not contain any translate file like " +
                "'<translate>.properties'.");
        }
        return languageDirs;
    }

    /*@NonNull
    @Override
    protected List<String> calculateFilenamesForLocale(String basename, Locale locale) {
        // 由于重写了 calculateAllFilenames 故不会执行该方法
        return Collections.emptyList();
    }*/

}
