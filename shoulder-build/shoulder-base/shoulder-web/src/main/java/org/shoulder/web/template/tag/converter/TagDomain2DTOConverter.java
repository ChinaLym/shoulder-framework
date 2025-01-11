package org.shoulder.web.template.tag.converter;

import org.shoulder.web.template.crud.AbstractVODataConverter;
import org.shoulder.web.template.tag.dto.TagDTO;
import org.shoulder.web.template.tag.model.TagEntity;

import javax.annotation.Nonnull;
import java.util.Date;

/**
 * Tag domain -> VO
 *
 * @author lym
 */
public class TagDomain2DTOConverter extends AbstractVODataConverter<TagEntity, TagDTO> {

    @Override
    public void doConvert(@Nonnull TagEntity sourceModel, @Nonnull TagDTO targetModel) {
        targetModel.setId(conversionService.convert(sourceModel.getId(), String.class));
        targetModel.setVersion(sourceModel.getVersion());
        targetModel.setDeleteVersion(sourceModel.getDeleteVersion());
        targetModel.setCreateTime(conversionService.convert(sourceModel.getCreateTime(), Date.class));
        targetModel.setUpdateTime(conversionService.convert(sourceModel.getUpdateTime(), Date.class));
        targetModel.setCreator(conversionService.convert(sourceModel.getCreator(), String.class));
        targetModel.setModifier(conversionService.convert(sourceModel.getModifier(), String.class));
        targetModel.setTenant(sourceModel.getTenant());
        targetModel.setTenant(sourceModel.getTenant());
        targetModel.setDisplayOrder(sourceModel.getDisplayOrder());
        targetModel.setBizId(sourceModel.getBizId());

        targetModel.setName(sourceModel.getName());
        // abstract 处理
        //targetModel.setCreator(sourceModel.getCreator());
        targetModel.setType(sourceModel.getType());
        // todo P2 i18n?
        targetModel.setDisplayName(sourceModel.getDisplayName());
        targetModel.setDisplayOrder(sourceModel.getDisplayOrder());
        targetModel.setIcon(sourceModel.getIcon());
        targetModel.setSource(sourceModel.getSource());
        targetModel.setDescription(sourceModel.getDescription());
    }

}
