package org.shoulder.ext.config.service;

import org.shoulder.ext.config.domain.ConfigType;
import org.shoulder.ext.config.domain.PageInfo;
import org.shoulder.ext.config.domain.model.ConfigData;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * 查询
 *
 * @author lym
 */
public interface ConfigQueryCoreService {

    // ======================================== 查询单个 =====================================

    /**
     * 根据主键查
     *
     * @param tenant        the tenant
     * @param configExample the config example
     * @return 查询结果
     */
    @Nullable
    default ConfigData queryByIndex(String tenant, Object configExample) {
        return queryByBizId(ConfigData.calculateBizId(tenant, configExample));
    }

    /**
     * 根据主键查
     *
     * @param tenant      the tenant
     * @param configType  the config name
     * @param indexFields the index fields
     * @return 查询结果
     */
    @Nullable
    default ConfigData queryByIndex(String tenant, ConfigType configType, Map<String, String> indexFields) {
        return queryByBizId(ConfigData.calculateBizId(tenant, configType, indexFields));
    }

    /**
     * 根据 bizId 查
     *
     * @param bizId the biz id
     * @return 查询结果
     */
    @Nullable
    ConfigData queryByBizId(String bizId);

    /**
     * 根据 bizId 锁
     *
     * @param bizId the biz id
     * @return 查询结果
     */
    @Nullable
    ConfigData lockByBizId(String bizId);


    // ======================================== 查询多个-全部 =====================================

    /**
     * 根据租户 + 配置类型查
     *
     * @param tenant     the tenant
     * @param configType the config name
     * @return 查询结果
     */
    default List<ConfigData> queryListByTenantAndConfigName(String tenant, ConfigType configType) {
        return queryListByMultiCondition(tenant, configType, null);
    }

    /**
     * 多条件查询
     *
     * @param tenant        the tenant
     * @param configExample the config example
     * @return 查询结果
     */
    default List<ConfigData> queryListByMultiCondition(String tenant, Object configExample) {
        return queryListByMultiCondition(tenant, ConfigType.getByType(configExample.getClass()),
                ConfigData.extractFieldsFromConfigObject(configExample));
    }


    /**
     * 多条件查询
     *
     * @param tenant          the tenant
     * @param configType      the config name
     * @param filterCondition the filter condition
     * @return 查询结果
     */
    List<ConfigData> queryListByMultiCondition(String tenant, ConfigType configType,
                                               @Nullable Map<String, String> filterCondition);


    // ======================================== 查询多个-分页 =====================================


    /**
     * 多条件分页查询
     *
     * @param tenant        the tenant
     * @param configExample the config example
     * @param pageNo        the page no
     * @param pageSize      the page size
     * @return the page info
     */
    default PageInfo<ConfigData> queryPageByMultiCondition(String tenant, Object configExample, int pageNo, int pageSize) {
        return queryPageByMultiCondition(tenant, ConfigType.getByType(configExample.getClass()),
                ConfigData.extractFieldsFromConfigObject(configExample), pageNo, pageSize);
    }


    /**
     * 多条件分页查询
     *
     * @param tenant     the tenant
     * @param configType the config name
     * @param pageNum    the page num
     * @param pageSize   the page size
     * @return 查询结果
     */
    default PageInfo<ConfigData> queryPageByTenantAndConfigName(String tenant, ConfigType configType, int pageNum, int pageSize) {
        return queryPageByMultiCondition(tenant, configType, null, pageNum, pageSize);
    }

    /**
     * 多条件分页查询
     *
     * @param tenant          the tenant
     * @param configType      the config name
     * @param filterCondition the filter condition
     * @param pageNum         the page num
     * @param pageSize        the page size
     * @return 查询结果
     */
    PageInfo<ConfigData> queryPageByMultiCondition(String tenant, ConfigType configType,
                                                   @Nullable Map<String, String> filterCondition,
                                                   int pageNum,
                                                   int pageSize);

}