package org.shoulder.ext.config.repository;

import jakarta.annotation.Nullable;
import org.shoulder.ext.config.domain.ConfigType;
import org.shoulder.ext.config.domain.PageInfo;
import org.shoulder.ext.config.domain.model.ConfigData;

import java.util.List;
import java.util.Map;

/**
 * 配置存储接口
 *
 * @author lym
 */
public interface ConfigRepository {

    /**
     * 保存
     *
     * @param configData BO
     */
    void save(ConfigData configData);

    /**
     * 更新
     *
     * @param configData BO
     */
    void updateByBizIdAndVersion(ConfigData configData);

    /**
     * 删除
     *
     * @param bizId   the biz id
     * @param version the version
     * @return 影响行数
     */
    int deleteByBizIdAndVersion(String bizId, int version);

    // ================= query ======================

    /**
     * 条件查询
     *
     * @param tenant        the tenant
     * @param configExample the config example
     * @return BO
     */
    @Nullable
    default ConfigData queryByIndex(String tenant, Object configExample) {
        return queryByBizId(ConfigData.calculateBizId(tenant, configExample));
    }

    /**
     * 根据索引查询
     *
     * @param tenant      the tenant
     * @param configType  the config name
     * @param indexFields the index fields
     * @return BO
     */
    @Nullable
    default ConfigData queryByIndex(String tenant, ConfigType configType, Map<String, String> indexFields) {
        return queryByBizId(ConfigData.calculateBizId(tenant, configType, indexFields));
    }

    /**
     * 根据索引查询
     *
     * @param bizId the biz id
     * @return BO
     */
    @Nullable
    ConfigData queryByBizId(String bizId);

    /**
     * 根据索引锁定
     *
     * @param bizId the biz id
     * @return BO
     */
    @Nullable
    ConfigData lockByBizId(String bizId);

    /**
     * 条件查询
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
     * 条件查询
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
     * 条件查询
     *
     * @param tenant     the tenant
     * @param configType the config name
     * @return 查询结果
     */
    default List<ConfigData> queryListByTenantAndConfigName(String tenant, ConfigType configType) {
        return queryListByMultiCondition(tenant, configType, null);
    }

    /**
     * 条件查询
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
     * 条件查询
     *
     * @param tenant          the tenant
     * @param configType      the config name
     * @param filterCondition the filter condition
     * @return 查询结果
     */
    List<ConfigData> queryListByMultiCondition(String tenant, ConfigType configType,
                                               @Nullable Map<String, String> filterCondition);

    /**
     * 条件查询
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
