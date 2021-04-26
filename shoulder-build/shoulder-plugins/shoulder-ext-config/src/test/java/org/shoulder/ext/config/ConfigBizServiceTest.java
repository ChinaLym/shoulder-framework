package org.shoulder.ext.config;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.shoulder.ext.config.domain.ConfigType;
import org.shoulder.ext.config.domain.enums.TenantEnum;
import org.shoulder.ext.config.service.ConfigQueryCoreService;
import org.shoulder.ext.config.service.impl.ConfigBizServiceImpl;
import org.shoulder.ext.config.usecase.RegionConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ConfigBizServiceTest extends BasePowerMock {

    ConfigBizServiceImpl configBizService;

    @Mock
    ConfigQueryCoreService configQueryCoreService;

    InMemoryConfigBizService inMemoryConfigBizService;

    ConfigType CONFIG_TYPE = ConfigType.getByType(RegionConfig.class);

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        configBizService = new ConfigBizServiceImpl();
        configBizService.setConfigQueryCoreService(configQueryCoreService);
        inMemoryConfigBizService = new InMemoryConfigBizService();
    }

    @Test
    public void queryByIndex() {
        RegionConfig config = new RegionConfig("US");
        inMemoryConfigBizService.addConfig(TenantEnum.DEFAULT, config);

        Map<String, String> condition = new HashMap<>();
        condition.put("region", "US");

        RegionConfig result1 = inMemoryConfigBizService.queryByIndex(TenantEnum.DEFAULT, CONFIG_TYPE, condition);
        RegionConfig result2 = inMemoryConfigBizService.queryByIndex(TenantEnum.DEFAULT, config);

        Assert.assertNotNull(result1);
        Assert.assertNotNull(result2);


        inMemoryConfigBizService.setConfigService(new InMemoryConfigRepositoryService());
        RegionConfig result3 = inMemoryConfigBizService.queryByIndex(TenantEnum.DEFAULT, CONFIG_TYPE, condition);
        RegionConfig result4 = inMemoryConfigBizService.queryByIndex(TenantEnum.DEFAULT, config);

        Assert.assertNull(result3);
        Assert.assertNull(result4);
    }


    @Test
    public void queryListByMultiCondition() {
        RegionConfig data1 = new RegionConfig("AB");
        RegionConfig data2 = new RegionConfig("US");
        RegionConfig data3 = new RegionConfig("SG");
        inMemoryConfigBizService.addConfig(TenantEnum.DEFAULT, data1);
        inMemoryConfigBizService.addConfig(TenantEnum.DEFAULT, data2);
        inMemoryConfigBizService.addConfig(TenantEnum.DEFAULT, data3);

        Map<String, String> condition = new HashMap();
        condition.put("region", "US");

        List<RegionConfig> result1 = inMemoryConfigBizService.queryListByTenantAndConfigName(TenantEnum.DEFAULT, CONFIG_TYPE);
        List<RegionConfig> result2 = inMemoryConfigBizService.queryListByMultiCondition(TenantEnum.DEFAULT, new RegionConfig());

        Assert.assertNotNull(result1);
        Assert.assertNotNull(result2);
        for (int i = 0; i < result1.size(); i++) {
            Assert.assertEquals(result1.get(i), result2.get(i));
        }
        Assert.assertEquals(result1, result2);
    }


}