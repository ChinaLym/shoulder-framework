package org.shoulder.crypto.asymmetric;

import org.assertj.core.api.Assertions;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.Test;
import org.shoulder.crypto.asymmetric.exception.KeyPairException;
import org.shoulder.crypto.asymmetric.factory.AsymmetricKeyPairFactory;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 非对称加密测试-基础工厂密钥对测试
 *
 * @author lym
 */
public class AsymmetricKeyPairFactoryTest {

    private final String provider = BouncyCastleProvider.PROVIDER_NAME;

    private final AsymmetricKeyPairFactory rsa2048KeyPairFactory = new AsymmetricKeyPairFactory("RSA", 2048, provider);

    private final AsymmetricKeyPairFactory ecc256KeyPairFactory = new AsymmetricKeyPairFactory("EC", 256, provider);


    /**
     * 测试签名和验签正确
     */
    @Test
    public void testBuild() throws Exception {
        testBuildAndRebuild(rsa2048KeyPairFactory);
        testBuildAndRebuild(ecc256KeyPairFactory);
    }

    @Test
    public void testBuildFailWithNoSuchAlgorithm() throws Exception {
        KeyPairException ex = assertThrows(KeyPairException.class, () -> {
            new AsymmetricKeyPairFactory("fakerAlgorithm", 256, provider).build();
        });
        assertAll(
                () -> assertEquals("build key pair error.", ex.getMessage()),
                () -> assertEquals(NoSuchAlgorithmException.class, ex.getCause().getClass()),
                () -> assertEquals("no such algorithm: fakerAlgorithm for provider BC", ex.getCause().getMessage())
        );
    }

    @Test
    public void testBuildFail2() throws Exception {
        KeyPairException ex = assertThrows(KeyPairException.class, () -> {
            new AsymmetricKeyPairFactory("EC", 256, "fakerProvider").build();
        });
        assertAll(
                () -> assertEquals("build key pair error.", ex.getMessage()),
                () -> assertEquals(NoSuchProviderException.class, ex.getCause().getClass()),
                () -> assertEquals("no such provider: fakerProvider", ex.getCause().getMessage())
        );

    }

    private void testBuildAndRebuild(AsymmetricKeyPairFactory keyPairFactory) throws KeyPairException {
        KeyPair keyPair = keyPairFactory.build();
        KeyPair reBuildKeyPair = keyPairFactory.buildFrom(keyPair.getPublic().getEncoded(), keyPair.getPrivate().getEncoded());
        Assertions.assertThat(keyPair.getPublic().getEncoded()).isEqualTo(reBuildKeyPair.getPublic().getEncoded());
        Assertions.assertThat(keyPair.getPrivate().getEncoded()).isEqualTo(reBuildKeyPair.getPrivate().getEncoded());
    }
}
