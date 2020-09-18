package com.example.demo5.controller;

import com.example.demo5.dto.ApiParam;
import com.example.demo5.dto.ApiResult;
import lombok.extern.slf4j.Slf4j;
import org.shoulder.core.util.JsonUtils;
import org.shoulder.crypto.asymmetric.exception.AsymmetricCryptoException;
import org.shoulder.crypto.asymmetric.processor.impl.DefaultAsymmetricCryptoProcessor;
import org.shoulder.crypto.negotiation.http.SecurityRestTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    private SecurityRestTemplate restTemplate = new SecurityRestTemplate();

    /**
     * 测试发起  <a href="http://localhost:8080/cipherapi/send"/>
     */
    @GetMapping("send")
    public ApiResult send(String text) throws AsymmetricCryptoException {
        ApiParam param = new ApiParam();
        param.setCipher("123");
        param.setText("12345");

        ApiResult result = restTemplate.postForObject("http://localhost:8001/cipherapi/receive", param, ApiResult.class);
        System.out.println(JsonUtils.toJson(result));
        return result;
    }

    @PostMapping("receive")
    public ApiResult receive(ApiParam param) throws AsymmetricCryptoException {
        System.out.println(JsonUtils.toJson(param));
        ApiResult result = new ApiResult();
        result.setCipher("shoulder");
        result.setText("666");
        return result;
    }


}
