package com.example.demo5.controller;

import com.example.demo5.dto.ApiParam;
import com.example.demo5.dto.ApiResult;
import lombok.extern.slf4j.Slf4j;
import org.shoulder.core.dto.response.BaseResponse;
import org.shoulder.core.util.JsonUtils;
import org.shoulder.crypto.asymmetric.exception.AsymmetricCryptoException;
import org.shoulder.crypto.asymmetric.processor.impl.DefaultAsymmetricCryptoProcessor;
import org.shoulder.crypto.negotiation.http.SecurityRestTemplate;
import org.shoulder.crypto.negotiation.http.SensitiveDateEncryptMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 接口加密
 *
 * @author lym
 * @see DefaultAsymmetricCryptoProcessor#ecc256
 */
@Slf4j
@RestController
@RequestMapping("cipherapi")
public class TransportCryptoDemoController {

    @Autowired
    private SecurityRestTemplate restTemplate;

    @Value("${server.port}")
    private String port;

    /**
     * 测试发起  <a href="http://localhost:80/cipherapi/send"/>
     *
     * @see SensitiveDateEncryptMessageConverter#writeInternal 观察参数确实是自动加密处理的
     * @see SensitiveDateEncryptMessageConverter#read 观察返回值确实是密文
     */
    @GetMapping("send")
    public ApiResult send() throws AsymmetricCryptoException {
        ApiParam param = new ApiParam();
        param.setCipher("123");
        param.setText("12345");

        HttpEntity<ApiParam> httpEntity = new HttpEntity<>(param, null);
        ParameterizedTypeReference<BaseResponse<ApiResult>> resultType = new ParameterizedTypeReference<>() {
        };
        ResponseEntity<BaseResponse<ApiResult>> responseEntity = restTemplate.exchange("http://localhost:80/cipherapi/receive", HttpMethod.POST,
                httpEntity, resultType);

        BaseResponse<ApiResult> apiResponse = responseEntity.getBody();
        System.out.println(JsonUtils.toJson(apiResponse));
        return apiResponse.getData();
    }


    /**
     * 测试直接请求加密接口  <a href="http://localhost:80/cipherapi/receive"/>
     *
     * @see 观察参数自动解密，和拒绝非握手的请求
     * @see 观察返回值自动加密
     */
    @PostMapping("receive")
    public ApiResult receive(@RequestBody ApiParam param) {
        System.out.println(param);
        ApiResult result = new ApiResult();
        result.setCipher("shoulder");
        result.setText("666");
        return result;
    }


}
