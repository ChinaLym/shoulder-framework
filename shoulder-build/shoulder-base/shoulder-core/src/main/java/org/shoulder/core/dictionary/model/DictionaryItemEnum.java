package org.shoulder.core.dictionary.model;

import org.springframework.core.GenericTypeResolver;

import java.util.Optional;
import java.util.function.BiFunction;

/**
 * 字典型枚举
 *
 * 1. 为什么有第一个泛型参数？
 * 自引用，约束子类必须是枚举，见到该接口就可以放心的转化为枚举调用枚举特性方法；
 * 编译安全检查，借助java编译器优势做代码类型检查，避免泛型滥用，类型安全转换；
 * GPT 也认为该参数确实非常有必要，由于泛型的存在，避免了使用者经常强制类型转换，复杂性由框架承担，而非业务代码。
 *
 * 2. 不用更强的泛型校验？如 E extends Enum<? extends DictionaryItemEnum<E, IDENTIFY>>
 * 是为了其他工具类用 Class<?> 调静态方法可以编译通过，做到代码复用，故采用了松泛型编译校验写法，
 * 是复用和简化的权衡。
 *
 * 不建议用户枚举直接继承该类，采用 {@link NameAsIdDictionaryItemEnum}{@link IntDictionaryItemEnum}
 *
 * @author lym
 */
public interface DictionaryItemEnum<E extends Enum<? extends DictionaryItemEnum<?, IDENTIFY>>, IDENTIFY> extends
                                                                                                         DictionaryItem<IDENTIFY> {

    /**
     * 将 id 转为 Enum
     *
     * @param enumClass 枚举类
     * @param id        id
     * @return Enum
     */
    static <ID, ENUM extends Enum<? extends DictionaryItemEnum<?, ID>>> ENUM fromId(
            Class<? extends Enum<? extends DictionaryItemEnum<?, ID>>> enumClass, ID id) {
        return decideActualEnum(enumClass.getEnumConstants(), id, compareWithId(), DictionaryItemEnum::onMissMatch);
    }

    /**
     * 将 id 转为 Enum
     *
     * @param enumClass 枚举类
     * @param id        id
     * @param defaultVal 不存在时默认值
     * @return Enum
     */
    static <ID, ENUM extends Enum<? extends DictionaryItemEnum<?, ID>>> ENUM fromId(
            Class<? extends Enum<? extends DictionaryItemEnum<?, ID>>> enumClass, ID id, ENUM defaultVal) {
        return decideActualEnum(enumClass.getEnumConstants(), id, compareWithId(), (enumClazz, val) -> defaultVal );
    }

    static <ID, ENUM extends Enum<? extends DictionaryItemEnum<?, ID>>> ENUM fromName(
            Class<? extends Enum<? extends DictionaryItemEnum<?, ID>>> enumClass, String name) {
        return decideActualEnum(enumClass.getEnumConstants(), name, compareWithName(), DictionaryItemEnum::onMissMatch);
    }

    static <ID, ENUM extends Enum<? extends DictionaryItemEnum<?, ID>>> ENUM fromName(
            Class<? extends Enum<? extends DictionaryItemEnum<?, ID>>> enumClass, String name, ENUM defaultVal) {
        return decideActualEnum(enumClass.getEnumConstants(), name, compareWithName(), (enumClazz, val) -> defaultVal );
    }

    static <ID, ENUM extends Enum<? extends DictionaryItemEnum<?, ID>>> ENUM fromOrder(
            Class<? extends Enum<? extends DictionaryItemEnum<?, ID>>> enumClass, int order) {
        return decideActualEnum(enumClass.getEnumConstants(), order, compareWithEnumOrdinal(), DictionaryItemEnum::onMissMatch);
    }

    /**
     * 将 name 转为 Enum
     *
     * @param enumClass 枚举类
     * @param name      name
     * @return Enum
     */
    static <ID, ENUM extends Enum<? extends DictionaryItemEnum<?, ID>>> ENUM fromEnumCodingName(
            Class<? extends Enum<? extends DictionaryItemEnum<?, ID>>> enumClass, String name) {
        return decideActualEnum(enumClass.getEnumConstants(), name, compareWithEnumCodingName(), DictionaryItemEnum::onMissMatch);
    }

    static BiFunction<Enum<? extends DictionaryItemEnum<?, ?>>, Object, Boolean> compareWithId() {
        return (e, id) -> ((DictionaryItemEnum<?, ?>) e).getItemId().equals(id);
    }

    static BiFunction<Enum<? extends DictionaryItemEnum<?, ?>>, Object, Boolean> compareWithName() {
        return (e, str) -> ((DictionaryItemEnum<?, ?>) e).getName().equals(str);
    }

    static BiFunction<Enum<? extends DictionaryItemEnum<?, ?>>, Object, Boolean> compareWithEnumOrdinal() {
        return (e, orderInt) -> orderInt.equals(e.ordinal());
    }

    static BiFunction<Enum<? extends DictionaryItemEnum<?, ?>>, Object, Boolean> compareWithEnumCodingName() {
        return (e, str) -> e.name().equals(str);
    }

    /**
     * 将 id 转为 Enum
     *
     * @param enumValues 需要查找的枚举值列表
     * @param id         id
     * @return Enum
     */
    @SuppressWarnings("unchecked")
    static <ID, ENUM extends Enum<? extends DictionaryItemEnum<?, ID>>> ENUM decideActualEnum(
            Enum<? extends DictionaryItemEnum<?, ID>>[] enumValues,
        Object input,
            BiFunction<Enum<? extends DictionaryItemEnum<?, ?>>, Object, Boolean> compare,
        BiFunction<Class<ENUM>, Object, ENUM> onMissMatch) {
        for (Enum<? extends DictionaryItemEnum<?, ID>> e : enumValues) {
            if (compare.apply(e, input)) {
                return (ENUM) e;
            }
        }
        return onMissMatch.apply((Class<ENUM>) enumValues.getClass().getComponentType(), input);
    }

    /**
     * 没找到对应的 枚举
     *
     * @param enumClass 枚举类
     * @param source    id / name
     * @return 默认抛异常，可以改为返回默认
     */
    static <ID, ENUM extends Enum<? extends DictionaryItemEnum<?, ID>>> ENUM onMissMatch(Class<ENUM> enumClass, Object source) {
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
    static <ID, ENUM extends Enum<? extends DictionaryItemEnum<?, ID>>> ENUM[] values(Class<ENUM> enumClass) {
        return enumClass.getEnumConstants();
    }

    /**
     * 解析枚举类 itemId 类型
     *
     * @param dictionaryItemClass 枚举类
     * @return integer / string
     */
    static Class<?> resovleEnumItemIdClass(Class<?> dictionaryItemClass) {
        // 第二个泛型是 itemId 类型
        return Optional.ofNullable(GenericTypeResolver.resolveTypeArguments(dictionaryItemClass, DictionaryItemEnum.class)).orElseThrow()[1];
    }

    /**
     * 获取当前枚举类 itemId 类型
     *
     * @param enumClass 枚举类
     * @return integer / string
     */
    default Class<?> getEnumItemIdClass() {
        // 第二个泛型是 itemId 类型
        return resovleEnumItemIdClass(getClass());
    }

    @Override
    default String getName() {
        return ((Enum<?>) this).name();
    }

    default String getDictionaryType() {
        return getClass().getSimpleName();
    }

    default Integer getDisplayOrder() {
        return ((Enum<?>) this).ordinal();
    }

}
