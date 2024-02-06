package org.shoulder.web.template.dictionary.convert;

import cn.hutool.core.util.ReflectUtil;
import org.shoulder.core.util.ContextUtils;
import org.shoulder.web.template.dictionary.model.DictionaryItem;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.convert.PropertyValueConverterRegistrar;
import org.springframework.data.mapping.PersistentProperty;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collection;

/**
 * 扫描 packageNameList 所有 @Document 类，为其所有 {@link DictionaryItem} 字段注册 str 转换器
 * 也就是 DictionaryItem 类型字段，持久化倒数据库时候，自动转换为 str 类型
 *
 * @author lym
 */
public class PackageScanDictionaryItem2StrConverterMongoConfiguration {

    private final Collection<? extends String> packageNameList;

    public PackageScanDictionaryItem2StrConverterMongoConfiguration(Collection<? extends String> packageNameList) {
        this.packageNameList = packageNameList;
    }

    public void registerAllDocumentDictionaryEnumFieldConverters(PropertyValueConverterRegistrar<? extends PersistentProperty> propertyValueConverterRegistrar) {
        packageNameList.forEach(packageName ->
            ContextUtils.loadClassInPackage(packageName,
                // mongodb 类
                clazz -> !clazz.isEnum() && Serializable.class.isAssignableFrom(clazz) && AnnotationUtils.isAnnotationDeclaredLocally(
                        org.springframework.data.mongodb.core.mapping.Document.class, clazz),
                clazz -> {
                    // 获取
                    for (Field field : ReflectUtil.getFields(clazz)) {
                        if (field.getAnnotation(org.springframework.data.mongodb.core.mapping.Field.class) != null
                                && DictionaryItem.class.isAssignableFrom(field.getType())) {
                            // 所有字典类字段

                            propertyValueConverterRegistrar.registerConverter(clazz, field.getName(), new EnumToStringPropertyValueConverter());
                        }
                    }
                    //logger.info("register " + clazz.getName() + " fieldName.");
                }));

    }
}
