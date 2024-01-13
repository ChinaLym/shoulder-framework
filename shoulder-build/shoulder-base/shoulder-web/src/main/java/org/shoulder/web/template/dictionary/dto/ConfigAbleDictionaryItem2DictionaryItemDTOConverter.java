
package org.shoulder.web.template.dictionary.dto;


import jakarta.annotation.Nonnull;
import org.shoulder.web.template.dictionary.base.BaseDataConverter;
import org.shoulder.web.template.dictionary.model.ConfigAbleDictionaryItem;
import org.springframework.stereotype.Component;

/**
 * @author feng wang
 */
@Component
public class ConfigAbleDictionaryItem2DictionaryItemDTOConverter extends BaseDataConverter<ConfigAbleDictionaryItem, DictionaryItemDTO> {

    @Override
    public void doConvert(@Nonnull ConfigAbleDictionaryItem sourceModel, @Nonnull DictionaryItemDTO targetModel) {
        targetModel.setCode(sourceModel.getCode());
        // 为空或者为0，都代表一级节点
//        targetModel.setParentCode(StringUtils.isBlank(sourceModel.getParentCode()) ? Constants.ZERO : sourceModel.getParentCode());
        targetModel.setBizType(sourceModel.getDictionaryType());
        targetModel.setDisplayName(sourceModel.getDisplayName());
        targetModel.setDisplayOrder(sourceModel.getDisplayOrder());
    }

}