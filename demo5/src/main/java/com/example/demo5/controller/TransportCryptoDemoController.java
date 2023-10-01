package com.example.demo5.controller;

import com.example.demo5.dto.SimpleParam;
import com.example.demo5.dto.SimpleResult;
import lombok.extern.slf4j.Slf4j;
import org.shoulder.core.util.JsonUtils;
import org.shoulder.crypto.asymmetric.exception.AsymmetricCryptoException;
import org.shoulder.crypto.asymmetric.impl.DefaultAsymmetricCipher;
import org.shoulder.crypto.negotiation.support.SecurityRestTemplate;
import org.shoulder.crypto.negotiation.support.Sensitive;
import org.shoulder.crypto.negotiation.support.client.SensitiveRequestEncryptMessageConverter;
import org.shoulder.crypto.negotiation.support.server.SensitiveRequestDecryptAdvance;
import org.shoulder.crypto.negotiation.support.server.SensitiveRequestDecryptHandlerInterceptor;
import org.shoulder.crypto.negotiation.support.server.SensitiveResponseEncryptAdvice;
import org.shoulder.web.annotation.SkipResponseWrap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpMessageConverterExtractor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.AbstractMessageConverterMethodArgumentResolver;

/**
 * 接口加密
 *
 * @author lym
 * @see DefaultAsymmetricCipher#ecc256
 */
@Slf4j
@SkipResponseWrap
@RestController
@RequestMapping("simple")
public class TransportCryptoDemoController {

    @Autowired
    private SecurityRestTemplate restTemplate;

    @Value("${server.port}")
    private String port;

    /**
     * 测试对另一个服务发起加密请求，自动进行密钥交换并加密传输  <a href="http://localhost:8080/simple/send"/>
     *
     * @see SensitiveRequestEncryptMessageConverter#writeInternal 观察参数确实是自动加密处理的
     * @see SensitiveRequestEncryptMessageConverter#read 观察返回值确实是密文（或者看控制台日志也能看到是密文）
     * @see RestTemplate.ResponseEntityResponseExtractor#extractData
     * @see HttpMessageConverterExtractor#messageConverters
     * 如果 client端拿到空响应，但是没报错，大概率就是 spring RestTemplate 传入的预期响应类型不正确，但Json.ObjectMapper使用了new Object
     */
    @GetMapping("send")
    public SimpleResult send() throws AsymmetricCryptoException {
        SimpleParam param = new SimpleParam();
        param.setCipher("123");
        param.setText("12345");

        HttpEntity<SimpleParam> httpEntity = new HttpEntity<>(param, null);
        ParameterizedTypeReference<SimpleResult> resultType = new ParameterizedTypeReference<>() {
        };
        ResponseEntity<SimpleResult> responseEntity = restTemplate.exchange("http://localhost:80/simple/receive", HttpMethod.POST,
                httpEntity, resultType);

        SimpleResult apiResponse = responseEntity.getBody();
        System.out.println(JsonUtils.toJson(apiResponse));
        return apiResponse;
    }


    /**
     * 测试直接请求加密接口  <a href="http://localhost:80/simple/receive"/>
     *
     * @see SensitiveRequestDecryptHandlerInterceptor 观察参数自动解密，和拒绝非握手的请求
     * @see AbstractMessageConverterMethodArgumentResolver#readWithMessageConverters this.getAdvice(). xxx 行，其中 body 就是实际收到的请求（未解密状态的）
     * @see SensitiveRequestDecryptAdvance#afterBodyRead 观察解密响应执行
     * @see SensitiveResponseEncryptAdvice 观察返回值自动加密
     */
    @Sensitive
    @RequestMapping(value = "receive", method = {RequestMethod.GET, RequestMethod.POST})
    public SimpleResult receive(@RequestBody(required = false) SimpleParam param) {
        System.out.println(param);
        SimpleResult result = new SimpleResult();
        result.setCipher("shoulder");
        result.setText("666");
        return result;
    }


}
