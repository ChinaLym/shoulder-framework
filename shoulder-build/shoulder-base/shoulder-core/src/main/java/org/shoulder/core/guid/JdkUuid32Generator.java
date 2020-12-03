package org.shoulder.core.guid;

import java.util.UUID;

/**
 * jdk uuid 去掉 -
 *
 * @author lym
 */
public class JdkUuid32Generator extends JdkUuidEnhancer implements StringGuidGenerator {

    @Override
    public String nextId() {
        return generateUUID();
    }

    /**
     * 去掉 UUID 中的 '-'
     * 相比 UUID.randomUUID().toString().replace("-", "")
     * 位移替代 String.replace，但由于多次创建String对象，性能高10%
     */
    public static String generateUUID() {
        UUID uuid = UUID.randomUUID();
        long most = uuid.getMostSignificantBits();

        long least = uuid.getLeastSignificantBits();

        return digits(most >> 32, 8) +
            digits(most >> 16, 4) +
            digits(most, 4) +
            digits(least >> 48, 4) +
            digits(least, 12);
    }

}
