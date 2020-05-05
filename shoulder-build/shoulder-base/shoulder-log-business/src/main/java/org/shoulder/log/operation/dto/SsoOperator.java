package org.shoulder.log.operation.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 单点登录的用户信息
 * @author lym
 */
@Data
@Accessors(chain = true)
public class SsoOperator implements Serializable, Operator {
	private static final long serialVersionUID = 1429478242021042150L;
	
	protected String userId;
	protected String personId;
	protected String ip;
	protected String mac;
	protected String tgc;
	protected String languageId;

	/** userId 必填 */
    public SsoOperator(String userId) {
        this.userId = userId;
    }

    public SsoOperator(String userId, String personId, String ip, String mac, String tgc, String languageId) {
        this.userId = userId;
        this.personId = personId;
        this.ip = ip;
        this.mac = mac;
        this.tgc = tgc;
        this.languageId = languageId;
    }

}
