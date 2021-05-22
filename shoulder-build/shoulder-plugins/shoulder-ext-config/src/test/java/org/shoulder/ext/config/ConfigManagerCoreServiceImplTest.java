package org.shoulder.ext.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.shoulder.ext.config.domain.model.ConfigData;
import org.shoulder.ext.config.repository.ConfigRepository;
import org.shoulder.ext.config.service.impl.ConfigManagerCoreServiceImpl;

public class ConfigManagerCoreServiceImplTest extends BasePowerMock {
    ConfigManagerCoreServiceImpl configManagerCoreService;

    @Mock
    ConfigRepository configRepository;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        configManagerCoreService = new ConfigManagerCoreServiceImpl();
        configManagerCoreService.setConfigRepository(configRepository);

    }

    @Test
    public void testInsertConfigHistoryLog() {
        ConfigData configData = new ConfigData();
        Mockito.when(configRepository.lockByBizId(Mockito.anyString())).thenReturn(configData);
        configRepository.save(configData);
        configManagerCoreService.insert(configData);
    }

    @Test
    public void testInsertConfigHistoryLoNull() {
        ConfigData configData = new ConfigData();
        Mockito.when(configRepository.lockByBizId(Mockito.anyString())).thenReturn(configData);
        configRepository.save(configData);
        configManagerCoreService.insert(configData);
    }

    @Test
    public void testMigration() {
        ConfigData configData = new ConfigData();
        Mockito.when(configRepository.lockByBizId(Mockito.anyString())).thenReturn(configData);
        configRepository.save(configData);
        configManagerCoreService.migration(configData, true);
    }

    @Test
    public void testUpdate() {
        ConfigData configData = new ConfigData();
        configRepository.updateByBizIdAndVersion(configData);
        configManagerCoreService.update(configData);
    }

    @Test
    public void testDelete() {
        ConfigData configData = new ConfigData();
        configData.setBizId("sa");
        configData.setVersion(0);
        configManagerCoreService.delete(configData);
    }

    @Test
    public void testDeleteFail() {
        ConfigData configData = new ConfigData();
        configData.setBizId("131");
        configData.setVersion(0);
        configManagerCoreService.delete(configData);
    }

}