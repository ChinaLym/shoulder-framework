package org.shoulder.ext.config.converter;

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
public class ConfigDataDomain2DOConverter extends BaseDataConverter<ConfigData, ConfigDataDO> {

    @Override
    public void doConvert(@Nonnull ConfigData sourceModel, @Nonnull ConfigDataDO targetModel) {
        targetModel.setCreateTime(sourceModel.getCreateTime());
        targetModel.setUpdateTime(sourceModel.getUpdateTime());
        targetModel.setTenant(conversionService.convert(sourceModel.getTenant(), String.class));
        targetModel.setType(conversionService.convert(sourceModel.getConfigType(), String.class));
        targetModel.setBizId(sourceModel.getBizId());
        targetModel.setVersion(sourceModel.getVersion());
        if(sourceModel.getConfigObj() != null) {
            targetModel.setBusinessValue(JsonUtils.toJson(sourceModel.getConfigObj()));
        }
    }
}
