package org.shoulder.ext.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.shoulder.ext.config.domain.ConfigType;
import org.shoulder.ext.config.domain.PageInfo;
import org.shoulder.ext.config.domain.enums.TenantEnum;
import org.shoulder.ext.config.domain.model.ConfigData;
import org.shoulder.ext.config.repository.ConfigRepository;
import org.shoulder.ext.config.service.impl.ConfigQueryCoreServiceImpl;
import org.shoulder.ext.config.usecase.RegionConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigQueryCoreServiceImplTest extends BasePowerMock {

    ConfigQueryCoreServiceImpl configQueryCoreService;

    @Mock
    ConfigRepository configRepository;

    ConfigType CONFIG_TYPE = ConfigType.getByType(RegionConfig.class);

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        configQueryCoreService = new ConfigQueryCoreServiceImpl();
        configQueryCoreService.setConfigRepository(configRepository);
    }

    @Test
    public void testQueryByBizId() {
        ConfigData configDataQuery = new ConfigData();
        Mockito.when(configRepository.queryByBizId(Mockito.anyString())).thenReturn(configDataQuery);
        configQueryCoreService.queryByBizId("123");
    }

    @Test
    public void testLockByBizId() {
        ConfigData configDataQuery = new ConfigData();
        Mockito.when(configRepository.lockByBizId(Mockito.anyString())).thenReturn(configDataQuery);
        configQueryCoreService.lockByBizId("123");
    }

    @Test
    public void testQueryPageByMultiCondition() {
        PageInfo<ConfigData> pageInfo = new PageInfo<>();
        Mockito.when(configRepository.queryPageByMultiCondition(Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(pageInfo);
        PageInfo<ConfigData> queryResult = configQueryCoreService.queryPageByMultiCondition(TenantEnum.DEFAULT, new RegionConfig(), 1, 10);
        Assertions.assertEquals(pageInfo, queryResult);
    }

    @Test
    public void testQueryListByMultiCondition() {
        List<ConfigData> configDataList = new ArrayList<>();
        Mockito.when(configRepository.queryListByMultiCondition(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(configDataList);

        List<ConfigData> configData = configQueryCoreService.queryListByMultiCondition(TenantEnum.DEFAULT, new RegionConfig());

        Assertions.assertNotNull(configData);
    }

    @Test
    public void testQueryByIndex() {
        RegionConfig regionConfig = new RegionConfig();
        regionConfig.setRegion("CN");
        Mockito.when(configRepository.queryByIndex(Mockito.any(), Mockito.any())).thenReturn(null);
        configQueryCoreService.queryByIndex(TenantEnum.DEFAULT, regionConfig);
    }


    @Test
    public void testQueryByIndexMul() {
        ConfigData configDataQuery = new ConfigData();
        Map<String, String> dataMap = new HashMap<>();
        dataMap.put("region", "CN");
        Mockito.when(configRepository.queryByBizId(Mockito.any())).thenReturn(configDataQuery);
        configQueryCoreService.queryByIndex(TenantEnum.DEFAULT, CONFIG_TYPE, dataMap);
    }

    @Test
    public void testQueryListByTenantAndConfigName() {
        List<ConfigData> configDataList = new ArrayList<>();
        Mockito.when(configRepository.queryListByTenantAndConfigName(Mockito.any(), Mockito.any())).thenReturn(configDataList);
        configQueryCoreService.queryListByTenantAndConfigName(TenantEnum.DEFAULT, CONFIG_TYPE);
    }

    @Test
    public void testQueryPageByTenantAndConfigName() {
        PageInfo<ConfigData> pageInfo = new PageInfo<>();
        Mockito.when(configRepository.queryPageByMultiCondition(Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(pageInfo);
        configQueryCoreService.queryPageByTenantAndConfigName(TenantEnum.DEFAULT, CONFIG_TYPE, 1, 1);
    }

}