package org.shoulder.core.guid;

import java.util.UUID;

/**
 * jdk uuid，并压缩为 19 位 todo 【功能开发】
 *
 * @author lym
 */
public class JdkUuidPressed19Generator implements StringGuidGenerator {

    @Override
    public String nextId() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

}
