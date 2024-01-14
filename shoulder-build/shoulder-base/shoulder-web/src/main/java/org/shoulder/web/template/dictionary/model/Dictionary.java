package org.shoulder.web.template.dictionary.model;

/**
 * 字典型枚举，枚举类选择实现该类
 * todo 【功能】基于数据库的、基于 RPC 调用的、带缓存的、混合的；放到 ext 模块
 * 泛型是为了让持久化存储时对象实现该接口时可选择 int/string、枚举无需该泛型
 *
 * @author lym
 */
public interface Dictionary {

    /**
     * 字典唯一标记
     *
     * @return id
     */
    String getDictionaryCode();

    String getName();

    /**
     * 获取该字典项的展示名称（仅用于字典管理后台，若无该需求则忽略该字段）
     *
     * @return 展示名称
     */
    default String getDisplayName() {
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
