
package org.shoulder.web.template.dictionary.dto;


import jakarta.annotation.Nonnull;
import org.shoulder.web.template.dictionary.base.BaseDataConverter;
import org.shoulder.web.template.dictionary.model.ConfigAbleDictionaryItem;
import org.shoulder.web.template.dictionary.model.DictionaryItem;

/**
 * DictionaryItem domain -> VO
 *
 * @author lym
 */
public class DictionaryItemDomain2ConverDTOter extends BaseDataConverter<DictionaryItem, DictionaryItemDTO> {

    @Override
    public void doConvert(@Nonnull DictionaryItem sourceModel, @Nonnull DictionaryItemDTO targetModel) {
        if (sourceModel instanceof ConfigAbleDictionaryItem) {
//            String parentCode = ((ConfigAbleDictionaryItem) sourceModel).getParentCode();
//            targetModel.setParentCode(StringUtils.isBlank(parentCode) ? Constants.ZERO : parentCode);
        }

        targetModel.setBizType(sourceModel.getDictionaryType());
        targetModel.setDisplayName(sourceModel.getDisplayName());
        targetModel.setDisplayOrder(sourceModel.getDisplayOrder());
        targetModel.setNote(sourceModel.getNote());
    }
}