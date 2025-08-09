package org.shoulder.ext.config.converter;

import org.apache.commons.lang3.StringUtils;
import org.shoulder.core.converter.BaseDataConverter;
import org.shoulder.core.util.JsonUtils;
import org.shoulder.ext.config.dal.dataobject.ConfigDataDO;
import org.shoulder.ext.config.domain.model.ConfigData;
import org.springframework.stereotype.Service;

import jakarta.annotation.Nonnull;


/**
 * converter
 *
 * @author lym
 */
@Service
public class ConfigDataDO2DomainConverter extends BaseDataConverter<ConfigDataDO, ConfigData> {

    @Override
    public void doConvert(@Nonnull ConfigDataDO sourceModel, @Nonnull ConfigData targetModel) {
        targetModel.setCreateTime(sourceModel.getCreateTime());
        targetModel.setUpdateTime(sourceModel.getUpdateTime());
        targetModel.setTenant(sourceModel.getTenant());
        // todo
        //targetModel.setConfigType(conversionService.convert(sourceModel.getType(), ConfigTypeEnum.class));
        targetModel.setBizId(sourceModel.getBizId());
        targetModel.setVersion(sourceModel.getVersion());
        if(StringUtils.isNotBlank(sourceModel.getBusinessValue())){
            targetModel.setConfigObj(JsonUtils.parseObject(sourceModel.getBusinessValue(), targetModel.getConfigType().getClazz()));
            targetModel.setBusinessValue(ConfigData.extractFieldsFromConfigObject(targetModel.getConfigObj()));
        }
    }
}
