package org.shoulder.log.operation.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.shoulder.log.operation.enums.TerminalType;

import java.io.Serial;
import java.io.Serializable;

/**
 * 当前登录的用户信息作为业务操作者
 *
 * @author lym
 * @implSpec 该类针对 shoulder 系统中单点登录用户信息进行封装
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ShoulderCurrentUserOperator implements Serializable, Operator {
    @Serial private static final long serialVersionUID = 1429478242021042150L;

    protected String userId;
    protected String userRealName;
    protected String terminalAddress;
    protected TerminalType terminalType = TerminalType.UNKNOWN;
    protected String terminalId;
    protected String terminalInfo;

    /**
     * userId 必填
     */
    public ShoulderCurrentUserOperator(String userId) {
        this.userId = userId;
    }

    /**
     * userId 必填
     */
    public ShoulderCurrentUserOperator(Operator operator) {
        this.userId = operator.getUserId();
        this.userRealName = operator.getUserName();
        this.userRealName = operator.getUserRealName();
        this.terminalAddress = operator.getTerminalAddress();
        this.terminalId = operator.getTerminalId();
        this.terminalInfo = operator.getTerminalInfo();
    }

}
