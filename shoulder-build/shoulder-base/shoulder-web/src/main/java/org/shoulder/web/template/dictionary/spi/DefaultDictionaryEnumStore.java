package org.shoulder.web.template.dictionary.spi;

import org.shoulder.core.exception.BaseRuntimeException;
import org.shoulder.validate.exception.ParamErrorCodeEnum;
import org.shoulder.web.template.dictionary.model.DictionaryEnum;

import javax.annotation.Nonnull;
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

    private final ConcurrentMap<String, Class<? extends DictionaryEnum>> repo = new ConcurrentHashMap<>();

    /**
     * 是否忽略字典类型名称大小写
     */
    private final boolean ignoreCase;

    public DefaultDictionaryEnumStore(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }

    @Override
    public <ID, ENUM extends Enum<? extends DictionaryEnum<ENUM, ID>>> void register(@Nonnull Class<? extends DictionaryEnum<ENUM, ID>> dictionaryEnum, @Nonnull String dictionaryType) {
        repo.put(processDictionaryTypeName(dictionaryType), dictionaryEnum);
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
    public List<DictionaryEnum> listAllAsDictionaryEnum(String enumClassType) {
        Class<? extends DictionaryEnum> dictionaryEnumClass = repo.get(processDictionaryTypeName(enumClassType));
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
    public Collection<Class<? extends DictionaryEnum>> listAllTypes() {
        return repo.values();
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
