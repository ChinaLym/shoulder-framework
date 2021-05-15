package org.shoulder.web.template.dictionary.model;

/**
 * 字典型枚举
 *
 * @author lym
 */
public interface DictionaryEnum<E extends Enum<? extends DictionaryEnum<E, IDENTIFY>>, IDENTIFY> extends DictionaryItem<IDENTIFY> {

    /**
     * 将 id 转为 Enum
     *
     * @param enumClass 枚举类
     * @param id        id
     * @return Enum
     */
    static <ID, ENUM extends Enum<? extends DictionaryEnum<ENUM, ID>>> ENUM fromId(Class<ENUM> enumClass, ID id) {
        return fromId(enumClass.getEnumConstants(), id);
    }

    /**
     * 将 id 转为 Enum
     *
     * @param enumValues 需要查找的枚举值列表
     * @param id         id
     * @return Enum
     */
    @SuppressWarnings("unchecked")
    static <ID, ENUM extends Enum<? extends DictionaryEnum<ENUM, ID>>> ENUM fromId(ENUM[] enumValues, ID id) {
        for (ENUM e : enumValues) {
            if (((DictionaryEnum<ENUM, ID>) e).getItemId().equals(id)) {
                return e;
            }
        }
        return onMissMatch((Class<ENUM>) enumValues.getClass().getComponentType(), id);
    }

    /**
     * 将 name 转为 Enum
     *
     * @param enumClass 枚举类
     * @param name      name
     * @return Enum
     */
    static <ID, ENUM extends Enum<? extends DictionaryEnum<ENUM, ID>>> ENUM fromName(Class<ENUM> enumClass, String name) {
        return fromName(enumClass.getEnumConstants(), name);
    }

    /**
     * 根据 name 转为 enum （忽略大小写）
     *
     * @param enumValues 需要查找的枚举值列表
     * @param name       枚举项名称
     * @param <ID>       枚举标识类型
     * @param <ENUM>     枚举类
     * @return 枚举项
     */
    @SuppressWarnings("unchecked")
    static <ID, ENUM extends Enum<? extends DictionaryEnum<ENUM, ID>>> ENUM fromName(ENUM[] enumValues, String name) {
        for (ENUM e : enumValues) {
            if (((DictionaryEnum<ENUM, ID>) e).getName().equalsIgnoreCase(name)) {
                return e;
            }
        }
        return onMissMatch((Class<ENUM>) enumValues.getClass().getComponentType(), name);
    }

    /**
     * 没找到对应的 枚举
     *
     * @param enumClass 枚举类
     * @param source    id / name
     * @return 默认抛异常，可以改为返回默认
     */
    static <ID, ENUM extends Enum<? extends DictionaryEnum<ENUM, ID>>> ENUM onMissMatch(Class<ENUM> enumClass, Object source) {
        throw new IllegalArgumentException(
                "can't convert '" + source + "' to " + enumClass.getSimpleName());
    }

    /**
     * 获取目标枚举的所有枚举值
     *
     * @param enumClass 枚举类
     * @param <ID>      枚举 id
     * @param <ENUM>    枚举
     * @return 枚举值[]
     */
    static <ID, ENUM extends Enum<? extends DictionaryEnum<ENUM, ID>>> ENUM[] values(Class<ENUM> enumClass) {
        return enumClass.getEnumConstants();
    }

}