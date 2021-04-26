package org.shoulder.ext.config.domain;

import org.shoulder.ext.config.domain.enums.ConfigErrorCodeEnum;
import org.shoulder.ext.config.domain.ex.ConfigException;
import org.shoulder.ext.config.domain.model.ConfigFieldInfo;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lym
 */
public interface ConfigType {

    ConcurrentHashMap<String, ConfigType> NAME_VALUES = new ConcurrentHashMap<>();

    ConcurrentHashMap<Class<?>, ConfigType> TYPE_VALUES = new ConcurrentHashMap<>();

    static Collection<ConfigType> values() {
        return NAME_VALUES.values();
    }


    /**
     * configName
     *
     * @return configName
     */
    String getConfigName();

    /**
     * description
     *
     * @return description
     */
    String getDescription();

    /**
     * configClazz
     *
     * @return configClazz
     */
    Class<?> getClazz();

    /**
     * configFieldInfoList
     *
     * @return configFieldInfoList
     */
    List<ConfigFieldInfo> getFieldInfoList();

    /**
     * indexFieldInfoList
     *
     * @return indexFieldInfoList
     */
    List<ConfigFieldInfo> getIndexFieldInfoList();

    static ConfigType getByName(String name) {
        return NAME_VALUES.computeIfAbsent(name, k -> {
            throw new ConfigException(ConfigErrorCodeEnum.CONFIG_TYPE_NOT_EXISTS);
        });
    }

    @Nonnull
    static ConfigType getByType(Class<?> clazz) {
        return TYPE_VALUES.getOrDefault(clazz, register(clazz, "auto generate"));
    }

    /**
     * 注册一个配置类
     *
     * @param configClass clazz
     * @param description desc
     * @return configType
     */
    static ConfigType register(@Nonnull Class<?> configClass, String description) {
        ConfigType configType;
        if ((configType = TYPE_VALUES.get(configClass)) != null) {
            return configType;
        }
        synchronized (TYPE_VALUES) {
            if ((configType = TYPE_VALUES.get(configClass)) != null) {
                return configType;
            }
            configType = new ConfigTypeInfo(configClass, description);
            TYPE_VALUES.put(configClass, configType);
            NAME_VALUES.put(configClass.getSimpleName(), configType);
            return configType;
        }
    }


}
