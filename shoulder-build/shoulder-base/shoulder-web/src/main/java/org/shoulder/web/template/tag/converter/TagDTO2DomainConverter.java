package org.shoulder.web.template.tag.converter;

import org.shoulder.web.template.crud.AbstractVODataConverter;
import org.shoulder.web.template.tag.dto.TagDTO;
import org.shoulder.web.template.tag.model.TagEntity;

import javax.annotation.Nonnull;

/**
 * Tag VO -> domain
 *
 * @author lym
 */
public class TagDTO2DomainConverter extends AbstractVODataConverter<TagDTO, TagEntity> {

    @Override
    public void doConvert(@Nonnull TagDTO sourceModel, @Nonnull TagEntity targetModel) {
        //date version creator 不转换
        //targetModel.setId(conversionService.convert(sourceModel.getId(), Long.class));
        targetModel.setVersion(sourceModel.getVersion());
        targetModel.setTenant(sourceModel.getTenant());
        targetModel.setDisplayOrder(sourceModel.getDisplayOrder());
        targetModel.setBizId(sourceModel.getBizId());
        targetModel.setName(sourceModel.getName());
        targetModel.setType(sourceModel.getType());
        targetModel.setDisplayName(sourceModel.getDisplayName());
        targetModel.setIcon(sourceModel.getIcon());
        targetModel.setSource(sourceModel.getSource());
        targetModel.setDescription(sourceModel.getDescription());
    }

}
