package org.shoulder.crypto.local;

import org.springframework.core.Ordered;

/**
 * 带判断能力的本地加解密器
 * 使用者可以扩展该接口，如加密方案更新时，可以额外扩展一个该接口，即可实现加密算法平滑升级，解密时会根据当时加密用的的算法解密，加密时采用最新的算法
 * 框架会将其实现按照从小到大排序，优先选择 Order 小的进行加密。
 *
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
