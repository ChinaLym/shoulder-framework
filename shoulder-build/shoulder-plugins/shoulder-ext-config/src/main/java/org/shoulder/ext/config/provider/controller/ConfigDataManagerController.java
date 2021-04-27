package org.shoulder.ext.config.provider.controller;

import org.shoulder.core.context.AppContext;
import org.shoulder.core.dto.response.BaseResult;
import org.shoulder.core.dto.response.ListResult;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;
import org.shoulder.core.util.AssertUtils;
import org.shoulder.ext.common.constant.ShoulderExtConstants;
import org.shoulder.ext.config.domain.ConfigType;
import org.shoulder.ext.config.domain.enums.ConfigErrorCodeEnum;
import org.shoulder.ext.config.domain.model.ConfigData;
import org.shoulder.ext.config.domain.model.ConfigFieldInfo;
import org.shoulder.ext.config.provider.dto.ConfigItemDTO;
import org.shoulder.ext.config.provider.dto.request.ConfigCreateRequest;
import org.shoulder.ext.config.provider.dto.request.ConfigDeleteRequest;
import org.shoulder.ext.config.provider.dto.request.ConfigUpdateRequest;
import org.shoulder.ext.config.service.ConfigManagerCoreService;
import org.shoulder.ext.config.service.ConfigQueryCoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nonnull;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 配置管理接口
 *
 * @author lym
 */
@Validated
@RestControllerAdvice
@Controller
@RequestMapping(ShoulderExtConstants.CONFIG_URL_PREFIX)
public class ConfigDataManagerController {

    protected static final Logger log = LoggerFactory.getLogger(ShoulderExtConstants.BACKSTAGE_BIZ_SERVICE_LOGGER);

    @Autowired
    private ConfigManagerCoreService configManagerCoreService;

    @Autowired
    private ConfigQueryCoreService configQueryCoreService;


    /**
     * 新增 / 复制
     *
     * @return 创建结果
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
     * 更新
     *
     * @return 更新结果
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
     * 批量删除
     *
     * @return 每一项删除结果
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

}