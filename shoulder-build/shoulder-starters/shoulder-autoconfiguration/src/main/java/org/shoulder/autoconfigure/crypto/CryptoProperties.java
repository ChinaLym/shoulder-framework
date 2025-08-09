package org.shoulder.autoconfigure.crypto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.shoulder.autoconfigure.core.BaseAppProperties;
import org.shoulder.crypto.asymmetric.dto.KeyPairDto;
import org.shoulder.crypto.negotiation.constant.NegotiationConstants;
import org.shoulder.crypto.negotiation.util.TransportCryptoByteUtil;
import org.shoulder.crypto.symmetric.SymmetricAlgorithmEnum;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 加密相关配置
 *
 * @author lym
 */
@Data
@ConfigurationProperties(prefix = CryptoProperties.PREFIX)
public class CryptoProperties {

    public static final String PREFIX = BaseAppProperties.KEY_PREFIX + "crypto";

    /**
     * 预留的公私钥对。若为空则代表采用自动生成，默认为空
     * key 为代码中使用的 id，value为公私钥对。
     */
    private Map<String, KeyPairDto> keyPair;

    /**
     * 对称加密相关配置
     */
    private LocalCryptoProperties local = new LocalCryptoProperties();

    /**
     * 密钥协商相关配置
     */
    private NegotiationProperties negotiation = new NegotiationProperties();

    /**
     * @deprecated 不支持也不推荐使用配置方式，推荐使用 @Bean 注入方式
     */
    private List<AsymmetricCryptoProperties> asymmetric;

    @Data
    public static class LocalCryptoProperties {

        /**
         * 加密元数据存储方式
         */
        private String repository;

        /**
         * 当以文件存储时，存储文件位置
         */
        private String metaInfoPath;

    }

    @Data
    public static class AsymmetricCryptoProperties {
        /**
         * 算法实现提供商
         */
        private String provider = "BC";

        /**
         * 算法名称
         */
        private String algorithm = "EC";

        /**
         * 密钥位数
         */
        private int keyLength = 256;

        /**
         * 算法实现
         */
        private String transformation = "ECIES";

        /**
         * 签名算法名称
         */
        private String signatureAlgorithm = "SHA256withECDSA";

        /**
         * 公钥端点
         */
        private EndpointProperties endpoint = new EndpointProperties(true, "/api/v1/crypto/publicKey/default");

    }

    @Data
    public static class NegotiationProperties {

        /**
         * 是否启用
         */
        private Boolean enable = true;

        /**
         * 算法实现
         */
        private String transformation = "ECIES";

        /**
         * 协商支持的算法的算法（数据密钥），如 AES、DES、SM4
         *
         * @see TransportCryptoByteUtil#SUPPORT_ENCRYPTION_SCHEMES
         * @see SymmetricAlgorithmEnum#getAlgorithmName
         */
        private Set<String> supportEncryptionSchemes = Set.of(SymmetricAlgorithmEnum.AES_CBC_PKCS5Padding.getAlgorithmName());

        /**
         * 数据密钥长度
         */
//        private List<Integer> supportKeyByteLength = ListUtil.of(16, 16, 24, 32);

        /**
         * 密钥协商 url
         */
        private EndpointProperties endpoint = new EndpointProperties(true, NegotiationConstants.DEFAULT_NEGOTIATION_URL);

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class EndpointProperties {

        /**
         * 是否启用
         */
        private Boolean enable;

        /**
         * 获取公钥 api 路径
         */
        private String path;

    }
}
