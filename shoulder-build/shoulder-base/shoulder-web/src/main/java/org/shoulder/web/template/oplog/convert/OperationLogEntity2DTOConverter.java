package org.shoulder.web.template.oplog.convert;

import cn.hutool.core.bean.BeanUtil;
import org.shoulder.log.operation.model.OperationLogDTO;
import org.shoulder.web.template.crud.AbstractDTODataConverter;
import org.shoulder.web.template.oplog.model.OperationLogEntity;

import javax.annotation.Nonnull;

/**
 * DTO -> domain
 *
 * @author lym
 */
public class OperationLogEntity2DTOConverter extends AbstractDTODataConverter<OperationLogEntity, OperationLogDTO> {

    public static final OperationLogEntity2DTOConverter INSTANCE = new OperationLogEntity2DTOConverter();

    @Override
    public void doConvert(@Nonnull OperationLogEntity sourceModel, @Nonnull OperationLogDTO targetModel) {
        // todo get/set
        BeanUtil.copyProperties(sourceModel, targetModel);
    }

}
