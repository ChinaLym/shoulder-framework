package com.example.demo5.controller;

import com.example.demo5.dto.ComplexParam;
import com.example.demo5.dto.ComplexResult;
import com.example.demo5.dto.SimpleParam;
import com.example.demo5.dto.SimpleResult;
import lombok.extern.slf4j.Slf4j;
import org.shoulder.core.dto.response.BaseResult;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.AbstractMessageConverterMethodArgumentResolver;

/**
 * 接口加密
 *
 * @author lym
 * @see DefaultAsymmetricCipher#ecc256
 */
@Slf4j
@RestController
@RequestMapping("complex")
public class TransportCryptoComplexController {

    @Autowired
    private SecurityRestTemplate restTemplate;

    @Value("${server.port}")
    private String port;

    /**
     * 测试发起  <a href="http://localhost:80/complex/send"/>
     *
     * @see SensitiveRequestEncryptMessageConverter#writeInternal 在这打断点，观察参数确实是自动加密处理的
     * @see SensitiveRequestEncryptMessageConverter#read 在这打断点，观察返回值确实是密文
     * @see AbstractMessageConverterMethodArgumentResolver#readWithMessageConverters this.getAdvice(). xxx 行，其中 body 就是实际收到的响应（未解密状态的）；afterBodyRead 中会有增强器进行自动解密
     * @see SensitiveRequestDecryptAdvance#afterBodyRead 观察解密响应执行
     */
    @SkipResponseWrap
    @GetMapping("send")
    public ComplexResult send() throws AsymmetricCryptoException {
        SimpleParam inner = new SimpleParam();
        inner.setCipher("innerCipher");
        inner.setText("innerText");
        ComplexParam param = new ComplexParam();
        param.setText("shoulder");
        param.setCipher("ChinaLym");
        param.setInnerCipher(inner);

        HttpEntity<ComplexParam> httpEntity = new HttpEntity<>(param, null);
        ParameterizedTypeReference<BaseResult<ComplexResult>> resultType = new ParameterizedTypeReference<>() {
        };
        ResponseEntity<BaseResult<ComplexResult>> responseEntity = restTemplate.exchange("http://localhost:80/complex/receive", HttpMethod.POST,
                httpEntity, resultType);

        BaseResult<ComplexResult> apiResponse = responseEntity.getBody();
        System.out.println(JsonUtils.toJson(apiResponse));
        return apiResponse.getData();
    }


    /**
     * 测试直接请求加密接口  <a href="http://localhost:80/complex/receive"/>
     *
     * @see SensitiveRequestDecryptHandlerInterceptor 观察服务端收到请求后，将敏感参数自动解密，或者 拒绝未握手/正确加密的请求
     * @see SensitiveResponseEncryptAdvice#beforeBodyWrite 观察返回值自动加密
     */
    @Sensitive
    @SkipResponseWrap
    @RequestMapping(value = "receive", method = {RequestMethod.GET, RequestMethod.POST})
    public ComplexResult receive(@RequestBody(required = false) ComplexParam param) {
        System.out.println(param);
        SimpleResult inner = new SimpleResult();
        inner.setCipher("innerResult");
        inner.setText("inner666");
        ComplexResult result = new ComplexResult();
        result.setCipher("shoulder");
        result.setText("666");
        result.setInnerCipher(inner);
        return result;
    }


}
