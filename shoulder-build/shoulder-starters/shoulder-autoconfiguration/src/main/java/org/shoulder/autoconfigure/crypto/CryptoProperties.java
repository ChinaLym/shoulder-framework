package org.shoulder.autoconfigure.crypto;

import lombok.Data;
import org.shoulder.core.constant.ShoulderFramework;
import org.shoulder.crypto.asymmetric.dto.KeyPairDto;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

/**
 * @author lym
 */
@Data
@ConfigurationProperties(prefix = ShoulderFramework.CONFIG_PREFIX + "crypto")
public class CryptoProperties {

    /**
     * 预留的公私钥对。若为空则代表采用自动生成，默认为空
     * key 为代码中使用的 id，value为公私钥对。
     */
    private Map<String, KeyPairDto> keyPair;

    /**
     * @deprecated 不支持也不推荐使用配置方式，推荐使用 @Bean 注入方式
     */
    private List<AsymmetricCryptoProperties> asymmetric;


    @Data
    public static class AsymmetricCryptoProperties {
        /** 算法实现提供商 */
        private String provider = "BC";

        /** 算法名称 */
        private String algorithm = "EC";

        /** 秘钥位数 */
        private int keyLength = 256;

        /** 算法实现 */
        private String transformation = "ECIES";

        /** 签名算法名称 */
        private String signatureAlgorithm = "SHA256withECDSA";
    }

}
