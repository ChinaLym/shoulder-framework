package org.shoulder.web.template.oplog.controller;

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
@RestController
@RequestMapping(value = "${shoulder.web.ext.oplog.apiPath + '/type':/api/v1/oplog}")
public class OperationLogQueryController extends BaseControllerImpl<OperationLogService, OperationLogEntity>
    implements QueryController<OperationLogEntity, Long, OperationLogDTO, OperationLogDTO> {

}
