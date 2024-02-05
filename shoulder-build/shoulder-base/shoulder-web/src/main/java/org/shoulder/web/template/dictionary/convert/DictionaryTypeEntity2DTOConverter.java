/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2021 All Rights Reserved.
 */
package org.shoulder.web.template.dictionary.convert;

import org.shoulder.web.template.crud.AbstractVODataConverter;
import org.shoulder.web.template.dictionary.dto.DictionaryTypeDTO;
import org.shoulder.web.template.dictionary.model.DictionaryTypeEntity;

import javax.annotation.Nonnull;

/**
 * Tag VO -> domain
 *
 * @author lym
 */
public class DictionaryTypeEntity2DTOConverter extends AbstractVODataConverter<DictionaryTypeEntity, DictionaryTypeDTO> {

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
        //targetModel.setTenant(sourceModel.getTenant());
        //targetModel.setIcon(sourceModel.getIcon());
    }

}
