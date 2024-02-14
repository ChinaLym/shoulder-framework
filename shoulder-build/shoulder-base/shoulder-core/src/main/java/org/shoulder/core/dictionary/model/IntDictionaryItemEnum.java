package org.shoulder.core.dictionary.model;

/**
 * 简化字典型枚举-int 类型 id
 *
 * @author lym
 */
public interface IntDictionaryItemEnum<E extends Enum<? extends IntDictionaryItemEnum<E>>> extends DictionaryItemEnum<E, Integer> {

    /**
     * 获取该字典项的 id，默认使用字典的 ordinal();
     *
     * @return id
     */
    @Override
    Integer getItemId();

}
