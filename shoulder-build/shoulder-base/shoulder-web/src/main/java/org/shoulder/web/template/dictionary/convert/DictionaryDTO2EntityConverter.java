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
public class DictionaryDTO2EntityConverter extends AbstractVODataConverter<DictionaryTypeDTO, DictionaryTypeEntity> {

    @Override
    public void doConvert(@Nonnull DictionaryTypeDTO sourceModel, @Nonnull DictionaryTypeEntity targetModel) {
        //date version creator 不转换
        //targetModel.setId(conversionService.convert(sourceModel.getId(), Long.class));
        /*targetModel.setVersion(sourceModel.getVersion());
        targetModel.setTenant(sourceModel.getTenant());
        targetModel.setDisplayOrder(sourceModel.getDisplayOrder());
        targetModel.setBizId(sourceModel.getBizId());
        targetModel.setName(sourceModel.getName());
        targetModel.setType(sourceModel.getType());
        targetModel.setDisplayName(sourceModel.getDisplayName());
        targetModel.setIcon(sourceModel.getIcon());
        targetModel.setSource(sourceModel.getSource());
        targetModel.setDescription(sourceModel.getDescription());*/
    }

}
