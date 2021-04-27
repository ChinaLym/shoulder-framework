package org.shoulder.ext.config.provider.controller;

import org.shoulder.core.dto.response.BaseResult;
import org.shoulder.core.dto.response.ListResult;
import org.shoulder.core.dto.response.PageResult;
import org.shoulder.ext.common.constant.ShoulderExtConstants;
import org.shoulder.ext.config.domain.ConfigType;
import org.shoulder.ext.config.domain.PageInfo;
import org.shoulder.ext.config.domain.model.ConfigData;
import org.shoulder.ext.config.domain.model.ConfigFieldInfo;
import org.shoulder.ext.config.provider.dto.ConfigDataDTO;
import org.shoulder.ext.config.provider.dto.ConfigFieldInfoDTO;
import org.shoulder.ext.config.provider.dto.ConfigTypeDTO;
import org.shoulder.ext.config.provider.dto.request.ConfigPageQueryRequest;
import org.shoulder.ext.config.service.ConfigManagerCoreService;
import org.shoulder.ext.config.service.ConfigQueryCoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 配置查询接口
 *
 * @author lym
 */
@Validated
@RestControllerAdvice
@Controller
@RequestMapping(ShoulderExtConstants.CONFIG_URL_PREFIX)
public class ConfigDataQueryController {

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


    /**
     * 查询所有的配置项类型（下拉框）
     *
     * @return
     */
    // todo 考虑根据租户返回可使用的 configType
    @GetMapping("queryConfigTypeNameList")
    @ResponseBody
    public BaseResult<ListResult<ConfigTypeDTO>> queryConfigTypeNameList() {
        return CONFIG_TYPE_LIST;
    }

    /**
     * 查询某个配置类的字段信息（关系到页面样式）
     *
     * @return 查询结果
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

    /**
     * 分页查询
     * todo 该接口需要去掉 xss 防御
     *
     * @return 分页查询
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

}