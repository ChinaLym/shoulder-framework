package org.shoulder.core.i18;

import lombok.extern.shoulder.SLog;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 实现了 spring i18n 接口的默认实现类 {@link ReloadableResourceBundleMessageSource}，对比如下：
 *
 *  Spring 多语言文件命名限制：由于其采用了 jdk {@link ResourceBundle} 的思想加载多语言文件（优先 资源名_语言_地区_变种 全匹配，然后 资源名_语言_地区 匹配...3次 系统语言对应文件名3次，传入的资源名）
 *  本类将加载文件的方式开放，改为以 language/{语言标识}_{地区标识}/{资源名}.properties 其中扩展名默认支持 properties。【目的：使得资源文件名称只包含关键字，每种语言按文件夹存放，容易维护】
 *  本类引入手动刷新机制
 *
 * @author lym
 */
@SLog
public class ShoulderReloadableResourceBundleMessageSource extends ReloadableResourceBundleMessageSource {

    private final ConcurrentMap<String, List<String>> cachedFilenamesMap = new ConcurrentHashMap<>();

    @Override
    @NonNull
    protected List<String> calculateAllFilenames(@Nullable String basename, Locale locale) {

        //return super.calculateAllFilenames(basename, locale);

        String language = locale.getLanguage();
        String country = locale.getCountry();
        String cacheFilenamesKey = language + "_" + country;
        List<String> cachedFilenames = this.cachedFilenamesMap.get(cacheFilenamesKey);
        if (cachedFilenames != null) {
            return cachedFilenames;
        }

        final List<String> notFound = Collections.emptyList();

        Resource resource = loadResource();
        if (!resource.exists()) {
            return notFound;
        }
        List<String> filenames = new LinkedList<>();
        try {
            if (!resource.getFile().isDirectory()) {
                return notFound;
            }
            File[] localeFiles = resource.getFile().listFiles();
            if(localeFiles == null){
                return notFound;
            }
            Arrays.stream(localeFiles).forEach(localeFile -> {
                String localeFileName = localeFile.getName();
                String[] parts = localeFileName.split("_");
                if (parts.length == 2) {
                    Locale locale1 = new Locale(parts[0], parts[1]);
                    String resourceLanguage = parts[0];
                    String resourceCountry = parts[1];
                    /**
                     * 当请求语言与遍历多语言的语言一致
                     */
                    boolean fullMatch = false;
                    if (resourceLanguage.equals(language)) {
                        if (resourceCountry.equals(country)) {
                            filenames.addAll(0, getLocaleFilenames(localeFile, locale1));
                            fullMatch = true;
                        } else {
                            if (fullMatch) {
                                filenames.addAll(1, getLocaleFilenames(localeFile, locale1));
                            } else {
                                filenames.addAll(0, getLocaleFilenames(localeFile, locale1));
                            }
                        }
                    } else if (parts[0].equals("zh")) { // 中文默认自动添加
                        filenames.addAll(filenames.size(), getLocaleFilenames(localeFile, locale1));
                    }
                }
            });
        } catch (IOException e) {
            logger.warn("file error:", e);
        }
        cachedFilenamesMap.put(cacheFilenamesKey, filenames);
        return filenames;
    }

    private List<String> getLocaleFilenames(File localeFileDir, Locale locale) {
        List<String> filenames = new LinkedList<>();
        File[] languageFiles = localeFileDir.listFiles();
        for (File languageFile : languageFiles) {
            String realBasename = languageFile.toURI().toString().replace(".properties", "");
            List<String> calculatedFileNames = super.calculateAllFilenames(realBasename, locale);
            filenames.addAll(calculatedFileNames);
        }
        return filenames;
    }

    @NonNull
    @Override
    protected List<String> calculateFilenamesForLocale(String basename, Locale locale) {
        return Collections.emptyList();
    }

    protected Resource loadResource() {
        if (true) {

            return new ClassPathResource("language");
        } else {
            // todo
            return new FileSystemResource("");
        }
    }


}
