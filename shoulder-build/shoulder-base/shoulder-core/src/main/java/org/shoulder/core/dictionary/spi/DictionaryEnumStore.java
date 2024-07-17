package org.shoulder.core.dictionary.spi;

import jakarta.annotation.Nonnull;
import org.shoulder.core.dictionary.model.DictionaryItemEnum;

import java.util.Collection;
import java.util.List;

/**
 * 枚举解析存储
 *
 * @author lym
 */
public interface DictionaryEnumStore {

    /**
     * 注册一个枚举类
     *
     * @param dictionaryEnum 枚举类
     * @param <ID>           枚举标识类型
     * @param <ENUM>         枚举
     */
    default <ID, ENUM extends Enum<? extends DictionaryItemEnum<ENUM, ID>>> void register(@Nonnull Class<? extends Enum<? extends DictionaryItemEnum<?, ?>>> dictionaryEnum) {
        register(dictionaryEnum, mapToStorageKey(dictionaryEnum));
    }


    /**
     * 注册一个枚举类
     *
     * @param <ID>           枚举标识类型
     * @param <ENUM>         枚举
     * @param dictionaryEnum 枚举类
     * @param dictionaryType 枚举类型名称
     */
    <ID, ENUM extends Enum<? extends DictionaryItemEnum<ENUM, ID>>> void register(@Nonnull Class<? extends Enum<? extends DictionaryItemEnum<?, ?>>> dictionaryEnum, @Nonnull String dictionaryType);


    /**
     * 列出所有 enumClass 下的字典项
     *
     * @param <ID>      枚举标识类型
     * @param <ENUM>    枚举
     * @param enumClass 枚举类
     * @return 所有符合条件的枚举
     */
    @SuppressWarnings("unchecked")
    @Nonnull
    default <ID, ENUM extends Enum<? extends DictionaryItemEnum<ENUM, ID>>> List<DictionaryItemEnum<ENUM, ID>> list(@Nonnull Class<? extends DictionaryItemEnum<ENUM, ID>> enumClass) {
        return list(mapToStorageKey((Class<? extends Enum<? extends DictionaryItemEnum<?, ?>>>) enumClass));
    }

    /**
     * 列出所有 enumClassType 下的字典项
     *
     * @param enumClassType 枚举类名/别名
     * @param <ID>          枚举标识类型
     * @param <ENUM>        枚举
     * @return 所有符合条件的枚举
     */
    @Nonnull
    <ID, ENUM extends Enum<? extends DictionaryItemEnum<ENUM, ID>>> List<DictionaryItemEnum<ENUM, ID>> list(@Nonnull String enumClassType);

    /**
     * 列出所有 enumClassType 下的字典项 （返回值去掉泛型）
     *
     * @param enumClassType 枚举类名/别名
     * @return 所有符合条件的枚举
     */
    @Nonnull
    @SuppressWarnings("rawtypes")
    List<Enum<? extends DictionaryItemEnum>> listAllAsDictionaryEnum(String enumClassType);

    /**
     * 列出所有支持的枚举类名
     *
     * @return 支持的字典名称列表
     */
    @Nonnull
    Collection<String> listAllTypeNames();

    /**
     * 列出所有支持的枚举类名
     *
     * @return 支持的字典类型列表
     */
    @Nonnull
    @SuppressWarnings("rawtypes")
    Collection<Class<? extends Enum<? extends DictionaryItemEnum>>> listAllTypes();

    @SuppressWarnings("unchecked")
    default boolean contains(Class<?> enumClass) {
        return contains(mapToStorageKey((Class<? extends Enum<? extends DictionaryItemEnum<?, ?>>>) enumClass));
    }

    boolean contains(String dictionaryType);

    @SuppressWarnings("unchecked")
    default void remove(Class<?> enumClass) {
        remove(mapToStorageKey((Class<? extends Enum<? extends DictionaryItemEnum<?, ?>>>) enumClass));
    }

    @SuppressWarnings("rawtypes")
    Class<? extends Enum<? extends DictionaryItemEnum>> remove(String dictionaryType);

    @SuppressWarnings("rawtypes")
    Class<? extends Enum<? extends DictionaryItemEnum>> getActuallyType(String dictionaryType);

    @Nonnull
    default <ID, ENUM extends Enum<? extends DictionaryItemEnum<ENUM, ID>>> String mapToStorageKey(@Nonnull Class<? extends Enum<? extends DictionaryItemEnum<?, ?>>> dictionaryEnum) {
        return dictionaryEnum.getSimpleName();
    }
}
