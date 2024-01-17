
package org.shoulder.web.template.dictionary.base;


import jakarta.annotation.Nonnull;
import org.shoulder.core.converter.BaseDataConverter;
import org.shoulder.core.i18.Translator;
import org.shoulder.core.util.ContextUtils;
import org.shoulder.web.template.dictionary.dto.DictionaryItemDTO;
import org.shoulder.web.template.dictionary.model.ConfigAbleDictionaryItem;
import org.shoulder.web.template.dictionary.model.DictionaryItem;

import java.util.Optional;

/**
 * DictionaryItem domain -> VO
 *
 * @author lym
 */
public class DictionaryItemDomain2DTOConverter extends BaseDataConverter<DictionaryItem, DictionaryItemDTO> {

    private final Translator translator;

    public DictionaryItemDomain2DTOConverter(Translator translator) {
        this.translator = translator;
    }

    @Override
    public void doConvert(@Nonnull DictionaryItem sourceModel, @Nonnull DictionaryItemDTO targetModel) {
        if (sourceModel instanceof ConfigAbleDictionaryItem) {
//            String parentCode = ((ConfigAbleDictionaryItem) sourceModel).getParentCode();
//            targetModel.setParentCode(StringUtils.isBlank(parentCode) ? Constants.ZERO : parentCode);
        }

        targetModel.setCode(sourceModel.getItemId().toString());
        targetModel.setDictionaryType(sourceModel.getDictionaryType());
        String displayI18nName = Optional.ofNullable(ContextUtils.getBean(Translator.class))
                .map(t -> t.getMessage(sourceModel.getDisplayName(), new Object[0],
                        sourceModel.getDisplayName(), translator.currentLocale()))
                .orElse(sourceModel.getDisplayName());
        targetModel.setName(sourceModel.getName());
        targetModel.setDisplayName(displayI18nName);
        targetModel.setDisplayOrder(sourceModel.getDisplayOrder());
        targetModel.setNote(sourceModel.getNote());
    }
}
