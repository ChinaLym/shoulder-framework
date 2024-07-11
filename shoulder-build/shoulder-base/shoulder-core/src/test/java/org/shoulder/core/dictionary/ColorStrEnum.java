package org.shoulder.core.dictionary;

import org.shoulder.core.dictionary.model.NameAsIdDictionaryItemEnum;

/**
 * @author lym
 */
public enum ColorStrEnum implements NameAsIdDictionaryItemEnum<ColorStrEnum> {

    WHITE,
    BLANK,
    RED,
    BLUE,
    YELLOW,
    ;

    @Override
    public boolean matchCondition(String key, String value) {
        return "color".equals(key) && name().equalsIgnoreCase(value);
    }
}
