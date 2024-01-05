package org.shoulder.ext.config.service;

import jakarta.annotation.Nullable;
import org.apache.commons.collections4.CollectionUtils;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.util.AssertUtils;
import org.shoulder.ext.config.domain.ConfigType;
import org.shoulder.ext.config.domain.model.ConfigData;

import java.util.List;
import java.util.Map;

/**
 * 业务中常用的，剥离了 ConfigData
 *
 * @author lym
 */
@SuppressWarnings("unchecked")
public interface ConfigBizService {


    // ======================================== 查询单个 =====================================

    /**
     * 根据业务索引查询
     *
     * @param <T>           the type parameter
     * @param tenant        the tenant
     * @param configExample the config example
     * @return 查询结果
     */
    @Nullable
    default <T> T queryByIndex(String tenant, T configExample) {
        List<T> result = queryListByMultiCondition(tenant, configExample);
        if (CollectionUtils.isEmpty(result)) {
            return null;
        }
        AssertUtils.isTrue(result.size() == 1, CommonErrorCodeEnum.UNKNOWN);
        return result.get(0);
    }

    /**
     * 根据业务索引查询
     *
     * @param <T>         the type parameter
     * @param tenant      the tenant
     * @param configType  the config type
     * @param indexFields the index fields
     * @return 查询结果
     */
    @Nullable
    default <T> T queryByIndex(String tenant, ConfigType configType, Map<String, String> indexFields) {
        List<T> result = queryListByMultiCondition(tenant, configType, indexFields);
        if (CollectionUtils.isEmpty(result)) {
            return null;
        }
        AssertUtils.isTrue(result.size() == 1, CommonErrorCodeEnum.UNKNOWN);
        return result.get(0);
    }


    // ======================================== 查询多个 =====================================


    /**
     * 根据租户 + 配置类查询所有
     *
     * @param tenant     the tenant
     * @param configType the config name
     * @return 查询结果
     */
    default <T> List<T> queryListByTenantAndConfigName(String tenant, ConfigType configType) {
        return queryListByMultiCondition(tenant, configType, null);
    }

    /**
     * 租户 + 条件查询
     *
     * @param tenant        the tenant
     * @param configExample the config example
     * @return 查询结果
     */
    default <T> List<T> queryListByMultiCondition(String tenant, Object configExample) {
        return queryListByMultiCondition(tenant, ConfigType.getByType(configExample.getClass()),
                ConfigData.extractFieldsFromConfigObject(configExample));
    }


    /**
     * 租户 + 条件查询
     *
     * @param tenant          the tenant
     * @param configType      the config name
     * @param filterCondition the filter condition
     * @return 查询结果
     */
    <T> List<T> queryListByMultiCondition(String tenant, ConfigType configType,
                                          @Nullable Map<String, String> filterCondition);

}
