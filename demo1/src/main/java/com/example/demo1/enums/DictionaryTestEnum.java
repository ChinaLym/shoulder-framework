package com.example.demo1.enums;

import com.example.demo1.config.DictionaryEnumConfiguration;
import org.shoulder.web.template.dictionary.model.NameAsIdDictionaryEnum;

/**
 * 让枚举实现 NameAsIdDictionaryEnum 接口，前段就可以调接口查询有哪些枚举啦
 * @see DictionaryEnumConfiguration#dictionaryEnumRepositoryCustomizer()
 *
 * @author lym
 */
public enum DictionaryTestEnum implements NameAsIdDictionaryEnum<DictionaryTestEnum> {

    /**
     *
     */
    BLUE,

    YELLOW,

    GREEN,

    RED,
    ;

    @Override
    public String getName() {
        return name();
    }

    @Override
    public long getDisplayOrder() {
        return ordinal();
    }

}
