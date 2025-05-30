package org.shoulder.autoconfiguration.test.redis;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.shoulder.autoconfigure.redis.RedisAutoConfiguration;

public class RedisKeySerializerTest {

    @Test
    public void testSerialize() {
        RedisAutoConfiguration.WithPrefixKeyStringRedisSerializer serializer = new RedisAutoConfiguration.WithPrefixKeyStringRedisSerializer("prefix");
        String key = "key";
        byte[] bytes = serializer.serialize(key);
        Assertions.assertEquals(key, serializer.deserialize(bytes));
    }
}
