package com.example.demo1.enums;

import org.shoulder.web.template.dictionary.model.NameAsIdDictionaryEnum;

/**
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
