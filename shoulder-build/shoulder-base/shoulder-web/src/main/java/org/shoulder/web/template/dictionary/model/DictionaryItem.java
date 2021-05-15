package org.shoulder.web.template.dictionary.model;

/**
 * 字典项
 *
 * @author lym
 */
public interface DictionaryItem<IDENTIFY> {

    /**
     * 获取该字典项的 id
     *
     * @return id
     */
    IDENTIFY getItemId();

    /**
     * 获取该字典项的名称 / 多语言 key
     *
     * @return source
     */
    String getName();

    /**
     * 获取该字典项的展示名称
     *
     * @return 展示名称
     */
    default String getDisplayName() {
        return getName();
    }

    /**
     * 获取排序序号
     *
     * @return 排序号
     */
    long getDisplayOrder();

    /**
     * 获取对应的枚举类型，通常是类名等 【非必须实现】
     *
     * @return 枚举类型
     */
    default String getDictionaryType() {
        return getClass().getSimpleName();
    }

}
