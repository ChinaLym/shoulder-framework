package org.shoulder.web.template.dictionary.model;

/**
 * 简化字典型枚举-不使用 id，使用 name() 作为标识
 *
 * @author lym
 */
public interface NameAsIdDictionaryItemEnum<E extends Enum<? extends NameAsIdDictionaryItemEnum<E>>> extends DictionaryItemEnum<E, String> {

    /**
     * 获取该字典项的 id
     *
     * @return id
     */
    @Override
    default String getItemId() {
        return getName();
    }

}