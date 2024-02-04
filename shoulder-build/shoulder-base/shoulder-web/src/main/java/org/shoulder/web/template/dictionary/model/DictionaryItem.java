package org.shoulder.web.template.dictionary.model;

import java.util.Map;

/**
 * 字典项
 * todo 要支持多级结构，如
 * 办公耗材：
 * - 笔
 *  - 铅笔
 *      - HB 铅笔
 *      - B4 铅笔
 *  - 圆珠笔
 * - 纸
 *  - 打印纸
 *      - A4纸
 *      - A3纸
 *  - 卡纸
 *      - 绿色卡纸
 *      - 黑色卡纸
 *  - 宣纸
 *      - xx
 *
 * @author lym
 */
public interface DictionaryItem<IDENTIFY> {

    /**
     * 获取该字典项的 id
     * 建议在 持久化，接口出口，用该字段作为字典项标志
     *
     * @return id
     */
    IDENTIFY getItemId();

    /**
     * 获取该字典项的名称 / 多语言 key
     * 通常用于java代码辅助获取枚举名、后台管理时透出方便和代码对应
     *
     * @return source
     */
    String getName();

    /**
     * 获取该字典项的展示名称，便于前端使用，小项目中减少前端工作量
     *
     * @return 展示名称
     */
    default String getDisplayName() {
        return getName();
    }

    /**
     * 备注、说明，便于前端使用，小项目中减少前端工作量
     */
    default String getDescription() {
        return null;
    }

    /**
     * 父节点
     */
    //default String getParent() {
    //    return null;
    //}

    /**
     * 获取排序序号
     *
     * @return 排序号
     */
    Integer getDisplayOrder();

    /**
     * 获取对应的枚举类型，通常是类名等 【非必须实现】
     *
     * @return 枚举类型
     */
    default String getDictionaryType() {
        return getClass().getSimpleName();
    }

    /**
     * 某一个具体字典项是否满足某过滤条件（默认都满足、不过滤）
     *
     * @param key   k
     * @param value v
     * @return 是否满足
     */
    default boolean matchCondition(String key, String value) {
        return true;
    }

    /**
     * 多个条件，默认都满足才行
     */
    default boolean matchAllCondition(Map<String, String> conditionMap) {
        return conditionMap.entrySet().stream()
                .allMatch(e -> matchCondition(e.getKey(), e.getValue()));
    }

}
