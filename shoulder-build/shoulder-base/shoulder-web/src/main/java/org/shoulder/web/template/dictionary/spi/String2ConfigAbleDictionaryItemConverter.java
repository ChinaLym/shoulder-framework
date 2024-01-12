package org.shoulder.web.template.dictionary.spi;

import org.shoulder.web.template.dictionary.model.ConfigAbleDictionaryItem;

/**
 * str 和 动态字典转换
 */
public interface String2ConfigAbleDictionaryItemConverter {

    /**
     * 转成动态配置字典项
     *
     * @param dictionaryType 字典标记
     * @param code dictionaryType 对应字典下的 code 值
     * @return DictionaryItem
     */
    ConfigAbleDictionaryItem convertToConfigAbleDictionaryItem(String dictionaryType, String code);

}
