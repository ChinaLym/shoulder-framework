package org.shoulder.ext.config;

import org.shoulder.ext.config.domain.ConfigType;
import org.shoulder.ext.config.domain.model.ConfigData;
import org.shoulder.ext.config.service.ConfigBizService;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class InMemoryConfigBizService implements ConfigBizService {

    private InMemoryConfigRepositoryService configService = new InMemoryConfigRepositoryService();

    @Nonnull
    @Override
    public <T> List<T> queryListByMultiCondition(String tenant, ConfigType configType, Map<String, String> filterCondition) {
        return configService.queryListByMultiCondition(tenant, configType, filterCondition)
                .stream()
                .map(c -> (T) c.getConfigObj())
                .collect(Collectors.toList());
    }

    public InMemoryConfigRepositoryService getConfigService() {
        return configService;
    }

    public void setConfigService(InMemoryConfigRepositoryService configService) {
        this.configService = configService;
    }

    /**
     * 模拟数据库存在的数据
     */
    public synchronized void addConfig(String tenant, @Nonnull Object config) {
        ConfigData configData = new ConfigData(tenant, config);
        configService.delete(configData);
        configService.insert(configData);
    }

}
