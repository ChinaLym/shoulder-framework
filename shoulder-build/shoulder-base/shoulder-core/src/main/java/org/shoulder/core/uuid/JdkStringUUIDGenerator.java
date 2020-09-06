package org.shoulder.core.uuid;

import java.util.UUID;

/**
 * 简单地将 jdk uuid 去掉 -
 *
 * @author lym
 */
public class JdkStringUUIDGenerator implements StringGuidGenerator {

    @Override
    public String nextId() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    @Override
    public String[] nextIds(int num) {
        // 注意限制 num 大小
        String[] ids = new String[num];

        for (int i = 0; i < num; i++) {
            ids[i] = nextId();
        }

        return ids;
    }

}
