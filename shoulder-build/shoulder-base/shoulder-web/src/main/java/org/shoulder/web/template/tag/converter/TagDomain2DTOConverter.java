/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2021 All Rights Reserved.
 */
package org.shoulder.web.template.tag.converter;

import org.shoulder.web.template.crud.AbstractVODataConverter;
import org.shoulder.web.template.tag.dto.TagDTO;
import org.shoulder.web.template.tag.model.TagEntity;

import javax.annotation.Nonnull;

/**
 * Tag domain -> VO
 *
 * @author lym
 */
public class TagDomain2DTOConverter extends AbstractVODataConverter<TagEntity, TagDTO> {

    @Override
    public void doConvert(@Nonnull TagEntity sourceModel, @Nonnull TagDTO targetModel) {
        targetModel.setId(conversionService.convert(sourceModel.getId(), String.class));
        targetModel.setBizId(sourceModel.getBizId());
        targetModel.setName(sourceModel.getName());
        // abstract 处理
        //targetModel.setCreator(sourceModel.getCreator());
        targetModel.setType(sourceModel.getType());
        // todo i18n?
        targetModel.setDisplayName(sourceModel.getDisplayName());
        targetModel.setIcon(sourceModel.getIcon());
        targetModel.setDescription(sourceModel.getDescription());
    }

}
