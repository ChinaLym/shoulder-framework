package org.shoulder.autoconfigure.web;

import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;
import org.shoulder.core.util.AssertUtils;
import org.shoulder.web.template.dictionary.model.DictionaryEnum;
import org.shoulder.web.template.dictionary.spi.DictionaryEnumStore;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 供使用者根据扫描包路径注册枚举类
 *
 * @author lym
 */
public class PackageScanDictionaryEnumRepositoryRegister implements DictionaryEnumRepositoryCustomizer {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Set<String> toScanPackages;

    public PackageScanDictionaryEnumRepositoryRegister(Collection<? extends CharSequence> toScanPackages) {
        AssertUtils.notNull(toScanPackages, CommonErrorCodeEnum.CODING);
        this.toScanPackages = toScanPackages.stream().map(CharSequence::toString).collect(Collectors.toSet());
    }

    @Override public void customize(DictionaryEnumStore dictionaryEnumStore) {
        toScanPackages.forEach(packageName -> {
            try {
                // 加載 packageName 下的 所有.class文件
                PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
                packageName = packageName.replace('.', '/');
                String classPath = "classpath*:" + packageName + (packageName.endsWith("/") ? "" : "/") + "**/*.class";
                Resource[] resources = resolver.getResources(classPath);
                int count = 0;
                for (Resource res : resources) {
                    // 先获取resource的元信息，然后获取class元信息，最后得到 class 全路径
                    String clsName = new SimpleMetadataReaderFactory().getMetadataReader(res).getClassMetadata().getClassName();
                    // 通过名称加载
                    Class<?> clazz = Class.forName(clsName);
                    if (clazz.isEnum() && DictionaryEnum.class.isAssignableFrom(clazz)) {
                        dictionaryEnumStore.register((Class<? extends Enum<? extends DictionaryEnum<?, ?>>>) clazz);
                        logger.info("register " + clsName + " to DefaultDictionaryEnumStore.");
                        count++;
                    }
                }
                logger.info("scan package(" + packageName + ") register " + count + " classes to DictionaryEnumStore.");
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });

    }

}
