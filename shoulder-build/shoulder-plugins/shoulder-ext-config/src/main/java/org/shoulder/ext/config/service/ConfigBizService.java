package org.shoulder.ext.config.service;

import org.apache.commons.collections4.CollectionUtils;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.util.AssertUtils;
import org.shoulder.ext.config.domain.ConfigType;
import org.shoulder.ext.config.domain.model.ConfigData;

import javax.annotation.Nullable;
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
     * Query by index
     *
     * @param <T>           the type parameter
     * @param tenant        the tenant
     * @param configExample the config example
     * @return the t
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
     * Query by index
     *
     * @param <T>         the type parameter
     * @param tenant      the tenant
     * @param configType  the config type
     * @param indexFields the index fields
     * @return the t
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
     * Query all by tenant and config name list.
     *
     * @param tenant     the tenant
     * @param configType the config name
     * @return the list
     */
    default <T> List<T> queryListByTenantAndConfigName(String tenant, ConfigType configType) {
        return queryListByMultiCondition(tenant, configType, null);
    }

    /**
     * Query list by multi condition list.
     *
     * @param tenant        the tenant
     * @param configExample the config example
     * @return the list
     */
    default <T> List<T> queryListByMultiCondition(String tenant, Object configExample) {
        return queryListByMultiCondition(tenant, ConfigType.getByType(configExample.getClass()),
                ConfigData.extractFieldsFromConfigObject(configExample));
    }


    /**
     * Query all by multi condition list.
     *
     * @param tenant          the tenant
     * @param configType      the config name
     * @param filterCondition the filter condition
     * @return the list
     */
    <T> List<T> queryListByMultiCondition(String tenant, ConfigType configType,
                                          @Nullable Map<String, String> filterCondition);

}