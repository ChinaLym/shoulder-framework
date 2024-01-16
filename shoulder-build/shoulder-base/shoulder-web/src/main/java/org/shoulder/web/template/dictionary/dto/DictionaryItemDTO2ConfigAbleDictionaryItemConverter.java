
package org.shoulder.web.template.dictionary.dto;


import org.shoulder.core.converter.BaseDataConverter;
import org.shoulder.web.template.dictionary.model.ConfigAbleDictionaryItem;

import javax.annotation.Nonnull;

/**
 * DictionaryItemDTO -> 动态配置字典
 *
 * @author lym
 */
public class DictionaryItemDTO2ConfigAbleDictionaryItemConverter extends BaseDataConverter<DictionaryItemDTO, ConfigAbleDictionaryItem> {

    @Override
    public void doConvert(@Nonnull DictionaryItemDTO sourceModel, @Nonnull ConfigAbleDictionaryItem targetModel) {
        targetModel.setCode(sourceModel.getCode());
        // 为空或者为0，都代表一级节点
//        targetModel.setParentCode(StringUtils.isBlank(sourceModel.getParentCode()) ? Constants.ZERO : sourceModel.getParentCode());
        targetModel.setDictionaryType(sourceModel.getDictionaryType());
        targetModel.setDisplayName(sourceModel.getDisplayName());
        targetModel.setDisplayOrder(sourceModel.getDisplayOrder());
        targetModel.setNote(sourceModel.getNote());
    }
}
