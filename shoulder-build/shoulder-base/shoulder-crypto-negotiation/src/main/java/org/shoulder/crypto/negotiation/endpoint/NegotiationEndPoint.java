package org.shoulder.crypto.negotiation.endpoint;

import org.shoulder.crypto.negotiation.dto.KeyExchangeRequest;
import org.shoulder.crypto.negotiation.dto.KeyExchangeResponse;
import org.shoulder.crypto.negotiation.exception.NegotiationException;
import org.shoulder.crypto.negotiation.service.TransportNegotiationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 密钥协商默认 endPoint
 * todo 考虑 @FrameworkEndPoint ? api-doc
 * @author lym
 */
@RestController("security/v1")
public class NegotiationEndPoint {

    private final TransportNegotiationService negotiationService;

    public NegotiationEndPoint(TransportNegotiationService negotiationService) {
        this.negotiationService = negotiationService;
    }


    @PostMapping("negotiation")
    public KeyExchangeResponse handleNegotiate(@RequestBody KeyExchangeRequest keyExchangeRequest) throws NegotiationException {
        return negotiationService.handleNegotiate(keyExchangeRequest);
    }


}
