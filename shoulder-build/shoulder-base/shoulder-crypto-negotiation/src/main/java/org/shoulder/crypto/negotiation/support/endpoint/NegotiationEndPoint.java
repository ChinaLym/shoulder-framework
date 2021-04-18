package org.shoulder.crypto.negotiation.support.endpoint;

import org.shoulder.core.dto.response.BaseResult;
import org.shoulder.crypto.negotiation.constant.NegotiationConstants;
import org.shoulder.crypto.negotiation.exception.NegotiationException;
import org.shoulder.crypto.negotiation.support.dto.NegotiationRequest;
import org.shoulder.crypto.negotiation.support.dto.NegotiationResponse;
import org.shoulder.crypto.negotiation.support.service.TransportNegotiationService;
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
     * @param negotiationRequest 密钥交换请求参数
     * @return 密钥协商结论
     * @throws NegotiationException 密钥交换失败
     */
    @PostMapping(NegotiationConstants.DEFAULT_NEGOTIATION_URL)
    public BaseResult<NegotiationResponse> handleNegotiate(@RequestBody NegotiationRequest negotiationRequest) throws NegotiationException {
        return BaseResult.success(negotiationService.handleNegotiate(negotiationRequest));
    }


}
