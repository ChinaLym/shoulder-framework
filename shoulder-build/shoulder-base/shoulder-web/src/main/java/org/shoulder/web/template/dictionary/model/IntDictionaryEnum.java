package org.shoulder.web.template.dictionary.model;

/**
 * 简化字典型枚举-int 类型 id
 *
 * @author lym
 */
public interface IntDictionaryEnum<E extends Enum<? extends IntDictionaryEnum<E>>> extends DictionaryEnum<E, Integer> {

    /**
     * 获取该字典项的 id，默认使用字典的 ordinal();
     *
     * @return id
     */
    @Override
    Integer getItemId();

}