package org.shoulder.core.dictionary.model;

/**
 * 字典类型
 * todo 【功能】基于数据库的、基于 RPC 调用的、带缓存的、混合的；放到 ext 模块
 * 泛型是为了让持久化存储时对象实现该接口时可选择 int/string、枚举无需该泛型
 *
 * @author lym
 */
public interface DictionaryType {

    /**
     * 字典唯一标记
     *
     * @return id
     */
    String getCode();

    /**
     * 获取该字典项的展示名称（仅用于字典管理后台，若无该需求则忽略该字段）
     *
     * @return 展示名称
     */
    String getDisplayName();

    /**
     * 获取该字典项的展示名称（仅用于字典管理后台，若无该需求则忽略该字段）
     *
     * @return 展示名称
     */
    Integer getDisplayOrder();

    /**
     * 能否修改
     *
     * @return 枚举不能修改，db 存储一般可修改
     */
    boolean modifyAble();

    /*
     * 将 id 转为 Enum
     *
     * @param enumClass 枚举类
     * @param id        id
     * @return Enum
     */
    //   Dictionary fromId(String dictionaryTypeName, ID id)
    //   Dictionary fromName(String dictionaryTypeName, String name)
    // List<Dictionary> values(String dictionaryTypeName)


}
