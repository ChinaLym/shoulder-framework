package org.shoulder.core.i18;

import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
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
 * 约定：默认会加载 classPath:language 中的多语言，作为 spi 便于自定义jar包中扩充，优先级较低，优先使用用户的
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

    private static final Logger log = LoggerFactory.getLogger(ReloadableLocaleDirectoryMessageSource.class);

    private ResourceLoader resourceLoader = new DefaultResourceLoader();

    public ReloadableLocaleDirectoryMessageSource() {
        super.addBasenames("classpath:language");
    }

    @Override
    public void setResourceLoader(@Nullable ResourceLoader resourceLoader) {
        super.setResourceLoader(resourceLoader);
        this.resourceLoader = (resourceLoader != null ? resourceLoader : new DefaultResourceLoader());
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
     * 加载特定语言对应的资源文件
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
            result.addAll(0, listLanguageSourceDir(temp.toString(), locale));
        }

        temp.append('_');
        boolean hasCountry = country.length() > 0;
        if (hasCountry) {
            temp.append(country);
            result.addAll(0, listLanguageSourceDir(temp.toString(), locale));
        }

        if (variant.length() > 0 && (hasLanguage || hasCountry)) {
            temp.append('_').append(variant);
            result.addAll(0, listLanguageSourceDir(temp.toString(), locale));
        }

        return result;
    }

    /**
     * 列出 basename 目录下所有要加载多语言文件（支持 jar/文件系统）
     *
     * @param basename 多语言路径，如 classpath:language
     * @return 举例 language/zh_CN、language/en_US
     */
    @NonNull
    private List<String> listLanguageSourceDir(String basename, Locale locale) {
        Resource resource = resourceLoader.getResource(basename);
        if (!resource.exists()) {
            log.debug("i18n basename(" + resource.getDescription() + ") not found");
            return Collections.emptyList();
        }
        try {
            String fileUrlName = resource.getURL().getFile();
            if (fileUrlName.contains(".jar")) {
                return loadI18nResourceFromJar(basename, locale, resource);
            }
            return loadI18nResourceFromFileSystem(locale, resource);
        }catch (IOException e){
            // 加载失败不应报错，而是返回未加载到
            logger.debug("load fail. basename=" + basename + ", locale=" + locale, e);
            return Collections.emptyList();
        }
    }

    // -------------------- 从文件系统中加载 ------------------------


    /**
     * 从文件系统中获取多语言资源文件
     *
     * @param locale   语种
     * @param resource 待加载的文件夹，默认 language
     * @return 资源文件加载通配符
     * @throws IOException 加载失败
     */
    private List<String> loadI18nResourceFromFileSystem(Locale locale, Resource resource) throws IOException {
        if (!resource.getFile().isDirectory()) {
            // 否则必须为本地的文件夹，不然不支持
            log.debug("i18n resource(" + resource.getDescription() + ") not a directory");
            return Collections.emptyList();
        }
        List<String> resourceFilePattern = new LinkedList<>();
        File[] languageDirs = resource.getFile().listFiles();
        // languageDirs item 举例 "language/zh_CN"
        if (languageDirs == null) {
            // 目录为空
            log.debug("resource(" + resource.getDescription() + ") contains 0 file named like '<translate>.properties'.");
            return Collections.emptyList();
        }
        String aimLanguage = locale.getLanguage();
        String aimCountry = locale.getCountry();
        for (File specialLanguageDir : languageDirs) {
            String[] dirLocaleParts = specialLanguageDir.getName().split("_");
            if (dirLocaleParts.length < 2) {
                log.debug("File name({}) not contains countryCode. " +
                        "Recommend: <languageCode>_<countryCode> example: 'zh_CN'. File absolutePath:{}",
                    specialLanguageDir.getName(), specialLanguageDir.getAbsolutePath());
            }
            String dirLanguage = dirLocaleParts[0];
            String dirCountry = dirLocaleParts.length > 1 ? dirLocaleParts[1] : "";
            if (!aimLanguage.equals(dirLanguage)) {
                // 语言不匹配直接返回
                continue;
            }
            if (aimCountry.equals(dirCountry)) {
                // 地区也匹配，优先级更高，放置在最前
                resourceFilePattern.addAll(0, getLocaleFilePatterns(specialLanguageDir));
            } else {
                resourceFilePattern.addAll(getLocaleFilePatterns(specialLanguageDir));
            }
            // 这里未加载默认语言资源
        }
        return resourceFilePattern;
    }

    /**
     * 获取特定语言文件夹下所有的（properties 或 xml）文件
     *
     * @param specialLanguageDir language/zh_CN
     * @return 加载这些文件对应的 spring resource 表达式，如 file:///d:/language/zh_CN
     */
    private List<String> getLocaleFilePatterns(File specialLanguageDir) {
        if (!specialLanguageDir.isDirectory()) {
            // 必须为文件夹
            return Collections.emptyList();
        }
        File[] specialLanguageResources = specialLanguageDir.listFiles();
        if (specialLanguageResources == null) {
            // 空文件夹
            return Collections.emptyList();
        }
        // 加载该文件下的所有 properties 和 xml 文件（同名不同类型可能导致重复加载，最好不要多个格式混用）
        return Arrays.stream(specialLanguageResources)
            .map(File::getAbsolutePath)
            .filter(this::canResolve)
            .map(name -> name.replace(".properties", ""))
            .map(name -> name.replace(".xml", ""))
            .map(name -> "file:///" + name)
            .collect(Collectors.toList());
    }

    // -------------------- 从 jar 里加载 ------------------------

    /**
     * 从 jar 里获取多语言资源文件
     *
     * @param basename 资源名，默认为 language
     * @param locale   语种
     * @param resource 待加载的 jar
     * @return 资源文件加载通配符
     * @throws IOException 加载失败
     */
    @NonNull
    protected List<String> loadI18nResourceFromJar(String basename, Locale locale, @NonNull Resource resource) throws IOException {
        // jar 内资源
        String[] parts = resource.getURL().getFile().split(".jar");
        String realPath = parts[0].replace("file:", "") + ".jar";
        List<String> relativeJarPath = listAllResourceFromJar(realPath, basename);
        if (CollectionUtils.isEmpty(relativeJarPath)) {
            return Collections.emptyList();
        }
        return relativeJarPath.stream()
            .map(path -> {
                String resourcePath = "classpath:" + path;
                return resourceLoader.getResource(resourcePath).isReadable() ? resourcePath : null;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }


    /**
     * 列出 jarFilePath 对应 jar 的 resourceDirName 目录中的 properties、xml 文件
     *
     * @param jarFilePath jar 文件路径，如 "/F:/files/mavenRepository/cn/itlym/shoulder-core/1.0/shoulder-core-1.0.jar"
     * @param baseName    如 classpath:language
     * @return 相对于该 jar 的resource path
     * @throws IOException 读取jar文件失败
     */
    private List<String> listAllResourceFromJar(String jarFilePath, String baseName) throws IOException {
        String resourceDirName = baseName;
        if (resourceDirName.contains(":")) {
            resourceDirName = baseName.split(":")[1];
        }
        JarFile jarFile = new JarFile(jarFilePath);
        Enumeration<JarEntry> entries = jarFile.entries();
        List<String> resourcePaths = new LinkedList<>();
        while (entries.hasMoreElements()) {
            JarEntry jarEntry = entries.nextElement();
            String entryName = jarEntry.getRealName();
            if (entryName.startsWith(resourceDirName) && canResolve(entryName)) {
                // 如 language/zh_CN/messages.properties
                resourcePaths.add(entryName);
            }
        }
        return resourcePaths;
    }

    /**
     * 是否支持该文件
     *
     * @param fileName 文件名 如 xxx.properties
     * @return 是否可以解析（properties、xml）
     */
    private boolean canResolve(String fileName) {
        return fileName.endsWith(".properties") ||fileName.endsWith(".xml");
    }
}
