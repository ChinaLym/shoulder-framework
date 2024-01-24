package org.shoulder.web.template.dictionary.convert;

import jakarta.persistence.AttributeConverter;
import org.shoulder.web.template.dictionary.model.DictionaryItem;

/**
 * 字典项转换
 * 注意该类不要直接时候
 *
 * @author lym
 */
public abstract class BaseDictionaryItem2StrJpaConverter<D extends DictionaryItem> implements AttributeConverter<DictionaryItem, String> {

    protected final Class<? extends DictionaryItem> actuallyType;

    public BaseDictionaryItem2StrJpaConverter(Class<? extends DictionaryItem> actuallyType) {
        this.actuallyType = actuallyType;
    }

    @Override
    public String convertToDatabaseColumn(DictionaryItem dictionaryItem) {
        return DictionaryItemConversions.toStr(dictionaryItem);
    }

    @Override
    public DictionaryItem convertToEntityAttribute(String s) {
        return DictionaryItemConversions.toItem(s, actuallyType);
    }
}
