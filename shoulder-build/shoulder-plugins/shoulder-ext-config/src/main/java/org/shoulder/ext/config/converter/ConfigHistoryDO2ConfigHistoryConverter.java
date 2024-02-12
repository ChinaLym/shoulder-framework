package org.shoulder.ext.config.converter;

import org.shoulder.core.converter.BaseDataConverter;
import org.shoulder.core.util.JsonUtils;
import org.shoulder.core.util.StringUtils;
import org.shoulder.ext.config.dal.dataobject.ConfigHistoryDO;
import org.shoulder.ext.config.domain.enums.ConfigOperationTypeEnum;
import org.shoulder.ext.config.domain.model.ConfigHistoryLog;
import org.springframework.stereotype.Service;

import java.util.Map;

import javax.annotation.Nonnull;

/**
 * converter
 *
 * @author lym
 */
@Service
public class ConfigHistoryDO2ConfigHistoryConverter extends BaseDataConverter<ConfigHistoryDO, ConfigHistoryLog> {

    @Override
    public void doConvert(@Nonnull ConfigHistoryDO sourceModel, @Nonnull ConfigHistoryLog targetModel) {
        targetModel.setConfigBizId(sourceModel.getConfigBizId());
        targetModel.setVersion(sourceModel.getVersion());
        targetModel.setOperation(conversionService.convert(sourceModel.getOperation(), ConfigOperationTypeEnum.class));
        if(StringUtils.isNotEmpty(sourceModel.getBusinessValue())){
            // 删除时可能为空
            targetModel.setBusinessValue(JsonUtils.parseObject(sourceModel.getBusinessValue(), Map.class, String.class, String.class));
        }
    }
}
