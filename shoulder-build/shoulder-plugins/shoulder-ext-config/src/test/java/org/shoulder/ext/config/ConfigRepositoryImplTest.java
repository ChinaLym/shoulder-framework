package org.shoulder.ext.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.shoulder.ext.config.dal.dao.ConfigDataDAO;
import org.shoulder.ext.config.dal.dataobject.ConfigDataDO;
import org.shoulder.ext.config.domain.ConfigType;
import org.shoulder.ext.config.domain.PageInfo;
import org.shoulder.ext.config.domain.enums.TenantEnum;
import org.shoulder.ext.config.domain.model.ConfigData;
import org.shoulder.ext.config.repository.impl.ConfigRepositoryImpl;
import org.shoulder.ext.config.usecase.RegionConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ConfigRepositoryImplTest extends BasePowerMock {
    ConfigRepositoryImpl configRepository;

    @Mock
    ConfigDataDAO configDataDAO;

    ConfigType CONFIG_TYPE = ConfigType.getByType(RegionConfig.class);

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        configRepository = new ConfigRepositoryImpl();
        configRepository.setConfigDataDAO(configDataDAO);
    }

    @Test
    public void testSave() {
        Mockito.when(configDataDAO.insert(Mockito.any())).thenReturn(1);
        ConfigData configData = new ConfigData();
        configData.setTenant(TenantEnum.DEFAULT);
        configData.setConfigType(CONFIG_TYPE);
        configData.setBusinessValue(new HashMap<>());
        configData.setConfigObj(new RegionConfig());
        configRepository.save(configData);
    }

    @Test
    public void testUpdateByBizIdAndVersion() {
        Mockito.when(configDataDAO.updateByBizIdAndVersion(Mockito.any())).thenReturn(1);
        ConfigData configData = new ConfigData();
        configData.setTenant(TenantEnum.DEFAULT);
        configData.setConfigType(CONFIG_TYPE);
        configData.setBusinessValue(new HashMap<>());
        configData.setConfigObj(new RegionConfig());
        configRepository.updateByBizIdAndVersion(configData);
    }

    @Test
    public void testDeleteByBizIdAndVersion() {
        Mockito.when(configDataDAO.updateDeleteVersionByBizIdAndVersion(Mockito.any())).thenReturn(1);
        configRepository.deleteByBizIdAndVersion("ass", 0);
    }

    @Test
    public void testQueryByBizId() {
        ConfigDataDO configDataDO = new ConfigDataDO();
        configDataDO.setBizId("1");
        configDataDO.setVersion(1);
        configDataDO.setTenant("DEFAULT");
        configDataDO.setType("RegionConfig");
        configDataDO.setBusinessValue("{\"region\":\"US\"}");
        Mockito.when(configDataDAO.querySingleByBizId(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(configDataDO);
        configRepository.queryByBizId("1");
    }


    @Test
    public void testLockByBizId() {
        ConfigDataDO configDataDO = new ConfigDataDO();
        configDataDO.setBizId("1");
        configDataDO.setVersion(1);
        configDataDO.setTenant("DEFAULT");
        configDataDO.setType("RegionConfig");
        configDataDO.setBusinessValue("{\"region\":\"US\"}");
        Mockito.when(configDataDAO.querySingleByBizId(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(configDataDO);
        configRepository.lockByBizId("1");
    }

    @Test
    public void testQueryListByMultiCondition() {
        List<ConfigDataDO> dataList = new ArrayList<>();
        ConfigDataDO configData = new ConfigDataDO();
        configData.setBizId("1");
        configData.setTenant(TenantEnum.DEFAULT);
        configData.setBusinessValue("{}");
        dataList.add(configData);
        Mockito.when(configDataDAO.queryListByMultiCondition(Mockito.any(), Mockito.anyList(), Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(dataList);
        configRepository.queryListByMultiCondition(TenantEnum.DEFAULT, CONFIG_TYPE, null);
    }


    @Test
    public void testQueryPageByMultiCondition() {
        List<ConfigDataDO> dataList = new ArrayList<>();
        ConfigDataDO configData = new ConfigDataDO();
        configData.setBizId("1");
        configData.setTenant(TenantEnum.DEFAULT);
        configData.setBusinessValue("{}");
        dataList.add(configData);

        PageInfo<ConfigData> pageList = new PageInfo<>();
        List<ConfigData> resultList = new ArrayList<>();
        ConfigData configDataDomain = new ConfigData();
        configDataDomain.setBizId("1");
        configDataDomain.setTenant(TenantEnum.DEFAULT);
        configDataDomain.setBusinessValue(new HashMap<>());
        pageList.setData(resultList);
        pageList.setPageNo(1);
        pageList.setPageSize(1);
        pageList.setTotalCount(1L);
        pageList.setTotalPage(1L);

        Mockito.when(configDataDAO.queryListByMultiCondition(Mockito.any(), Mockito.anyList(), Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(dataList);
        configRepository.queryPageByMultiCondition(TenantEnum.DEFAULT, CONFIG_TYPE,
                null, 1, 1);
    }


}