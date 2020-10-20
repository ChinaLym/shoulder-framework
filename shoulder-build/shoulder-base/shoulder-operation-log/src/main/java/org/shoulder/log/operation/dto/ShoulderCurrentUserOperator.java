package org.shoulder.log.operation.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 当前登录的用户信息作为业务操作者
 *
 * @author lym
 * @implSpec 该类针对 shoulder 系统中单点登录用户信息进行封装
 */
@Data
@Accessors(chain = true)
public class ShoulderCurrentUserOperator implements Serializable, Operator {
    private static final long serialVersionUID = 1429478242021042150L;

    protected String userId;
    protected String userRealName;
    protected String ip;
    protected String terminalId;
    protected String tgc;
    protected String languageId;

    /**
     * userId 必填
     */
    public ShoulderCurrentUserOperator(String userId) {
        this.userId = userId;
    }

    public ShoulderCurrentUserOperator(String userId, String userRealName, String ip, String terminalId, String tgc, String languageId) {
        this.userId = userId;
        this.userRealName = userRealName;
        this.ip = ip;
        this.terminalId = terminalId;
        this.tgc = tgc;
        this.languageId = languageId;
    }

}
