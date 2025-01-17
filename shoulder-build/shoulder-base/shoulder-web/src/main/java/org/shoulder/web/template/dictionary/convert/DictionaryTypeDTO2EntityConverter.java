package org.shoulder.web.template.dictionary.convert;

import org.shoulder.web.template.crud.AbstractDTODataConverter;
import org.shoulder.web.template.dictionary.dto.DictionaryTypeDTO;
import org.shoulder.web.template.dictionary.model.DictionaryTypeEntity;

import javax.annotation.Nonnull;

/**
 * Tag DTO -> domain
 *
 * @author lym
 */
public class DictionaryTypeDTO2EntityConverter extends AbstractDTODataConverter<DictionaryTypeDTO, DictionaryTypeEntity> {

    public static final DictionaryTypeDTO2EntityConverter INSTANCE = new DictionaryTypeDTO2EntityConverter();

    @Override
    public void doConvert(@Nonnull DictionaryTypeDTO sourceModel, @Nonnull DictionaryTypeEntity targetModel) {
        //date version creator 不转换
        targetModel.setId(conversionService.convert(sourceModel.getId(), Long.class));
        targetModel.setVersion(sourceModel.getVersion());
        targetModel.setDisplayOrder(sourceModel.getDisplayOrder());
        targetModel.setCode(sourceModel.getCode());
        targetModel.setDisplayName(sourceModel.getDisplayName());
        targetModel.setSource(sourceModel.getSource());
        targetModel.setDescription(sourceModel.getDescription());
        //targetModel.setTenant(sourceModel.getTenant());
        //targetModel.setIcon(sourceModel.getIcon());
    }

}
