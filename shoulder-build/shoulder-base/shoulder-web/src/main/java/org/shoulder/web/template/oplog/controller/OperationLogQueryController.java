package org.shoulder.web.template.oplog.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.shoulder.core.converter.ShoulderConversionService;
import org.shoulder.log.operation.model.OperationLogDTO;
import org.shoulder.web.template.crud.BaseControllerImpl;
import org.shoulder.web.template.crud.QueryController;
import org.shoulder.web.template.oplog.model.OperationLogEntity;
import org.shoulder.web.template.oplog.service.OperationLogService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 操作日志查询 api
 *
 * @author lym
 */
@Tag(name = "操作日志查询-OperationLogQueryController")
@RestController
@RequestMapping(value = "${shoulder.web.ext.oplog.apiPath:/api/v1/oplogs}")
public class OperationLogQueryController extends BaseControllerImpl<OperationLogService, OperationLogEntity>
    implements QueryController<OperationLogEntity, Long, OperationLogDTO, OperationLogDTO> {

    public OperationLogQueryController(OperationLogService service, ShoulderConversionService conversionService) {
        super(service, conversionService);
    }
}
