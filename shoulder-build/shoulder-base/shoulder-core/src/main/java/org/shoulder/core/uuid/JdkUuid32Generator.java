package org.shoulder.core.uuid;

import java.util.UUID;

/**
 * jdk uuid 去掉 -
 *
 * @author lym
 */
public class JdkUuid32Generator implements StringGuidGenerator {

    @Override
    public String nextId() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

}
