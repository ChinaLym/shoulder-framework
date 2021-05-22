package org.shoulder.ext.config.service.impl;

import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;
import org.shoulder.core.util.AssertUtils;
import org.shoulder.ext.common.constant.ShoulderExtConstants;
import org.shoulder.ext.config.domain.enums.ConfigErrorCodeEnum;
import org.shoulder.ext.config.domain.model.ConfigData;
import org.shoulder.ext.config.repository.ConfigRepository;
import org.shoulder.ext.config.service.ConfigManagerCoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * 配置管理
 *
 * @author lym
 */
@Service
public class ConfigManagerCoreServiceImpl implements ConfigManagerCoreService {

    protected static final Logger log = LoggerFactory.getLogger(ShoulderExtConstants.BACKSTAGE_BIZ_SERVICE_LOGGER);

    @Autowired
    private ConfigRepository configRepository;

    @Override
    @Transactional(transactionManager = ShoulderExtConstants.BACKSTAGE_TRANSACTION_MANAGER, rollbackFor = Exception.class)
    public void insert(ConfigData configData) {
        // 锁定 bizId，且不存在
        ConfigData configDataInDb = configRepository.lockByBizId(configData.getBizId());
        AssertUtils.isNull(configDataInDb, ConfigErrorCodeEnum.CONFIG_DATA_ALREADY_EXISTS);

        // 插入
        configRepository.save(configData);
        // todo 保存日志
    }

    @Override
    @Transactional(transactionManager = ShoulderExtConstants.BACKSTAGE_TRANSACTION_MANAGER, rollbackFor = Exception.class)
    public void migration(ConfigData configData, boolean overwrite) {
        // 锁定 bizId，且不存在
        ConfigData configDataInDb = configRepository.lockByBizId(configData.getBizId());
        if (configDataInDb != null) {
            if (!overwrite) {
                AssertUtils.isNull(configDataInDb, ConfigErrorCodeEnum.CONFIG_DATA_ALREADY_EXISTS);
                return;
            } else {
                log.warn(configData.getBizId() + " already exist, OVERWRITE.");
                delete(configDataInDb);
            }
        }
        // 插入
        configRepository.save(configData);
        // todo 保存日志
    }

    @Override
    @Transactional(transactionManager = ShoulderExtConstants.BACKSTAGE_TRANSACTION_MANAGER, rollbackFor = Exception.class)
    public void update(ConfigData configData) {
        // 更新
        configRepository.updateByBizIdAndVersion(configData);
        // todo 保存日志
    }

    @Override
    @Transactional(transactionManager = ShoulderExtConstants.BACKSTAGE_TRANSACTION_MANAGER, rollbackFor = Exception.class)
    public boolean delete(ConfigData configData) {
        int changed = configRepository.deleteByBizIdAndVersion(configData.getBizId(), configData.getVersion());
        if (changed > 0) {
            // todo 保存日志

            return true;
        }
        return false;
    }

    public void setConfigRepository(ConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

}