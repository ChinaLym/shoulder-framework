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

/**
 * 配置查询
 *
 * @author lym
 */
@Service
public class ConfigQueryCoreServiceImpl implements ConfigQueryCoreService {

    @Autowired
    private ConfigRepository configRepository;

    /**
     * 根据 bizId 查询
     */
    @Override
    public ConfigData queryByBizId(String bizId) {
        return configRepository.queryByBizId(bizId);
    }

    /**
     * 根据 bizId 锁定
     */
    @Override
    public ConfigData lockByBizId(String bizId) {
        return configRepository.lockByBizId(bizId);
    }

    /**
     * 多条件查询
     */
    @Override
    public List<ConfigData> queryListByMultiCondition(String tenant, ConfigType configType,
                                                      @Nullable Map<String, String> filterCondition) {
        return configRepository.queryListByMultiCondition(tenant, configType, filterCondition);
    }

    /**
     * 多条件分页查询
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