package org.shoulder.ext.config.web.controller;

import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.shoulder.core.context.AppContext;
import org.shoulder.core.dto.response.BaseResult;
import org.shoulder.core.dto.response.ListResult;
import org.shoulder.core.dto.response.PageResult;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;
import org.shoulder.core.util.AssertUtils;
import org.shoulder.core.util.JsonUtils;
import org.shoulder.core.util.StringUtils;
import org.shoulder.ext.common.constant.ShoulderExtConstants;
import org.shoulder.ext.config.domain.ConfigType;
import org.shoulder.ext.config.domain.PageInfo;
import org.shoulder.ext.config.domain.enums.ConfigErrorCodeEnum;
import org.shoulder.ext.config.domain.enums.TenantEnum;
import org.shoulder.ext.config.domain.ex.ConfigException;
import org.shoulder.ext.config.domain.model.ConfigData;
import org.shoulder.ext.config.domain.model.ConfigFieldInfo;
import org.shoulder.ext.config.provider.mvc.dto.ConfigDataDTO;
import org.shoulder.ext.config.provider.mvc.dto.ConfigFieldInfoDTO;
import org.shoulder.ext.config.provider.mvc.dto.ConfigItemDTO;
import org.shoulder.ext.config.provider.mvc.dto.ConfigTypeDTO;
import org.shoulder.ext.config.provider.mvc.dto.request.ConfigCreateRequest;
import org.shoulder.ext.config.provider.mvc.dto.request.ConfigDeleteRequest;
import org.shoulder.ext.config.provider.mvc.dto.request.ConfigPageQueryRequest;
import org.shoulder.ext.config.provider.mvc.dto.request.ConfigUpdateRequest;
import org.shoulder.ext.config.service.ConfigManagerCoreService;
import org.shoulder.ext.config.service.ConfigQueryCoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nonnull;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author lym
 */
@Validated
@RestControllerAdvice
@Controller
@RequestMapping(ShoulderExtConstants.CONFIG_URL_PREFIX)
public class ConfigDataController {

    protected static final Logger log = LoggerFactory.getLogger(ShoulderExtConstants.BACKSTAGE_BIZ_SERVICE_LOGGER);

    // ============================= MetaInfo ==============================

    private static final BaseResult<ListResult<ConfigTypeDTO>> CONFIG_TYPE_LIST;

    static {
        List<ConfigTypeDTO> configTypeDTOList = ConfigType.values().stream()
                .map(configType -> new ConfigTypeDTO(configType.getConfigName(), configType.getDescription()))
                .collect(Collectors.toList());
        CONFIG_TYPE_LIST = BaseResult.success(configTypeDTOList);
    }

    @Autowired
    private ConfigManagerCoreService configManagerCoreService;

    @Autowired
    private ConfigQueryCoreService configQueryCoreService;


    // todo 考虑根据租户返回可使用的 configType
    @GetMapping("queryConfigTypeNameList")
    @ResponseBody
    public BaseResult<ListResult<ConfigTypeDTO>> queryConfigTypeNameList() {
        return CONFIG_TYPE_LIST;
    }

    /**
     * Query config field list result.
     *
     * @return the result
     */
    @GetMapping("queryConfigFieldList")
    @ResponseBody
    public BaseResult<ListResult<ConfigFieldInfoDTO>> queryConfigFieldList(@NotNull @RequestParam("configType") String configType) {
        List<ConfigFieldInfo> fieldInfoList = ConfigType.getByName(configType).getFieldInfoList();
        List<ConfigFieldInfoDTO> dtoList = fieldInfoList.stream().map(this::convertToDTO).collect(Collectors.toList());
        return BaseResult.success(dtoList);
    }

    private ConfigFieldInfoDTO convertToDTO(ConfigFieldInfo configFieldInfo) {
        ConfigFieldInfoDTO dto = new ConfigFieldInfoDTO();
        dto.setOrder(configFieldInfo.getOrderNum());
        dto.setName(configFieldInfo.getName());
        dto.setDisplayName(configFieldInfo.getDisplayName());
        dto.setType(configFieldInfo.getType().getTypeName());
        dto.setIndex(configFieldInfo.isIndex());
        dto.setNotEmpty(configFieldInfo.isNotNull());
        dto.setNotBlank(configFieldInfo.isNotBlank());
        dto.setMinLength(configFieldInfo.getMinLength());
        dto.setMaxLength(configFieldInfo.getMaxLength());
        dto.setDescription(configFieldInfo.getDescription());
        dto.setDefaultValue(configFieldInfo.getDefaultValue());
        return dto;
    }

    /** ============================================ Manager ===================================================== */

    /**
     * Add result.
     *
     * @return the result
     */
    @PostMapping("create")
    @ResponseBody
    public BaseResult<Void> create(@Valid @NotNull @RequestBody ConfigCreateRequest request) {

        // 从请求中拿出 Config Data

        ConfigType configType = ConfigType.getByName(request.getConfigType());

        ConfigData configData = analyzeConfigData(request.getTenant(), configType, request.getData()::get);
        configData.setOperatorNo(AppContext.getUserId());
        configData.setOperatorName(AppContext.getUserName());
        configManagerCoreService.insert(configData);

        return BaseResult.success();
    }

    /**
     * Update result.
     *
     * @return the result
     */
    @PostMapping("update")
    @ResponseBody
    public BaseResult<Void> update(@Valid @NotNull @RequestBody ConfigUpdateRequest request) {
        // 数据库中存在且 version 正确
        ConfigData configDataInDb = configQueryCoreService.lockByBizId(request.getBizId());
        AssertUtils.notNull(configDataInDb, ConfigErrorCodeEnum.CONFIG_DATA_NOT_EXISTS);
        AssertUtils.isTrue(configDataInDb.getVersion() == request.getVersion(), ConfigErrorCodeEnum.DATA_VERSION_EXPIRED);

        // 从请求中拿出 Config Data
        String tenant = configDataInDb.getTenant();
        ConfigType configType = configDataInDb.getConfigType();
        Map<String, String> newFields = extractFieldsMapFromRequest(configType, request.getData()::get);

        // merge fields
        configDataInDb.getBusinessValue().forEach(newFields::putIfAbsent);

        // create
        ConfigData newConfigData = new ConfigData(tenant, configType, newFields);
        newConfigData.setOperatorNo(AppContext.getUserId());
        newConfigData.setOperatorName(AppContext.getUserName());

        // 校验 bizId 未改变（index字段不允许修改）
        AssertUtils.equals(configDataInDb.getBizId(), newConfigData.getBizId(), ConfigErrorCodeEnum.CONFIG_DATA_BIZ_ID_CHANGED);

        // 赋值到 db 数据
        configDataInDb.setConfigObj(newConfigData.getConfigObj());
        configDataInDb.setBusinessValue(newConfigData.getBusinessValue());
        configDataInDb.setOperatorNo(newConfigData.getOperatorNo());
        configDataInDb.setOperatorName(newConfigData.getOperatorName());

        // 更新
        configManagerCoreService.update(configDataInDb);

        return BaseResult.success();
    }

    /**
     * Delete result.
     *
     * @return the result 删除成功的
     */
    @PostMapping("delete")
    @ResponseBody
    public BaseResult<ListResult<ConfigItemDTO>> delete(@Valid @NotNull @RequestBody ConfigDeleteRequest deleteRequest) {
        // configItemList 循环删除
        List<ConfigItemDTO> configItemList = deleteRequest.getConfigItemList();
        for (ConfigItemDTO item : configItemList) {
            ConfigData configData = new ConfigData();
            configData.setBizId(item.getBizId());
            configData.setVersion(item.getVersion());
            configData.setOperatorNo(AppContext.getUserId());
            configData.setOperatorName(AppContext.getUserName());
            // 删除
            item.setSuccess(configManagerCoreService.delete(configData));
        }

        return BaseResult.success(configItemList);
    }

    /**
     * 从请求中解析出 configData
     */
    private ConfigData analyzeConfigData(String tenant, ConfigType configType,
                                         Function<String, String> fieldValueFunction) {
        // 组装 configData
        Map<String, String> fieldMap = extractFieldsMapFromRequest(configType, fieldValueFunction);
        return new ConfigData(tenant, configType, fieldMap);
    }

    /**
     * 仅提取合法属性
     */
    @Nonnull
    private static Map<String, String> extractFieldsMapFromRequest(ConfigType configType,
                                                                   Function<String, String> fieldValueFunction) {
        return configType.getFieldInfoList().stream()
                .map(ConfigFieldInfo::getName)
                .filter(fieldName -> fieldValueFunction.apply(fieldName) != null)
                .collect(Collectors.toMap(k -> k, fieldValueFunction, (v1, v2) -> v1));
    }

    /** ============================================ Query ===================================================== */

    /**
     * Page query result.
     * todo xss
     *
     * @return the result
     */
    //@SkipXss
    @GetMapping("pageQuery")
    @ResponseBody
    public BaseResult<PageResult<ConfigDataDTO>> pageQuery(@Valid @NotNull ConfigPageQueryRequest request) {
        ConfigType configType = ConfigType.getByName(request.getConfigType());
        PageInfo<ConfigData> configDataPage = configQueryCoreService.queryPageByTenantAndConfigName(request.getTenant(), configType,
                request.getPageNo(), request.getPageSize());
        List<ConfigDataDTO> dtoList = configDataPage.getData().stream().map(this::convertToDTO).collect(Collectors.toList());

        return BaseResult.success(PageResult.build(dtoList, configDataPage.getPageNo(), configDataPage.getPageSize(),
                configDataPage.getTotalCount()));
    }

    public ConfigDataDTO convertToDTO(ConfigData configData) {
        if (configData == null) {
            return null;
        }
        ConfigDataDTO dto = new ConfigDataDTO();
        dto.setBizId(configData.getBizId());
        dto.setVersion(configData.getVersion());
        dto.setTenant(configData.getTenant());
        dto.setConfigType(configData.getConfigType().getConfigName());
        dto.setBusinessValue(configData.getBusinessValue());
        dto.setOperatorNo(configData.getOperatorNo());
        dto.setOperatorName(configData.getOperatorName());
        dto.setLastModifyTime(configData.getModifyTime());
        return dto;
    }

    /**
     * ============================================ setter =====================================================
     */

    /**
     * Setter method for property <tt>configManagerCoreService</tt>.
     *
     * @param configManagerCoreService value to be assigned to property configManagerCoreService
     */
    public void setConfigManagerCoreService(ConfigManagerCoreService configManagerCoreService) {
        this.configManagerCoreService = configManagerCoreService;
    }

    /**
     * Setter method for property <tt>configQueryCoreService</tt>.
     *
     * @param configQueryCoreService value to be assigned to property configQueryCoreService
     */
    public void setConfigQueryCoreService(ConfigQueryCoreService configQueryCoreService) {
        this.configQueryCoreService = configQueryCoreService;
    }

    /**
     * ======================================================= migration =================================================================
     */

    @RequestMapping("migration")
    @ResponseBody
    public BaseResult<String> migration(@RequestParam("tenant") String tenant, @RequestParam("configType") String configTypeName,
                                        @RequestHeader(name = "overwrite", required = false, defaultValue = "false") Boolean overwrite,
                                        @RequestHeader(name = "deleteAllExists", required = false, defaultValue = "false") Boolean deleteAllExists
    ) {
        ConfigType configType = ConfigType.getByName(configTypeName);
        try {

            if (deleteAllExists) {
                log.info("delete All exists");
                List<ConfigData> exists = configQueryCoreService.queryListByTenantAndConfigName(tenant, configType);
                exists.forEach(configManagerCoreService::delete);
            }
            List oldDataList = new ArrayList();
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
                    log.error(configTypeId + "-" + i + " JSR303_ERROR. " + toValidationStr(e), e);
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

    private String toValidationStr(ConstraintViolationException e) {
        Set<ConstraintViolation<?>> constraintViolations = e.getConstraintViolations();
        List<String> messages = Lists.newArrayList();
        for (ConstraintViolation<?> constraintViolation : constraintViolations) {
            messages.add(constraintViolation.getPropertyPath().toString() + " " + constraintViolation.getMessage());
        }
        return StringUtils.join(messages, ",");
    }

    @RequestMapping("check")
    @ResponseBody
    public BaseResult<String> check(@RequestParam("tenant") String tenant, @RequestParam("configType") String configTypeName) {
        ConfigType configType = ConfigType.getByName(configTypeName);
        String configTypeId = configType.getConfigName() + "#" + tenant;

        try {

            List oldDataList = new ArrayList();
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
            return BaseResult.success("FAIL by validation " + toValidationStr(e));
        } catch (Exception e) {
            log.error(configTypeId + " FAIL! ", e);
            return BaseResult.success("FAIL by UNKNOWN " + e.getMessage());
        }
    }

    @RequestMapping("migrationAll")
    @ResponseBody
    public BaseResult<Map<String, String>> migrationAll(@RequestHeader(name = "overwrite", required = false, defaultValue = "false") Boolean overwrite,
                                                        @RequestHeader(name = "deleteAllExists", required = false, defaultValue = "false") Boolean deleteAllExists) {
        Map<String, String> result = new TreeMap<>();
        for (String tenant : TenantEnum.values()) {
            for (ConfigType configType : ConfigType.values()) {
                BaseResult<String> r = migration(tenant, configType.getConfigName(), overwrite, deleteAllExists);
                result.put(configType.getConfigName() + "#" + tenant, r.getData());
            }
        }
        return BaseResult.success(result);
    }

    @RequestMapping("checkAll")
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

    @RequestMapping("list")
    @ResponseBody
    public List<Map<String, Object>> list(@RequestParam(required = false) String status) {
        List<Map<String, Object>> r = new ArrayList<>();

        for (String tenant : TenantEnum.values()) {
            for (ConfigType configType : ConfigType.values()) {
                try {
                    List oldDataList = new ArrayList();
                    int num = oldDataList == null ? 0 : oldDataList.size();
                    if ("success".equals(status) && oldDataList == null) {
                        continue;
                    } else if ("fail".equals(status) && oldDataList != null) {
                        continue;
                    }
                    Map<String, Object> m = new HashMap<>(1);
                    m.put(configType.getConfigName() + "#" + tenant, num);
                    r.add(m);
                } catch (Exception e) {
                    throw new ConfigException(e, CommonErrorCodeEnum.UNKNOWN);
                }
            }
        }
        return r;

    }

}