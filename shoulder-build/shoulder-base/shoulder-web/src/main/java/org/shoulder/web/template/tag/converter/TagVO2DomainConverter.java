/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2021 All Rights Reserved.
 */
package org.shoulder.web.template.tag.converter;

import org.shoulder.web.template.crud.AbstractVODataConverter;
import org.shoulder.web.template.tag.dto.TagDTO;
import org.shoulder.web.template.tag.entity.TagEntity;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;

/**
 * Tag VO -> domain
 *
 * @author lym
 */
@Service
public class TagVO2DomainConverter extends AbstractVODataConverter<TagDTO, TagEntity> {

    @Override
    public void doConvert(@Nonnull TagDTO sourceModel, @Nonnull TagEntity targetModel) {
        targetModel.setId(conversionService.convert(sourceModel.getId(), Long.class));
        targetModel.setBizType(sourceModel.getBizType());
        targetModel.setName(sourceModel.getDisplayName());
    }
}
