package org.shoulder.crypto.asymmetric;

import org.assertj.core.api.Assertions;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Test;
import org.shoulder.crypto.asymmetric.exception.KeyPairException;
import org.shoulder.crypto.asymmetric.factory.AsymmetricKeyPairFactory;

import java.security.KeyPair;

/**
 * AsymmetricKeyPairFactoryTest
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

    @Test(expected = KeyPairException.class)
    public void testBuildFail1() throws Exception {
        new AsymmetricKeyPairFactory("fakerAlgorithm", 256, provider).build();
    }

    @Test(expected = KeyPairException.class)
    public void testBuildFail2() throws Exception {
        new AsymmetricKeyPairFactory("EC", 256, "fakerProvider").build();
    }

    private void testBuildAndRebuild(AsymmetricKeyPairFactory keyPairFactory) throws KeyPairException {
        KeyPair keyPair = keyPairFactory.build();
        KeyPair reBuildKeyPair = keyPairFactory.buildFrom(keyPair.getPublic().getEncoded(), keyPair.getPrivate().getEncoded());
        Assertions.assertThat(keyPair.getPublic().getEncoded()).isEqualTo(reBuildKeyPair.getPublic().getEncoded());
        Assertions.assertThat(keyPair.getPrivate().getEncoded()).isEqualTo(reBuildKeyPair.getPrivate().getEncoded());
    }
}
