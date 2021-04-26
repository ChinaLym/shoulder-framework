package org.shoulder.ext.config.repository;

import org.shoulder.ext.config.domain.ConfigType;
import org.shoulder.ext.config.domain.PageInfo;
import org.shoulder.ext.config.domain.model.ConfigData;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public interface ConfigRepository {

    /**
     * Create.
     *
     * @param configData the config data
     */
    void save(ConfigData configData);

    /**
     * Update by id.
     *
     * @param configData the config data
     */
    void updateByBizIdAndVersion(ConfigData configData);

    /**
     * Delete by biz id and version int.
     *
     * @param bizId   the biz id
     * @param version the version
     * @return the int
     */
    int deleteByBizIdAndVersion(String bizId, int version);

    // ================= query ======================

    /**
     * Query by index field config data.
     *
     * @param tenant        the tenant
     * @param configExample the config example
     * @return the config data
     */
    @Nullable
    default ConfigData queryByIndex(String tenant, Object configExample) {
        return queryByBizId(ConfigData.calculateBizId(tenant, configExample));
    }

    /**
     * Query by index field config data.
     *
     * @param tenant      the tenant
     * @param configType  the config name
     * @param indexFields the index fields
     * @return the config data
     */
    @Nullable
    default ConfigData queryByIndex(String tenant, ConfigType configType, Map<String, String> indexFields) {
        return queryByBizId(ConfigData.calculateBizId(tenant, configType, indexFields));
    }

    /**
     * Query by biz id config data.
     *
     * @param bizId the biz id
     * @return the config data
     */
    @Nullable
    ConfigData queryByBizId(String bizId);

    /**
     * Lock by biz id config data.
     *
     * @param bizId the biz id
     * @return the config data
     */
    @Nullable
    ConfigData lockByBizId(String bizId);

    /**
     * Query list by multi condition list.
     *
     * @param tenant        the tenant
     * @param configExample the config example
     * @return the list
     */
    default List<ConfigData> queryListByMultiCondition(String tenant, Object configExample) {
        return queryListByMultiCondition(tenant, ConfigType.getByType(configExample.getClass()),
                ConfigData.extractFieldsFromConfigObject(configExample));
    }

    /**
     * Query page by multi condition page info.
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
     * Query all by tenant and config name list.
     *
     * @param tenant     the tenant
     * @param configType the config name
     * @return the list
     */
    default List<ConfigData> queryListByTenantAndConfigName(String tenant, ConfigType configType) {
        return queryListByMultiCondition(tenant, configType, null);
    }

    /**
     * Query page by tenant and config name list.
     *
     * @param tenant     the tenant
     * @param configType the config name
     * @param pageNum    the page num
     * @param pageSize   the page size
     * @return the list
     */
    default PageInfo<ConfigData> queryPageByTenantAndConfigName(String tenant, ConfigType configType, int pageNum, int pageSize) {
        return queryPageByMultiCondition(tenant, configType, null, pageNum, pageSize);
    }

    /**
     * Query all by multi condition list.
     *
     * @param tenant          the tenant
     * @param configType      the config name
     * @param filterCondition the filter condition
     * @return the list
     */
    List<ConfigData> queryListByMultiCondition(String tenant, ConfigType configType,
                                               @Nullable Map<String, String> filterCondition);

    /**
     * Query page by multi condition list.
     *
     * @param tenant          the tenant
     * @param configType      the config name
     * @param filterCondition the filter condition
     * @param pageNum         the page num
     * @param pageSize        the page size
     * @return the list
     */
    PageInfo<ConfigData> queryPageByMultiCondition(String tenant, ConfigType configType,
                                                   @Nullable Map<String, String> filterCondition,
                                                   int pageNum,
                                                   int pageSize);

}