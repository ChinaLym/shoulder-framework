package com.example.demo1.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 测试加解密的返回值
 *
 * @author lym
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CryptoTestDTO {
    String text;
    String cipher;
    String decryptText;
}
