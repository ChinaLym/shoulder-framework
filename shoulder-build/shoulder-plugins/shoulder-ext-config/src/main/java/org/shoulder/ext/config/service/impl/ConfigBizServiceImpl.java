package org.shoulder.ext.config.service.impl;

import jakarta.annotation.Nonnull;
import lombok.Setter;
import org.shoulder.ext.config.domain.ConfigType;
import org.shoulder.ext.config.service.ConfigBizService;
import org.shoulder.ext.config.service.ConfigQueryCoreService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author lym
 */
@SuppressWarnings("unchecked")
public class ConfigBizServiceImpl implements ConfigBizService {

    @Setter
    @Autowired
    private ConfigQueryCoreService configQueryCoreService;

    @Nonnull
    @Override
    public <T> List<T> queryListByMultiCondition(@Nonnull String tenant, @Nonnull ConfigType configType, Map<String, String> filterCondition) {
        return configQueryCoreService.queryListByMultiCondition(tenant, configType, filterCondition)
                .stream()
                .map(c -> (T) c.getConfigObj())
                .collect(Collectors.toList());
    }

}
