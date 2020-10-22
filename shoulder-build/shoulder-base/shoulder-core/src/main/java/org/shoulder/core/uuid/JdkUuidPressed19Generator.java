package org.shoulder.core.uuid;

import java.util.UUID;

/**
 * jdk uuid，并压缩为 19 位 todo
 *
 * @author lym
 */
public class JdkUuidPressed19Generator implements StringGuidGenerator {

    @Override
    public String nextId() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

}
