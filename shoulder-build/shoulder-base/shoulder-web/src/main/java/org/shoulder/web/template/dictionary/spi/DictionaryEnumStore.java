package org.shoulder.web.template.dictionary.spi;

import jakarta.annotation.Nonnull;
import org.shoulder.web.template.dictionary.model.DictionaryEnum;

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
    default <ID, ENUM extends Enum<? extends DictionaryEnum<ENUM, ID>>> void register(@Nonnull Class<? extends Enum<? extends DictionaryEnum<?, ?>>> dictionaryEnum) {
        register(dictionaryEnum, dictionaryEnum.getSimpleName());
    }


    /**
     * 注册一个枚举类
     *
     * @param <ID>           枚举标识类型
     * @param <ENUM>         枚举
     * @param dictionaryEnum 枚举类
     * @param dictionaryType 枚举类型名称
     */
    <ID, ENUM extends Enum<? extends DictionaryEnum<ENUM, ID>>> void register(@Nonnull Class<? extends Enum<? extends DictionaryEnum<?, ?>>> dictionaryEnum, @Nonnull String dictionaryType);


    /**
     * 列出所有 enumClass 下的字典项
     *
     * @param <ID>      枚举标识类型
     * @param <ENUM>    枚举
     * @param enumClass 枚举类
     * @return 所有符合条件的枚举
     */
    @Nonnull
    default <ID, ENUM extends Enum<? extends DictionaryEnum<ENUM, ID>>> List<DictionaryEnum<ENUM, ID>> list(@Nonnull Class<? extends DictionaryEnum<ENUM, ID>> enumClass) {
        return list(enumClass.getTypeName());
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
    <ID, ENUM extends Enum<? extends DictionaryEnum<ENUM, ID>>> List<DictionaryEnum<ENUM, ID>> list(@Nonnull String enumClassType);

    /**
     * 列出所有 enumClassType 下的字典项 （返回值去掉泛型）
     *
     * @param enumClassType 枚举类名/别名
     * @return 所有符合条件的枚举
     */
    @Nonnull
    @SuppressWarnings("rawtypes")
    List<Enum<? extends DictionaryEnum>> listAllAsDictionaryEnum(String enumClassType);

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
    Collection<Class<? extends Enum<? extends DictionaryEnum>>> listAllTypes();

    boolean contains(Class<?> enumClass);
}
