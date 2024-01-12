package org.shoulder.web.template.dictionary.model;

/**
 * 字典型枚举
 * todo 【功能】基于数据库的、基于 RPC 调用的、带缓存的、混合的；放到 ext 模块
 *
 * @author lym
 */
public interface Dictionary<IDENTIFY> {

    /**
     * 获取该字典项的 id / code
     *
     * @return id
     */
    IDENTIFY getId();

    /**
     * 获取该字典项的 名称
     *
     * @return 名称
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
     * 获取对应的枚举类型，通常是类名等
     *
     * @return 枚举类型
     */
    default String getDictionaryType() {
        return getName();
    }

    /**
     * 是否字典项个数固定
     *
     * @return 是否字典项个数固定；默认不固定
     */
    default boolean hasFixItems() {
        return false;
    }

    /*
     * 将 id 转为 Enum
     *
     * @param enumClass 枚举类
     * @param id        id
     * @return Enum
     */
    //  <ID> Dictionary<ID> fromId(String dictionaryTypeName, ID id)
    //  <ID> Dictionary<ID> fromName(String dictionaryTypeName, String name)
    // List<Dictionary> values(String dictionaryTypeName)


}
