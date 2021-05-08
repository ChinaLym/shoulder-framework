package org.shoulder.ext.config.repository.impl;

import org.apache.commons.collections4.MapUtils;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.util.AssertUtils;
import org.shoulder.core.util.JsonUtils;
import org.shoulder.core.util.StringUtils;
import org.shoulder.ext.config.dal.dao.ConfigDataDAO;
import org.shoulder.ext.config.dal.dataobject.ConfigDataDO;
import org.shoulder.ext.config.domain.ConfigType;
import org.shoulder.ext.config.domain.PageInfo;
import org.shoulder.ext.config.domain.ex.ConfigException;
import org.shoulder.ext.config.domain.model.ConfigData;
import org.shoulder.ext.config.domain.model.ConfigFieldInfo;
import org.shoulder.ext.config.repository.ConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ConfigRepositoryImpl implements ConfigRepository {

    @Autowired
    private ConfigDataDAO configDataDAO;

    @Override
    public void save(ConfigData configData) {
        ConfigDataDO configDataDO = convertToDO(configData);
        int changed = configDataDAO.insert(configDataDO);
        AssertUtils.isTrue(changed == 1, CommonErrorCodeEnum.UNKNOWN);
    }

    @Override
    public void updateByBizIdAndVersion(ConfigData configData) {
        int changed = configDataDAO.updateByBizIdAndVersion(convertToDO(configData));
        AssertUtils.isTrue(changed == 1, CommonErrorCodeEnum.UNKNOWN);
    }

    @Override
    public int deleteByBizIdAndVersion(String bizId, int version) {
        ConfigDataDO configDataDO = new ConfigDataDO();
        configDataDO.setBizId(bizId);
        configDataDO.setVersion(version);
        return configDataDAO.updateDeleteVersionByBizIdAndVersion(configDataDO);
    }

    // ============================================================

    @Nullable
    @Override
    public ConfigData queryByBizId(String bizId) {
        ConfigDataDO result = configDataDAO.querySingleByBizId(bizId, false);
        return convertToDomain(result);
    }

    @Override
    public ConfigData lockByBizId(String bizId) {
        ConfigDataDO result = configDataDAO.querySingleByBizId(bizId, true);
        return convertToDomain(result);
    }

    @Override
    public List<ConfigData> queryListByMultiCondition(String tenant, ConfigType configType,
                                                      @Nullable Map<String, String> filterCondition) {

        ConfigDataDO configDataDO = genAdaptiveCondition(tenant, configType, filterCondition);
        List<ConfigDataDO> configDataDAOList = configDataDAO.queryListByMultiCondition(configDataDO, null, null, null);

        return convertToDomainList(configDataDAOList, filterCondition);
    }

    @Override
    public PageInfo<ConfigData> queryPageByMultiCondition(String tenant, ConfigType configType,
                                                          @Nullable Map<String, String> filterCondition, int pageNum,
                                                          int pageSize) {
        ConfigDataDO configDataDO = genAdaptiveCondition(tenant, configType, filterCondition);
        List<ConfigDataDO> configDataDAOList = configDataDAO.queryListByMultiCondition(configDataDO, null, (pageNum - 1) * pageSize, pageSize);

        long total = configDataDAO.countByMultiCondition(configDataDO, null);
        List<ConfigData> configDOList = convertToDomainList(configDataDAOList, filterCondition);
        return PageInfo.success(configDOList, pageNum, pageSize, total);
    }

    /**
     * 生成自适应的查询条件，将尝试走索引
     *
     * @return 查询条件
     */
    private ConfigDataDO genAdaptiveCondition(String tenant, ConfigType configType,
                                              @Nullable Map<String, String> filterCondition) {
        ConfigDataDO configDataDO = new ConfigDataDO();
        boolean useIndex = MapUtils.isNotEmpty(filterCondition) && canUseIndex(configType, filterCondition);
        if (useIndex) {
            configDataDO.setBizId(ConfigData.calculateBizId(tenant, configType, filterCondition));
        } else {
            configDataDO.setTenant(tenant);
            configDataDO.setType(configType.getConfigName());
        }
        return configDataDO;
    }

    /**
     * 能否使用索引加速查询
     *
     * @return boolean
     */
    private boolean canUseIndex(ConfigType configType, Map<String, String> filterCondition) {
        boolean indexMiss = configType.getIndexFieldInfoList().stream().anyMatch(f -> !filterCondition.containsKey(f.getName()));
        return !indexMiss;
    }

    /**
     * 转为领域模型
     */
    private List<ConfigData> convertToDomainList(List<ConfigDataDO> configDataDOList, Map<String, String> filterCondition) {
        boolean withoutAnyCondition = MapUtils.isEmpty(filterCondition);
        return configDataDOList.stream()
                // convert
                .map(this::convertToDomain)
                // filter
                .filter(c -> withoutAnyCondition || isMatchCondition(c, filterCondition))
                .collect(Collectors.toList());
    }

    /**
     * 检查是否满足 过滤条件
     * 只支持一级过滤
     *
     * @param config          配置信息
     * @param filterCondition 过滤条件，目前只有等于
     * @return 是否满足
     */
    private boolean isMatchCondition(ConfigData config, Map<String, String> filterCondition) {
        if (config == null) {
            return false;
        }
        if (MapUtils.isEmpty(filterCondition)) {
            return true;
        }
        ConfigType configType = config.getConfigType();
        List<ConfigFieldInfo> needFilterFieldList = configType.getFieldInfoList().stream()
                // 筛选出需要过滤的字段: 存在 key，且 value != null
                .filter(f -> filterCondition.get(f.getName()) != null).collect(Collectors.toList());
        try {
            for (ConfigFieldInfo fieldInfo : needFilterFieldList) {
                if (!StringUtils.equals(filterCondition.get(fieldInfo.getName()),
                        String.valueOf(fieldInfo.getReadMethod().invoke(config.getConfigObj())))) {
                    // 字段值不符合期望值
                    return false;
                }
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new ConfigException(e, CommonErrorCodeEnum.UNKNOWN);
        }
        return true;
    }

    private ConfigData convertToDomain(ConfigDataDO configDO) {
        if (configDO == null) {
            return null;
        }
        ConfigData configData = new ConfigData();
        configData.setCreateTime(configDO.getCreateTime());
        configData.setUpdateTime(configDO.getUpdateTime());
        configData.setTenant(configDO.getTenant());
        configData.setConfigType(ConfigType.getByName(configDO.getType()));
        configData.setBizId(configDO.getBizId());
        configData.setVersion(configDO.getVersion());
        configData.setOperatorNo(configDO.getModifier());
        // todo 创建者名称
        //configData.setOperatorName(configDO.getOperatorName());
        configData.setConfigObj(JsonUtils.toObject(configDO.getBusinessValue(), configData.getConfigType().getClazz()));
        configData.setBusinessValue(ConfigData.extractFieldsFromConfigObject(configData.getConfigObj()));
        return configData;
    }

    private ConfigDataDO convertToDO(ConfigData configData) {
        if (configData == null) {
            return null;
        }
        ConfigDataDO configDO = new ConfigDataDO();
        configDO.setCreateTime(configData.getCreateTime());
        configDO.setUpdateTime(configData.getUpdateTime());
        configDO.setTenant(configData.getTenant());
        configDO.setType(configData.getConfigType().getConfigName());
        configDO.setBizId(configData.getBizId());
        configDO.setVersion(configData.getVersion());
        configDO.setModifier(configData.getOperatorNo());
        //configDO.setModifier(configData.getOperatorName());
        AssertUtils.notNull(configData.getConfigObj(), CommonErrorCodeEnum.UNKNOWN);
        configDO.setBusinessValue(JsonUtils.toJson(configData.getConfigObj()));
        return configDO;
    }

    public void setConfigDataDAO(ConfigDataDAO configDataDAO) {
        this.configDataDAO = configDataDAO;
    }
}