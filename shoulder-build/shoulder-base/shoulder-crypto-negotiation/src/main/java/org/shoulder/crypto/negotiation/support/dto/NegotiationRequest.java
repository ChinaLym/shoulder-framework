package org.shoulder.crypto.negotiation.support.dto;

import lombok.Data;

import java.util.Set;

/**
 * 协商发起者提供参数
 * Token = 服务端公钥签名（xSessionId + publicKey）
 *
 * @author lym
 */
@Data
public class NegotiationRequest {

    /**
     * Header 中 会话标识
     */
    private transient String xSessionId;

    /**
     * Header 中 防篡改签名
     */
    private transient String token;

    /**
     * 发起者用于协商的公钥
     */
    private String publicKey;

    /**
     * 【非空】发起者支持的 报文 加解密算法，如 AES-128 / AES-192 / AES-256
     */
    private Set<String> encryptionSchemeSupports;

    /**
     * 是否强制双方重新协商
     */
    private boolean refresh = false;

}
