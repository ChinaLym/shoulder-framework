package org.shoulder.crypto.negotiation.service;

import org.shoulder.crypto.negotiation.cache.dto.KeyExchangeResult;
import org.shoulder.crypto.negotiation.dto.KeyExchangeRequest;
import org.shoulder.crypto.negotiation.dto.KeyExchangeResponse;
import org.shoulder.crypto.negotiation.exception.NegotiationException;

/**
 * @author lym
 */
public interface TransportNegotiationService {

    /**
     * 与服务方进行密钥协商请求
     * @param serviceId 目标服务标识
     * @return 是否协商成功
     * @throws NegotiationException 密钥协商异常
     */
    KeyExchangeResult requestForNegotiate(String serviceId) throws NegotiationException;


    /**
     * 处理其他服务发起的密钥交换请求
     * @param keyExchangeRequest 请求参数
     * @return 是否协商成功
     * @throws NegotiationException 密钥协商异常
     */
    KeyExchangeResponse handleNegotiate(KeyExchangeRequest keyExchangeRequest) throws NegotiationException;

}
