package org.shoulder.crypto.asymmetric.store;

import org.apache.commons.collections4.MapUtils;
import org.shoulder.crypto.asymmetric.dto.KeyPairDto;
import org.shoulder.crypto.asymmetric.exception.NoSuchKeyPairException;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * 密钥对存储
 * 如果分布式部署，需要共享存储
 *
 * @author lym
 */
public interface KeyPairCache {
    /**
     * 存储密钥对
     *
     * @param id         id
     * @param keyPairDto 密钥对
     */
    void put(String id, @Nonnull KeyPairDto keyPairDto);

    /**
     * 存储密钥对
     *
     * @param id         id
     * @param keyPairDto 密钥对
     * @return 是否更新
     */
    boolean putIfAbsent(String id, @Nonnull KeyPairDto keyPairDto);


    /**
     * 存储多个密钥对
     *
     * @param keyPairDtoMap 密钥对 map。key: id; value: keyPairDto
     */
    default void put(Map<String, KeyPairDto> keyPairDtoMap) {
        if (MapUtils.isNotEmpty(keyPairDtoMap)) {
            keyPairDtoMap.forEach(this::put);
        }
    }

    /**
     * 获取密钥对
     *
     * @param id id
     * @return 密钥对
     * @throws NoSuchKeyPairException 密钥对缺失
     */
    @Nonnull
    KeyPairDto get(String id) throws NoSuchKeyPairException;

}
