package org.shoulder.web.template.oplog.convert;

import cn.hutool.core.bean.BeanUtil;
import org.shoulder.log.operation.model.OperationLogDTO;
import org.shoulder.web.template.crud.AbstractVODataConverter;
import org.shoulder.web.template.oplog.model.OperationLogEntity;

import javax.annotation.Nonnull;

/**
 * VO -> domain
 *
 * @author lym
 */
public class OperationLogDTO2EntityConverter extends AbstractVODataConverter<OperationLogDTO, OperationLogEntity> {

    public static final OperationLogDTO2EntityConverter INSTANCE = new OperationLogDTO2EntityConverter();

    @Override
    public void doConvert(@Nonnull OperationLogDTO sourceModel, @Nonnull OperationLogEntity targetModel) {
        // todo get/set
        BeanUtil.copyProperties(sourceModel, targetModel);
    }

}
