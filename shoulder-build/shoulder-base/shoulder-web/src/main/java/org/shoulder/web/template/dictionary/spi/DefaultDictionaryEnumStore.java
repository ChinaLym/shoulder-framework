package org.shoulder.web.template.dictionary.spi;

import jakarta.annotation.Nonnull;
import org.shoulder.core.exception.BaseRuntimeException;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.util.AssertUtils;
import org.shoulder.validate.exception.ParamErrorCodeEnum;
import org.shoulder.web.template.dictionary.model.DictionaryEnum;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * repo
 *
 * @author lym
 */
@SuppressWarnings("unchecked, rawtypes")
public class DefaultDictionaryEnumStore implements DictionaryEnumStore {

    private final ConcurrentMap<String, Class<? extends Enum<? extends DictionaryEnum>>> repo = new ConcurrentHashMap<>();

    /**
     * 是否忽略字典类型名称大小写
     */
    private final boolean ignoreCase;

    public DefaultDictionaryEnumStore(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }

    @Override
    public <ID, ENUM extends Enum<? extends DictionaryEnum<ENUM, ID>>> void register(@Nonnull Class<? extends Enum<? extends DictionaryEnum<?, ?>>> dictionaryEnum, @Nonnull String dictionaryType) {
        AssertUtils.notNull(dictionaryEnum, ParamErrorCodeEnum.PARAM_ILLEGAL);
        Class<? extends Enum<? extends DictionaryEnum>> oldValue =
            repo.put(processDictionaryTypeName(dictionaryType), dictionaryEnum);
        AssertUtils.isTrue(oldValue == null || oldValue == dictionaryEnum, CommonErrorCodeEnum.CODING, "not support repeat name of enum.");
    }

    @Nonnull
    @Override
    public <ID, ENUM extends Enum<? extends DictionaryEnum<ENUM, ID>>> List<DictionaryEnum<ENUM, ID>> list(@Nonnull String enumClassType) {
        Class<? extends DictionaryEnum<ENUM, ID>> dictionaryEnumClass = (Class<? extends DictionaryEnum<ENUM, ID>>) repo.get(processDictionaryTypeName(enumClassType));
        if (dictionaryEnumClass == null) {
            throw createDictionaryTypeNotFoundException(enumClassType);
        }
        return Arrays.stream(dictionaryEnumClass.getEnumConstants()).collect(Collectors.toList());
    }

    @Nonnull
    @Override
    public List<Enum<? extends DictionaryEnum>> listAllAsDictionaryEnum(String enumClassType) {
        Class<? extends Enum<? extends DictionaryEnum>> dictionaryEnumClass = repo.get(processDictionaryTypeName(enumClassType));
        if (dictionaryEnumClass == null) {
            throw createDictionaryTypeNotFoundException(enumClassType);
        }
        return Arrays.stream(dictionaryEnumClass.getEnumConstants()).collect(Collectors.toList());
    }

    @Nonnull
    @Override
    public Collection<String> listAllTypeNames() {
        return repo.keySet();
    }

    @Nonnull
    @Override
    public Collection<Class<? extends Enum<? extends DictionaryEnum>>> listAllTypes() {
        return repo.values();
    }

    @Override
    public boolean contains(String dictionaryType) {
        return repo.containsKey(processDictionaryTypeName(dictionaryType));
    }

    @Override
    public Class<? extends Enum<? extends DictionaryEnum>> getActuallyType(String dictionaryType) {
        return repo.get(processDictionaryTypeName(dictionaryType));
    }

    protected BaseRuntimeException createDictionaryTypeNotFoundException(String dictionaryType) {
        return new BaseRuntimeException(ParamErrorCodeEnum.PARAM_ILLEGAL, "The dictionary type('" + dictionaryType + "') not exist!");
    }

    protected String processDictionaryTypeName(String dictionaryTypeName) {
        return ignoreCase ? dictionaryTypeName.toLowerCase(Locale.ROOT) : dictionaryTypeName;
    }

    public boolean isIgnoreCase() {
        return ignoreCase;
    }

}
