package org.shoulder.crypto.negotiation.endpoint;

import org.shoulder.core.dto.response.BaseResponse;
import org.shoulder.crypto.negotiation.constant.KeyExchangeConstants;
import org.shoulder.crypto.negotiation.dto.KeyExchangeRequest;
import org.shoulder.crypto.negotiation.dto.KeyExchangeResponse;
import org.shoulder.crypto.negotiation.exception.NegotiationException;
import org.shoulder.crypto.negotiation.service.TransportNegotiationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 密钥协商默认 endPoint
 *
 * @author lym
 */
@RestController
public class NegotiationEndPoint {

    private final TransportNegotiationService negotiationService;

    public NegotiationEndPoint(TransportNegotiationService negotiationService) {
        this.negotiationService = negotiationService;
    }

    /**
     * 密钥交换请求 默认处理地址
     *
     * @param keyExchangeRequest 密钥交换请求参数
     * @return 密钥协商结论
     * @throws NegotiationException 密钥交换失败
     */
    @PostMapping(KeyExchangeConstants.DEFAULT_NEGOTIATION_URL)
    public BaseResponse<KeyExchangeResponse> handleNegotiate(@RequestBody KeyExchangeRequest keyExchangeRequest) throws NegotiationException {
        return BaseResponse.success(negotiationService.handleNegotiate(keyExchangeRequest));
    }


}
