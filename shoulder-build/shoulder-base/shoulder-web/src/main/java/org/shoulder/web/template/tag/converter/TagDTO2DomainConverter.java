/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2021 All Rights Reserved.
 */
package org.shoulder.web.template.tag.converter;

import org.shoulder.web.template.crud.AbstractVODataConverter;
import org.shoulder.web.template.tag.dto.TagDTO;
import org.shoulder.web.template.tag.model.TagEntity;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;

/**
 * Tag VO -> domain
 *
 * @author lym
 */
@Service
public class TagDTO2DomainConverter extends AbstractVODataConverter<TagDTO, TagEntity> {

    @Override
    public void doConvert(@Nonnull TagDTO sourceModel, @Nonnull TagEntity targetModel) {
        targetModel.setId(conversionService.convert(sourceModel.getId(), Long.class));
        targetModel.setType(sourceModel.getBizType());
        targetModel.setName(sourceModel.getDisplayName());
    }
}
