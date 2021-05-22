package org.shoulder.ext.common.constant;

/**
 * 扩展常量
 *
 * @author lym
 */
public interface ShoulderExtConstants {

    /**
     * 后台管理 api 前缀
     */
    String BACKSTAGE_API_URL_PREFIX_V1 = "/backstage/api/v1";

    String CONFIG_URL_PREFIX = BACKSTAGE_API_URL_PREFIX_V1 + "/config";

    String TENANT_URL_PREFIX = BACKSTAGE_API_URL_PREFIX_V1 + "/tenant";

    /**
     * 后台管理日志 biz logger
     */
    String BACKSTAGE_BIZ_SERVICE_LOGGER = "BACKSTAGE-BIZ-SERVICE-LOGGER";

    /**
     * 后台管理日志 digest logger
     */
    String BACKSTAGE_DIGEST_LOGGER = "BACKSTAGE-DIGEST-LOGGER";

    /**
     * 后台管理数据源-事务管理器
     */
    String BACKSTAGE_TRANSACTION_MANAGER = "backstageTransactionManager";
}

