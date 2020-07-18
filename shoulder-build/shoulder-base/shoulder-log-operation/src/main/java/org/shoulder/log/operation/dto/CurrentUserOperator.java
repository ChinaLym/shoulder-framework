package org.shoulder.log.operation.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 当前登录的用户信息作为业务操作者
 * @author lym
 */
@Data
@Accessors(chain = true)
public class CurrentUserOperator implements Serializable, Operator {
	private static final long serialVersionUID = 1429478242021042150L;

	protected String userId;
	protected String personId;
	protected String ip;
	protected String terminalId;
	protected String tgc;
	protected String languageId;

	/** userId 必填 */
    public CurrentUserOperator(String userId) {
        this.userId = userId;
    }

    public CurrentUserOperator(String userId, String personId, String ip, String terminalId, String tgc, String languageId) {
        this.userId = userId;
        this.personId = personId;
        this.ip = ip;
        this.terminalId = terminalId;
        this.tgc = tgc;
        this.languageId = languageId;
    }

}
