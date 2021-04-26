package org.shoulder.ext.config.service.impl;

import org.shoulder.ext.config.domain.ConfigType;
import org.shoulder.ext.config.domain.PageInfo;
import org.shoulder.ext.config.domain.model.ConfigData;
import org.shoulder.ext.config.repository.ConfigRepository;
import org.shoulder.ext.config.service.ConfigQueryCoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

@Service
public class ConfigQueryCoreServiceImpl implements ConfigQueryCoreService {

    @Autowired
    private ConfigRepository configRepository;

    /**
     * Query by biz id config data.
     *
     * @param bizId the biz id
     * @return the config data
     */
    @Override
    public ConfigData queryByBizId(String bizId) {
        return configRepository.queryByBizId(bizId);
    }

    /**
     * Lock by biz id config data.
     *
     * @param bizId the biz id
     * @return the config data
     */
    @Override
    public ConfigData lockByBizId(String bizId) {
        return configRepository.lockByBizId(bizId);
    }

    /**
     * Query list by multi condition list.
     *
     * @param tenant          the tenant
     * @param configType      the config type
     * @param filterCondition the filter condition
     * @return the list
     */
    @Override
    public List<ConfigData> queryListByMultiCondition(String tenant, ConfigType configType,
                                                      @Nullable Map<String, String> filterCondition) {
        return configRepository.queryListByMultiCondition(tenant, configType, filterCondition);
    }

    /**
     * Query page by multi condition page info.
     *
     * @param tenant          the tenant
     * @param configType      the config type
     * @param filterCondition the filter condition
     * @param pageNum         the page num
     * @param pageSize        the page size
     * @return the page info
     */
    @Override
    public PageInfo<ConfigData> queryPageByMultiCondition(String tenant, ConfigType configType,
                                                          @Nullable Map<String, String> filterCondition, int pageNum,
                                                          int pageSize) {
        return configRepository.queryPageByMultiCondition(tenant, configType, filterCondition, pageNum, pageSize);
    }

    public void setConfigRepository(ConfigRepository configRepository) {
        this.configRepository = configRepository;
    }
}