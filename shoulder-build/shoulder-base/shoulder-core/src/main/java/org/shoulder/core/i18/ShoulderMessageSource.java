package org.shoulder.core.i18;

import org.shoulder.core.context.AppContext;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;
import org.springframework.context.MessageSourceResolvable;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * spring i18n 接口实现，与其默认实现类 {@link ReloadableResourceBundleMessageSource} 对比如下：
 *
 * <p>
 * 多语言文件命名限制：由于 Spring 采用了 jdk {@link ResourceBundle} 的思想加载多语言文件，故对多语言资源文件命名有一定限制
 * 优先 资源名_语言_地区_变种 全匹配，然后 资源名_语言_地区 匹配...3次系统语言对应文件名3次，传入的资源名
 * <p>
 * 本类修改资源文件加载文件的方式，将其文件名约束改为文件路径约束。改为 多语言资源文件路径/{语言标识}_{地区标识}/{资源名}.properties
 * 其中扩展名默认支持 properties。【目的：使得资源文件名称只包含关键字，每种语言按文件夹存放，容易维护】
 * 本类只加载上述特定语言文件夹下的，而不加载系统语言对应的默认资源
 * 自动适配上下文，从当前用户或请求头中获取语言标识
 * <p>
 * 约定：会加载 classPath:language 中的多语言，便于自定义jar包中扩充，优先级较低，优先使用用户的
 * <p>
 * 翻译场景推荐 注：Thymeleaf、FreeMark 等动态页面由后端翻译，html静态页面或前后分离时推荐由前端翻译
 * 若有大量重复 message 映射时，如多租户，每个租户可以定制自己的界面和提示信息，可采用继承方式简化多语言管理
 * <p>
 * 清空多语言缓存，通过父类方法 {@link ReloadableResourceBundleMessageSource#clearCacheIncludingAncestors}
 * todo 目前仅支持文件加载，需要支持Spring中Resource允许的加载方式
 *
 * @author lym
 */
public class ShoulderMessageSource extends ReloadableResourceBundleMessageSource implements Translator {

    private static final Logger log = LoggerFactory.getLogger(ShoulderMessageSource.class);

    private final ConcurrentMap<String, List<String>> cachedFilenamesMap = new ConcurrentHashMap<>();

    private ResourceLoader resourceLoader = new DefaultResourceLoader();

    private static final String[] supportFile = new String[]{".properties", "set"};

    public ShoulderMessageSource() {
        super.addBasenames("classpath:language");
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
        Locale currentLocale = AppContext.getLocale();
        return currentLocale == null ? getDefaultLocale() : currentLocale;
    }


    /**
     * 根据模块名（basename）和语言标识列出所有多语言文件名称
     * 这里忽略 basename，只使用 locale
     */
    @Override
    @NonNull
    protected List<String> calculateAllFilenames(@NonNull String basename, @NonNull Locale locale) {

        String aimLanguage = locale.getLanguage();
        String aimCountry = locale.getCountry();
        String cacheFilenamesKey = basename + "." + aimLanguage + "_" + aimCountry;
        List<String> cachedFilenames = this.cachedFilenamesMap.get(cacheFilenamesKey);
        if (cachedFilenames != null) {
            return cachedFilenames;
        }

        List<String> resourceFilePattern = new LinkedList<>();
        List<File> languageSourceDirs = Collections.emptyList();
        try {
            languageSourceDirs = listLanguageSourceDir(basename);
        } catch (IOException e) {
            logger.warn("load i18n file(basename:" + basename + ") fail:", e);
        }
        for (File specialLanguageDir : languageSourceDirs) {
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
        if (CollectionUtils.isEmpty(resourceFilePattern)) {
            // 没有 basename 文件夹/locale/*.properties
            log.info("can't load any file with basename({}), locale({})", basename, locale);
        } else {
            cachedFilenamesMap.put(cacheFilenamesKey, resourceFilePattern);
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
        if(!specialLanguageDir.isDirectory()){
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

    /**
     * 列出资源目录下所有多语言文件
     *
     * @param resourceDir 多语言路径，如 language
     * @return 举例 language/zh_CN、language/en_US
     */
    @NonNull
    private List<File> listLanguageSourceDir(String resourceDir) throws IOException {
        Resource resource = resourceLoader.getResource(resourceDir);
        if (!resource.exists()) {
            log.debug("i18n resourceDir(" + resource.getDescription() + ") not found");
            return Collections.emptyList();
        }
        String fileUrlName = resource.getURL().getFile();
        if(fileUrlName.contains(".jar")){
            // jar 内资源
            String[] parts = fileUrlName.split(".jar");
            String realPath = parts[0].replace("file:", "") + ".jar";
            List<String> relativeJarPath = getJarFileResource(realPath, resourceDir);
        } else if (!resource.getFile().isDirectory()) {
            // 否则必须为本地的文件夹，不然不支持
            log.debug("i18n resource(" + resource.getDescription() + ") not a directory");
            return Collections.emptyList();
        }

        File[] languageDirs = resource.getFile().listFiles();
        // languageDirs item 举例 "language/zh_CN"
        if (languageDirs == null) {
            // 目录为空
            log.debug("resource(" + resource.getDescription() + ") contains 0 file named like '<translate>.properties'.");
            return Collections.emptyList();
        }
        return Arrays.asList(languageDirs);
    }

    /**
     * 列出 jarFilePath 对应 jar 的 resourceDirName 目录中的 properties、xml 文件
     *
     * @param jarFilePath jar 文件路径，如 "/F:/files/mavenRepository/cn/itlym/shoulder-core/1.0/shoulder-core-1.0.jar"
     * @param resourceDirName 资源文件夹名称，如 language
     * @return 相对于该 jar 的resource path
     * @throws IOException 读取jar文件失败
     */
    private List<String> getJarFileResource(String jarFilePath, String resourceDirName) throws IOException {
        JarFile jarFile = new JarFile(jarFilePath);
        Enumeration<JarEntry> entries = jarFile.entries();
        List<String> resourcePaths = new LinkedList<>();
        while (entries.hasMoreElements()){
            JarEntry jarEntry = entries.nextElement();
            String entryName = jarEntry.getRealName();
            if(entryName.startsWith(resourceDirName) && canResolve(entryName)){
                // 如 language/zh_CN/messages.properties
                resourcePaths.add(entryName);
            }
        }
        return resourcePaths;
    }

    /**
     * 是否支持该文件
     * @param fileName 文件名 如 xxx.properties
     * @return 是否可以解析（properties、xml）
     */
    private boolean canResolve(String fileName){
        for (String fileNameSub : supportFile) {
            if(fileName.endsWith(fileNameSub)){
                return true;
            }
        }
        return false;
    }
}
