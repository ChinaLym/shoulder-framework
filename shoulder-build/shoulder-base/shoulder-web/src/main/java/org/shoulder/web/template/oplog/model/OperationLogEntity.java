package org.shoulder.web.template.oplog.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.shoulder.data.mybatis.template.entity.BaseEntity;
import org.shoulder.log.operation.model.OperationLogDTO;

import java.time.Instant;

/**
 * opLog
 *
 * @author lym
 * @see OperationLogDTO
 */
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName("log_operation")
public class OperationLogEntity
    extends BaseEntity<Long> {

    private String  appId;
    private String  version;
    private String  instanceId;
    private String  userId;
    private String  userName;
    private String  userRealName;
    private String  userOrgId;
    private String  userOrgName;
    private Integer terminalType;
    private String  terminalAddress;
    private String  terminalId;
    private String  terminalInfo;
    private String  objectType;
    private String  objectId;
    private String  objectName;
    private String  operationParam;
    private String  operation;
    private String  detail;
    private String  detailI18nKey;
    private String  detailI18nItem;
    private Integer result;
    private String  errorCode;
    private Instant operationTime;
    private Instant endTime;
    private Long    duration;
    private String  traceId;
    private String  relationId;
    private String  tenantCode;
    //private Instant insertTime;
    private String  extendedField0;
    private String  extendedField1;
    private String  extendedField2;
    private String  extendedField3;
    private String  extendedField4;

}
