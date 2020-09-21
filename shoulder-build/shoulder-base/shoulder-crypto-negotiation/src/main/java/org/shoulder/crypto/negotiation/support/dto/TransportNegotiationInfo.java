package org.shoulder.crypto.negotiation.support.dto;

import lombok.Data;

/**
 * 传输加密信息
 * 用于注册给 SecurityRestTemplate
 *
 * @author lym
 */
@Data
public class TransportNegotiationInfo {

    /**
     * 应用标识（服务名称），用于寻址
     */
    private String appId;

    /**
     * 密钥协商地址，用于密钥协商
     */
    private String negotiationUrl;

}
