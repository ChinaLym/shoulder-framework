package org.shoulder.core.guid;

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
