package com.example.demo5.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.shoulder.crypto.negotiation.annotation.Sensitive;

/**
 * 测试加解密的返回值
 *
 * @author lym
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResult {

    String text;

    @Sensitive
    String cipher;
}
