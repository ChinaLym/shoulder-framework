package org.shoulder.web.template.dictionary.convert;

import org.shoulder.core.dictionary.model.DictionaryType;
import org.shoulder.web.template.crud.AbstractVODataConverter;
import org.shoulder.web.template.dictionary.dto.DictionaryTypeDTO;

import javax.annotation.Nonnull;

/**
 * Tag domain -> VO
 *
 * @author lym
 */
public class DictionaryTypeDomain2DTOConverter extends AbstractVODataConverter<DictionaryType, DictionaryTypeDTO> {

    public static final DictionaryTypeDomain2DTOConverter INSTANCE = new DictionaryTypeDomain2DTOConverter();


    @Override
    public void doConvert(@Nonnull DictionaryType sourceModel, @Nonnull DictionaryTypeDTO targetModel) {
        targetModel.setModifyAble(sourceModel.modifyAble());
        targetModel.setDisplayName(sourceModel.getDisplayName());
        targetModel.setDisplayOrder(sourceModel.getDisplayOrder());
        targetModel.setCode(sourceModel.getCode());
        //
        //targetModel.setId(conversionService.convert(sourceModel.getId(), String.class));
        //targetModel.setVersion(sourceModel.getVersion());
        //targetModel.setDeleteVersion(sourceModel.getDeleteVersion());
        //targetModel.setCreateTime(conversionService.convert(sourceModel.getCreateTime(), Date.class));
        //targetModel.setUpdateTime(conversionService.convert(sourceModel.getUpdateTime(), Date.class));
        //targetModel.setCreator(conversionService.convert(sourceModel.getCreator(), String.class));
        //targetModel.setModifier(conversionService.convert(sourceModel.getModifier(), String.class));
        //targetModel.setTenant(sourceModel.getTenant());
        //targetModel.setTenant(sourceModel.getTenant());
        //
        //targetModel.setBizId(sourceModel.getBizId());
        //
        //targetModel.setName(sourceModel.getName());
        //// abstract 处理
        ////targetModel.setCreator(sourceModel.getCreator());
        //targetModel.setType(sourceModel.getType());
        //// todo i18n?
        //targetModel.setDisplayName(sourceModel.getDisplayName());
        //targetModel.setDisplayOrder(sourceModel.getDisplayOrder());
        //targetModel.setIcon(sourceModel.getIcon());
        //targetModel.setSource(sourceModel.getSource());
        //targetModel.setDescription(sourceModel.getDescription());
    }

}
