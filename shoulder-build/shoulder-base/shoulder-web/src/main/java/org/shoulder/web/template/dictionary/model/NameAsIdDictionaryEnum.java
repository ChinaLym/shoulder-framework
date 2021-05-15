package org.shoulder.web.template.dictionary.model;

/**
 * 简化字典型枚举-不使用 id，使用 name() 作为标识
 *
 * @author lym
 */
public interface NameAsIdDictionaryEnum<E extends Enum<? extends NameAsIdDictionaryEnum<E>>> extends DictionaryEnum<E, String> {

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