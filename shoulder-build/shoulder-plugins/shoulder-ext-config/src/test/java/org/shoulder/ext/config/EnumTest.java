package org.shoulder.ext.config;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.shoulder.core.exception.BaseRuntimeException;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.ext.config.domain.ConfigType;
import org.shoulder.ext.config.domain.enums.ConfigErrorCodeEnum;
import org.shoulder.ext.config.domain.enums.TenantEnum;
import org.shoulder.ext.config.domain.ex.ConfigException;

public class EnumTest {

    @Test
    public void testFieldList() {
        for (ConfigType e : ConfigType.values()) {
            Assert.assertTrue(CollectionUtils.isNotEmpty(e.getFieldInfoList()));
            Assert.assertTrue(CollectionUtils.isNotEmpty(e.getIndexFieldInfoList()));
        }
    }

    @Test
    public void testGetByFail() {
        ConfigType configType = null;
        try {
            configType = ConfigType.getByType(Integer.class);
            Assert.fail();
        } catch (BaseRuntimeException excepted) {
            Assert.assertEquals(excepted.getCode(), ConfigErrorCodeEnum.CODING_ERROR.getCode());
        }
        Assert.assertNull(configType);


        try {
            configType = ConfigType.getByName("123");
            Assert.fail();
        } catch (BaseRuntimeException excepted) {
            Assert.assertEquals(excepted.getCode(), ConfigErrorCodeEnum.CONFIG_TYPE_NOT_EXISTS.getCode());
        }
        Assert.assertNull(configType);

        String tenantEnum = null;
        try {
            tenantEnum = TenantEnum.getByName("ABC");
            Assert.fail();
        } catch (ConfigException excepted) {
            Assert.assertEquals(excepted.getCode(), CommonErrorCodeEnum.TENANT_INVALID.getCode());
        }
        Assert.assertNull(tenantEnum);

    }

}