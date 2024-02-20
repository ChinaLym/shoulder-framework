
package org.shoulder.web.template.dictionary.convert;

import jakarta.annotation.Nonnull;
import org.shoulder.core.converter.BaseDataConverter;
import org.shoulder.core.dictionary.model.DictionaryItem;
import org.shoulder.core.i18.Translator;
import org.shoulder.web.template.dictionary.dto.DictionaryItemDTO;
import org.shoulder.web.template.dictionary.model.ConfigAbleDictionaryItem;

import java.util.Optional;

/**
 * DictionaryItem domain -> VO
 *
 * @author lym
 */
@SuppressWarnings("rawtypes")
public class DictionaryItemDomain2DTOConverter extends BaseDataConverter<DictionaryItem, DictionaryItemDTO> {

    public static DictionaryItemDomain2DTOConverter INSTANCE = new DictionaryItemDomain2DTOConverter(null);

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
        String displayI18nName = Optional.ofNullable(translator)
                .map(t -> t.getMessageOrDefault(sourceModel.getDisplayName(),
                        sourceModel.getDisplayName()))
                .orElse(sourceModel.getDisplayName());
        targetModel.setName(sourceModel.getName());
        targetModel.setDisplayName(displayI18nName);
        targetModel.setDisplayOrder(sourceModel.getDisplayOrder());
        targetModel.setDescription(sourceModel.getDescription());
    }
}
