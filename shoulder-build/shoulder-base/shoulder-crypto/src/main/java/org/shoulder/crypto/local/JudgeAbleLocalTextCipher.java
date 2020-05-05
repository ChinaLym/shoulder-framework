package org.shoulder.crypto.local;

import org.springframework.core.Ordered;

/**
 * 带判断能力的本地加解密器
 * 使用者可以扩展该接口，如加密方案更新时，可以额外扩展一个该接口，即可实现平滑升级
 * @author lym
 */
public interface JudgeAbleLocalTextCipher extends LocalTextCipher, Ordered {

    /**
     * 预判 cipherText 是否位本算法加密而成的（是否可以解密）
     * @param cipherText 密文
     * @return 是否可以解密
     */
    boolean support(String cipherText);
}
