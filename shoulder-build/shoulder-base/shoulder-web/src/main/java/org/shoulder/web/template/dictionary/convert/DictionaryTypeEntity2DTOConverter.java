package org.shoulder.web.template.dictionary.convert;

import org.shoulder.web.template.crud.AbstractDTODataConverter;
import org.shoulder.web.template.dictionary.dto.DictionaryTypeDTO;
import org.shoulder.web.template.dictionary.model.DictionaryTypeEntity;

import jakarta.annotation.Nonnull;

/**
 * Tag DTO -> domain
 *
 * @author lym
 */
public class DictionaryTypeEntity2DTOConverter extends AbstractDTODataConverter<DictionaryTypeEntity, DictionaryTypeDTO> {

    public static final DictionaryTypeEntity2DTOConverter INSTANCE = new DictionaryTypeEntity2DTOConverter();

    @Override
    public void doConvert(@Nonnull DictionaryTypeEntity sourceModel, @Nonnull DictionaryTypeDTO targetModel) {
        //date version creator 不转换
        targetModel.setId(conversionService.convert(sourceModel.getId(), String.class));
        targetModel.setVersion(sourceModel.getVersion());
        targetModel.setDisplayOrder(sourceModel.getDisplayOrder());
        targetModel.setCode(sourceModel.getCode());
        targetModel.setDisplayName(sourceModel.getDisplayName());
        targetModel.setSource(sourceModel.getSource());
        targetModel.setDescription(sourceModel.getDescription());
        targetModel.setModifyAble(sourceModel.modifyAble());
        //targetModel.setTenant(sourceModel.getTenant());
        //targetModel.setIcon(sourceModel.getIcon());
    }

}
