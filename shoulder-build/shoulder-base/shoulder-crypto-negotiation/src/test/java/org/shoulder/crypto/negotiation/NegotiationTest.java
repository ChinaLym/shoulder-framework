package org.shoulder.crypto.negotiation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.shoulder.core.constant.ByteSpecification;
import org.shoulder.crypto.asymmetric.impl.DefaultAsymmetricCipher;
import org.shoulder.crypto.asymmetric.store.impl.MemoryKeyPairCache;
import org.shoulder.crypto.negotiation.algorithm.DelegateNegotiationAsymmetricCipher;
import org.shoulder.crypto.negotiation.cipher.DefaultTransportCipher;
import org.shoulder.crypto.negotiation.cipher.TransportTextCipher;
import org.shoulder.crypto.negotiation.constant.NegotiationConstants;
import org.shoulder.crypto.negotiation.dto.NegotiationResult;
import org.shoulder.crypto.negotiation.support.dto.NegotiationRequest;
import org.shoulder.crypto.negotiation.support.dto.NegotiationResponse;
import org.shoulder.crypto.negotiation.util.TransportCryptoByteUtil;
import org.shoulder.crypto.negotiation.util.TransportCryptoUtil;
import org.springframework.http.HttpHeaders;

public class NegotiationTest {

    private static final TransportCryptoUtil client = new TransportCryptoUtil(new TransportCryptoByteUtil(new DelegateNegotiationAsymmetricCipher(DefaultAsymmetricCipher.ecc256(new MemoryKeyPairCache()))));

    private static final TransportCryptoUtil server = new TransportCryptoUtil(new TransportCryptoByteUtil(new DelegateNegotiationAsymmetricCipher(DefaultAsymmetricCipher.ecc256(new MemoryKeyPairCache()))));

    /**
     * 测试密钥协商机制和请求加解密正确
     */
    @Test
    public void testNegotiation() throws Exception {
        // client 请求 握手---------------------------
        NegotiationRequest clientNegotiationRequest = client.createRequest();
        Assertions.assertNotNull(clientNegotiationRequest);
        Assertions.assertFalse(clientNegotiationRequest.isRefresh());
        Assertions.assertNotNull(clientNegotiationRequest.getToken());
        Assertions.assertNotNull(clientNegotiationRequest.getXSessionId());
        Assertions.assertNotNull(clientNegotiationRequest.getPublicKey());

        // server 响应 握手---------------------------
        boolean serverCheckNegoReqToken = server.verifyToken(clientNegotiationRequest);
        Assertions.assertTrue(serverCheckNegoReqToken);
        NegotiationResponse serverPrepareResponse = server.prepareNegotiation(clientNegotiationRequest);
        NegotiationResponse serverNegotiationParam = serverPrepareResponse.clone();
        // 设置成对方的密钥进行握手
        serverNegotiationParam.setPublicKey(clientNegotiationRequest.getPublicKey());
        NegotiationResult serverNegotiationResult = server.negotiation(serverNegotiationParam);
        Assertions.assertEquals(clientNegotiationRequest.getPublicKey(), clientNegotiationRequest.getPublicKey());

        // client 确认 握手 + 生成DK---------------------------
        boolean clientVerifyNegoRespToken = client.verifyToken(serverPrepareResponse);
        Assertions.assertTrue(clientVerifyNegoRespToken);
        NegotiationResult clientNegotiationResult = client.negotiation(serverPrepareResponse);
        Assertions.assertArrayEquals(ByteSpecification.decodeToBytes(serverPrepareResponse.getPublicKey()), clientNegotiationResult.getOtherPublicKey());
//        Assertions.assertArrayEquals(clientNegotiationResult.getPublicKey());
        // check 不为空

        // client 发送DK加密后的密文---------------------------
        byte[] requestDkInClient = TransportCryptoUtil.generateDataKey(clientNegotiationResult.getKeyLength());
        TransportTextCipher requestEncryptCipher = DefaultTransportCipher.buildEncryptCipher(clientNegotiationResult, requestDkInClient);
        String originTextInClient = "hello shoulder In Client";
        String transportCipherTextByClientReq = requestEncryptCipher.encrypt(originTextInClient);
        Assertions.assertNotEquals(originTextInClient, transportCipherTextByClientReq);
        HttpHeaders clientReqHeader = client.generateHeaders(clientNegotiationResult, requestDkInClient);

        // server 解密DK，用DK解密请求，并返回加密响应---------------------------
        // 解密dk
        String xDkByClientReq = clientReqHeader.getFirst(NegotiationConstants.SECURITY_DATA_KEY);
        String xSessionIdByClientReq = clientReqHeader.getFirst(NegotiationConstants.SECURITY_SESSION_ID);
        String tokenByClientReq = clientReqHeader.getFirst(NegotiationConstants.TOKEN);
        boolean serverCheckDk = server.verifyToken(xSessionIdByClientReq, xDkByClientReq, tokenByClientReq, serverNegotiationResult.getOtherPublicKey());
        Assertions.assertTrue(serverCheckDk);
        byte[] requestDkInServer = TransportCryptoUtil.decryptDk(serverNegotiationResult, xDkByClientReq);
        Assertions.assertArrayEquals(requestDkInClient, requestDkInServer);
        // server 端解密密文
        DefaultTransportCipher requestDecryptCipherInServer = DefaultTransportCipher.buildDecryptCipher(serverNegotiationResult, requestDkInServer);
        String textInServer = requestDecryptCipherInServer.decrypt(transportCipherTextByClientReq);
        Assertions.assertEquals(originTextInClient, textInServer);

        // server 响应密文

        byte[] responseDk = TransportCryptoUtil.generateDataKey(serverNegotiationResult.getKeyLength());
        TransportTextCipher respEncryptCipher = DefaultTransportCipher.buildEncryptCipher(serverNegotiationResult, responseDk);
        String originTextInServer = "hello shoulder In Server";
        String transportCipherTextByServerResp = respEncryptCipher.encrypt(originTextInServer);
        Assertions.assertNotEquals(originTextInServer, transportCipherTextByServerResp);
        HttpHeaders serverRespHeader = server.generateHeaders(serverNegotiationResult, responseDk);

        // client 解密响应 ---------------------------
        String xDkByServerResp = serverRespHeader.getFirst(NegotiationConstants.SECURITY_DATA_KEY);
        String xSessionIdByServerResp = serverRespHeader.getFirst(NegotiationConstants.SECURITY_SESSION_ID);
        String tokenByServerResp = serverRespHeader.getFirst(NegotiationConstants.TOKEN);

        boolean clientCheckDk = client.verifyToken(xSessionIdByServerResp, xDkByServerResp, tokenByServerResp, clientNegotiationResult.getOtherPublicKey());
        Assertions.assertTrue(clientCheckDk);
        byte[] respDkInClient = TransportCryptoUtil.decryptDk(clientNegotiationResult, xDkByServerResp);
        Assertions.assertArrayEquals(responseDk, respDkInClient);
        // client 端解密响应密文
        DefaultTransportCipher responseDecryptCipherInServer = DefaultTransportCipher.buildDecryptCipher(clientNegotiationResult, respDkInClient);
        String textInClient = responseDecryptCipherInServer.decrypt(transportCipherTextByServerResp);
        Assertions.assertEquals(originTextInServer, textInClient);

    }
}
