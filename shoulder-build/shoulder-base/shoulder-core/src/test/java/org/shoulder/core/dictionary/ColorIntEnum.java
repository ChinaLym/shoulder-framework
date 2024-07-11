package org.shoulder.core.dictionary;

import org.shoulder.core.dictionary.model.IntDictionaryItemEnum;

/**
 * @author lym
 */
public enum ColorIntEnum implements IntDictionaryItemEnum<ColorIntEnum> {

    WHITE(256),
    BLANK(0),
    GRAY(128),
    ;

    /**
     * 色彩亮度
     */
    private final int color;

    ColorIntEnum(int color) {
        this.color = color;
    }

    @Override public Integer getItemId() {
        return color;
    }

    @Override
    public boolean matchCondition(String key, String value) {
        return "color".equals(key) && name().equalsIgnoreCase(value);
    }

}
