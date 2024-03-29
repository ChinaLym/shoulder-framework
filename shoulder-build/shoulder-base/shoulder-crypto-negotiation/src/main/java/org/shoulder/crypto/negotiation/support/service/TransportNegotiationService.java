package org.shoulder.crypto.negotiation.support.service;

import jakarta.annotation.Nonnull;
import org.shoulder.crypto.negotiation.dto.NegotiationResult;
import org.shoulder.crypto.negotiation.exception.NegotiationException;
import org.shoulder.crypto.negotiation.support.SecurityRestTemplate;
import org.shoulder.crypto.negotiation.support.dto.NegotiationRequest;
import org.shoulder.crypto.negotiation.support.dto.NegotiationResponse;
import org.shoulder.crypto.negotiation.support.endpoint.NegotiationEndPoint;

import java.net.URI;

/**
 * 安全会话，密钥协商逻辑抽象接口，供以下两处使用
 *
 * @author lym
 * @see SecurityRestTemplate 发起密钥交换请求
 * @see NegotiationEndPoint  处理密钥交换请求
 */
public interface TransportNegotiationService {

    /**
     * 与服务方进行密钥协商请求
     *
     * @param uri 要请求的地址
     * @return 是否协商成功
     * @throws NegotiationException 密钥协商异常
     */
    NegotiationResult requestForNegotiate(URI uri) throws NegotiationException;


    /**
     * 处理其他服务发起的密钥交换请求
     *
     * @param negotiationRequest 请求参数
     * @return 是否协商成功
     * @throws NegotiationException 密钥协商异常
     */
    NegotiationResponse handleNegotiate(NegotiationRequest negotiationRequest) throws NegotiationException;

    /**
     * 判断一个 url 是否为密钥交换的地址
     *
     * @param uri uri
     * @return 是否为已经标识为密钥交换的地址
     */
    boolean isNegotiationUrl(@Nonnull URI uri);
}
