package org.shoulder.ext.config.converter;

import org.apache.commons.collections4.MapUtils;
import org.shoulder.core.converter.BaseDataConverter;
import org.shoulder.core.util.JsonUtils;
import org.shoulder.ext.config.dal.dataobject.ConfigHistoryDO;
import org.shoulder.ext.config.domain.model.ConfigHistoryLog;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;

/**
 * converter
 *
 * @author lym
 */
@Service
public class ConfigHistory2ConfigHistoryDOConverter extends BaseDataConverter<ConfigHistoryLog, ConfigHistoryDO> {

    @Override
    public void doConvert(@Nonnull ConfigHistoryLog sourceModel, @Nonnull ConfigHistoryDO targetModel) {
        targetModel.setConfigBizId(sourceModel.getConfigBizId());
        targetModel.setVersion(sourceModel.getVersion());
        targetModel.setOperation(conversionService.convert(sourceModel.getOperation(), String.class));
        if(MapUtils.isNotEmpty(sourceModel.getBusinessValue())){
            // 删除时可能为空
            targetModel.setBusinessValue(JsonUtils.toJson(sourceModel.getBusinessValue()));
        }
    }
}
