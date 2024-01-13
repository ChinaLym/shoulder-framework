package org.shoulder.web.template.dictionary.model;

/**
 * 字典型枚举
 *
 * 不用更强的泛型校验：E extends Enum<? extends DictionaryEnum<E, IDENTIFY>>
 *     是为了其他工具类用 Class<?> 调静态方法可以编译通过，做到代码复用，故采用了松泛型编译校验写法，
 *     是复用和简化的权衡
 * 不建议用户枚举直接继承该类，采用 {@link NameAsIdDictionaryEnum}{@link IntDictionaryEnum}
 * todo 是否需要用户感知泛型？？
 *
 * @author lym
 */
public interface DictionaryEnum<E extends Enum<? extends DictionaryEnum<?, IDENTIFY>>, IDENTIFY> extends DictionaryItem<IDENTIFY> {

    /**
     * 将 id 转为 Enum
     *
     * @param enumClass 枚举类
     * @param id        id
     * @return Enum
     */
    static <ID, ENUM extends Enum<? extends DictionaryEnum<?, ID>>> ENUM fromId(Class<? extends Enum<? extends DictionaryEnum<?, ID>>> enumClass, ID id) {
        return fromIdWithEnumConstants(enumClass.getEnumConstants(), id);
    }

    /**
     * 将 id 转为 Enum
     *
     * @param enumValues 需要查找的枚举值列表
     * @param id         id
     * @return Enum
     */
    @SuppressWarnings("unchecked")
    static <ID, ENUM extends Enum<? extends DictionaryEnum<?, ID>>> ENUM fromIdWithEnumConstants(Enum<? extends DictionaryEnum<?, ID>>[] enumValues, ID id) {
        for (Enum<? extends DictionaryEnum<?, ID>> e : enumValues) {
            if (((DictionaryEnum<ENUM, ID>) e).getItemId().equals(id)) {
                return (ENUM) e;
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
    static <ID, ENUM extends Enum<? extends DictionaryEnum<?, ID>>> ENUM fromName(Class<? extends Enum<? extends DictionaryEnum<?, ID>>> enumClass, String name) {
        return fromNameWithEnumConstants(enumClass.getEnumConstants(), name);
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
    static <ID, ENUM extends Enum<? extends DictionaryEnum<?, ID>>> ENUM fromNameWithEnumConstants(Enum<? extends DictionaryEnum<?, ID>>[] enumValues, String name) {
        for (Enum<? extends DictionaryEnum<?, ID>> e : enumValues) {
            if (((DictionaryEnum<ENUM, ID>) e).getName().equalsIgnoreCase(name)) {
                return (ENUM) e;
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
    static <ID, ENUM extends Enum<? extends DictionaryEnum<?, ID>>> ENUM onMissMatch(Class<ENUM> enumClass, Object source) {
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
    static <ID, ENUM extends Enum<? extends DictionaryEnum<?, ID>>> ENUM[] values(Class<ENUM> enumClass) {
        return enumClass.getEnumConstants();
    }

    @Override
    default String getName() {
        return ((Enum<?>) this).name();
    }

    default Integer getDisplayOrder() {
        return ((Enum<?>) this).ordinal();
    }


}
