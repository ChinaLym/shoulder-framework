package org.shoulder.ext.config.provider.controller;

import jakarta.validation.ConstraintViolationException;
import org.apache.commons.collections4.CollectionUtils;
import org.shoulder.core.dto.response.BaseResult;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;
import org.shoulder.core.util.JsonUtils;
import org.shoulder.core.util.StringUtils;
import org.shoulder.ext.common.constant.ShoulderExtConstants;
import org.shoulder.ext.config.domain.ConfigType;
import org.shoulder.ext.config.domain.enums.ConfigErrorCodeEnum;
import org.shoulder.ext.config.domain.enums.TenantEnum;
import org.shoulder.ext.config.domain.ex.ConfigException;
import org.shoulder.ext.config.domain.model.ConfigData;
import org.shoulder.ext.config.provider.controller.spi.OldConfigDataQueryService;
import org.shoulder.ext.config.service.ConfigManagerCoreService;
import org.shoulder.ext.config.service.ConfigQueryCoreService;
import org.shoulder.validate.util.ValidateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * 配置项迁移接口
 *
 * @author lym
 */
@Validated
@RestControllerAdvice
@Controller
@RequestMapping(ShoulderExtConstants.CONFIG_URL_PREFIX + "/migration")
public class ConfigDataMigrationController {

    protected static final Logger log = LoggerFactory.getLogger(ShoulderExtConstants.BACKSTAGE_BIZ_SERVICE_LOGGER);

    @Autowired
    private ConfigManagerCoreService configManagerCoreService;

    @Autowired
    private ConfigQueryCoreService configQueryCoreService;

    @Autowired
    private OldConfigDataQueryService oldConfigDataQueryService;

    /**
     * 迁移全部
     *
     * @param overwrite       覆盖
     * @param deleteAllExists 迁移前先删除
     * @return 迁移结果
     */
    @RequestMapping("all")
    @ResponseBody
    public BaseResult<Map<String, String>> migrationAll(@RequestParam(name = "overwrite", required = false, defaultValue = "false") Boolean overwrite,
                                                        @RequestParam(name = "deleteAllExists", required = false, defaultValue = "false") Boolean deleteAllExists) {
        Map<String, String> result = new TreeMap<>();
        for (String tenant : TenantEnum.values()) {
            for (ConfigType configType : ConfigType.values()) {
                BaseResult<String> r = migration(tenant, configType.getConfigName(), overwrite, deleteAllExists);
                result.put(configType.getConfigName() + "#" + tenant, r.getData());
            }
        }
        return BaseResult.success(result);
    }

    /**
     * 迁移
     *
     * @param overwrite       覆盖
     * @param deleteAllExists 迁移前先删除
     * @return 迁移结果
     */
    @RequestMapping("")
    @ResponseBody
    public BaseResult<String> migration(@RequestParam("tenant") String tenant, @RequestParam("configType") String configTypeName,
                                        @RequestParam(name = "overwrite", required = false, defaultValue = "false") Boolean overwrite,
                                        @RequestParam(name = "deleteAllExists", required = false, defaultValue = "false") Boolean deleteAllExists
    ) {
        ConfigType configType = ConfigType.getByName(configTypeName);
        try {

            if (deleteAllExists) {
                log.info("delete All exists");
                List<ConfigData> exists = configQueryCoreService.queryListByTenantAndConfigName(tenant, configType);
                exists.forEach(configManagerCoreService::delete);
            }
            // 获取迁移之前系统的数据
            List<Object> oldDataList = queryOldDataList(tenant, configTypeName);
            String configTypeId = configType.getConfigName() + "#" + tenant;
            log.info(configTypeId + " result size:" + (CollectionUtils.isNotEmpty(oldDataList) ? oldDataList.size() : 0));
            if (CollectionUtils.isEmpty(oldDataList)) {
                return BaseResult.success("empty");
            }
            int success = 0;
            for (int i = 0; i < oldDataList.size(); i++) {
                Object c = oldDataList.get(i);
                if (c == null) {
                    continue;
                }
                try {
                    log.info(configTypeId + "-" + i + " from configCenter: " + JsonUtils.toJson(c));
                    ConfigData configData = new ConfigData(tenant, c);
                    configData.setOperatorNo("MIGRATION");
                    configData.setOperatorName("MIGRATION");
                    configManagerCoreService.migration(configData, overwrite);
                    success++;
                    log.error(configTypeId + "-" + i + " SUCCESS: " + configData.getBizId());
                } catch (ConstraintViolationException e) {
                    log.error(configTypeId + "-" + i + " JSR303_ERROR. " + ValidateUtil.toValidationStr(e), e);
                } catch (ConfigException e) {
                    if (StringUtils.equals(e.getCode(), ConfigErrorCodeEnum.CONFIG_DATA_ALREADY_EXISTS.getCode())) {
                        log.error(configTypeId + "-" + i + JsonUtils.toJson(c) + e.getMessage());
                    } else {
                        log.error(configTypeId + "-" + i + e.getMessage(), e);
                    }
                } catch (Exception e) {
                    log.error(configTypeId + "-" + i + " FAIL: " + JsonUtils.toJson(c), e);
                }
            }
            return BaseResult.success(success + "/" + oldDataList.size());
        } catch (Exception e) {
            throw new ConfigException(e, CommonErrorCodeEnum.UNKNOWN);
        }
    }


    @RequestMapping("check")
    @ResponseBody
    public BaseResult<String> check(@RequestParam("tenant") String tenant, @RequestParam("configType") String configTypeName) {
        ConfigType configType = ConfigType.getByName(configTypeName);
        String configTypeId = configType.getConfigName() + "#" + tenant;
        try {
            List<Object> oldDataList = queryOldDataList(tenant, configTypeName);
            List<ConfigData> configDataList = configQueryCoreService.queryListByTenantAndConfigName(tenant, configType);
            if (CollectionUtils.isEmpty(oldDataList)) {
                return BaseResult.success("SUCCESS! center empty");
            }

            Map<String, Map<String, String>> configDataMapFromDb = configDataList.stream().collect(Collectors.toMap(ConfigData::getBizId,
                    ConfigData::getBusinessValue));
            int dbCount = configDataList.size();
            int centerCount = oldDataList.size();
            if (dbCount < centerCount) {
                log.error(configTypeId + " ERROR! current: " + dbCount + " | old: " + centerCount);
            }
            // old 的 db 都得有
            int success = 0;
            int index = 0;
            for (Object c : oldDataList) {
                ConfigData configData = new ConfigData(tenant, c);
                Map<String, String> remote = configData.getBusinessValue();
                Map<String, String> db = configDataMapFromDb.get(configData.getBizId());
                if (db == null) {
                    log.error(configTypeId + "-" + index + " NOT EXIST! bizId=" + configData.getBizId());
                } else {
                    // 比较 db 是否是 remote 的超集
                    AtomicReference<Boolean> hasError = new AtomicReference<>(false);
                    remote.forEach((k, v) -> {
                        String dbValue = db.get(k);
                        if (!StringUtils.equals(v, dbValue)) {
                            hasError.set(true);
                        }
                    });
                    if (hasError.get()) {
                        log.error(configTypeId + "-" + index + " NOT VALID! bizId=" + configData.getBizId() + " remote: " + JsonUtils.toJson(remote));
                    } else {
                        success++;
                    }
                }
                index++;
            }
            boolean right = index == success;
            return BaseResult.success((right ? "SUCCESS" : "FAIL") + " center: " + centerCount + " | success: " + success);
        } catch (ConstraintViolationException e) {
            return BaseResult.success("FAIL by validation " + ValidateUtil.toValidationStr(e));
        } catch (Exception e) {
            log.error(configTypeId + " FAIL! ", e);
            return BaseResult.success("FAIL by UNKNOWN " + e.getMessage());
        }
    }


    @RequestMapping("check/all")
    @ResponseBody
    public BaseResult<Map<String, String>> checkAll() {
        Map<String, String> result = new TreeMap<>();
        for (String tenant : TenantEnum.values()) {
            for (ConfigType configType : ConfigType.values()) {
                BaseResult<String> r = check(tenant, configType.getConfigName());
                result.put(configType.getConfigName() + "#" + tenant, r.getData());
            }
        }
        return BaseResult.success(result);
    }

    protected List<Object> queryOldDataList(String tenant, String configTypeName) {
        return oldConfigDataQueryService.queryOldDataList(tenant, configTypeName);
    }

}
