
package org.shoulder.web.template.dictionary.dto;

import org.shoulder.web.template.dictionary.base.BaseDataConverter;
import org.shoulder.web.template.dictionary.model.ConfigAbleDictionaryItem;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * VO 模型转换器
 * 自动转换 id、version、创建、更新时间、创建、更新人id、名称
 * <p>
 * targetModel\.set(.*?)\(\)\;
 * targetModel.set$1(sourceModel.get$1());
 *
 * @author lym
 */
@SuppressWarnings("unchecked")
public abstract class AbstractVODataConverter<S, T> extends BaseDataConverter<S, T> {

    @Override
    protected void preHandle(S sourceModel, T targetModel) {
//        if (BaseVO.class.isAssignableFrom(sourceEntityClass) && BaseModel.class.isAssignableFrom(targetEntityClass)) {
//            // vo -> domain
//            BaseVO vo = (BaseVO) sourceModel;
//            BaseModel domain = (BaseModel) targetModel;
//            // version
//            domain.setId(conversionService.convert(vo.getId(), Long.class));
//            domain.setVersion(vo.getVersion());
//            // operator
//            domain.setCreator(conversionService.convert(vo.getCreator(), Operator.class));
//            domain.setModifier(conversionService.convert(vo.getModifier(), Operator.class));
//        }if (BaseModel.class.isAssignableFrom(sourceEntityClass) && BaseVO.class.isAssignableFrom(targetEntityClass)) {
//            // domain -> vo
//            BaseModel domain = (BaseModel) sourceModel;
//            BaseVO vo = (BaseVO) targetModel;
//
//            // id
//            vo.setId(conversionService.convert(domain.getId(), String.class));
//            vo.setVersion(domain.getVersion());
//            // time
//            vo.setCreateTime(conversionService.convert(domain.getGmtCreate(), Date.class));
//            vo.setUpdateTime(conversionService.convert(domain.getGmtModified(), Date.class));
//            // operator
//            vo.setCreator(conversionService.convert(domain.getCreator(), OperatorVO.class));
//            vo.setModifier(conversionService.convert(domain.getModifier(), OperatorVO.class));
//        }

    }

    protected ConfigAbleDictionaryItem convertToConfigAbleDictionaryItem(String dictionaryCode, DictionaryItemDTO dictionaryItemDTO) {
        return dictionaryItemDTO == null ? null
                : string2ConfigAbleDictionaryItemConverter.convertToConfigAbleDictionaryItem(dictionaryCode, dictionaryItemDTO.getCode());
    }

    protected List<ConfigAbleDictionaryItem> convertToConfigAbleDictionaryItem(String dictionaryCode, Collection<? extends DictionaryItemDTO> dictionaryItemDTOList) {
        return dictionaryItemDTOList == null ? null
                : dictionaryItemDTOList.stream().map(vo -> convertToConfigAbleDictionaryItem(dictionaryCode, vo))
                .collect(Collectors.toList());
    }

}
