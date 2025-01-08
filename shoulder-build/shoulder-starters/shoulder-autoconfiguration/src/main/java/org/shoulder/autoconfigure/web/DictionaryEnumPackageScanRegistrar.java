package org.shoulder.autoconfigure.web;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 枚举类字典扫描路径
 *
 * @author lym
 */
public final class DictionaryEnumPackageScanRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(@Nonnull AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry,
                                        @Nonnull BeanNameGenerator importBeanNameGenerator) {

//        String pkgName = getPackageName(importingClassMetadata) + ".enums";
        String[] scanEnumDicPkgs = (String[]) importingClassMetadata
                .getAnnotationAttributes(EnableDictionaryItemEnum.class.getName())
                .get("value");

        GenericBeanDefinition autoScanEnumDictionaryBeanDefinition = (GenericBeanDefinition) BeanDefinitionBuilder.
                genericBeanDefinition(PackageScanDictionaryEnumRepositoryRegister.class)
                .addConstructorArgValue(Arrays.stream(scanEnumDicPkgs).collect(Collectors.toSet()))
                .getBeanDefinition();

        registry.registerBeanDefinition("autoScanEnumDictionary", autoScanEnumDictionaryBeanDefinition);
    }

    private static String getPackageName(AnnotationMetadata metadata) {
        String className = metadata.getClassName();
        // 分割类名以获取包名
        String[] packageParts = className.split("\\.");
        StringBuilder packageName = new StringBuilder();
        for (int i = 0; i < packageParts.length - 1; i++) {
            packageName.append(packageParts[i]);
            if (i < packageParts.length - 2) {
                packageName.append(".");
            }
        }
        return packageName.toString();
    }
}