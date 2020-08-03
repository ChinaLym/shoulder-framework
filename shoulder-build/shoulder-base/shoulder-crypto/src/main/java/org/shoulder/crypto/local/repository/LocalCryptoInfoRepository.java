package org.shoulder.crypto.local.repository;

import org.shoulder.crypto.local.entity.LocalCryptoInfoEntity;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * AES 加密所需信息持久化接口
 * 实现类保证持久化即可，不限制实现方式
 *
 * @author lym
 */
public interface LocalCryptoInfoRepository {

    /**
     * 保存加密信息
     *
     * @param aesInfo 待保存的的加密信息
     * @throws Exception when aesInfo persist fail.
     */
    void save(@NonNull LocalCryptoInfoEntity aesInfo) throws Exception;

    /**
     * 获取特定的加密信息
     *
     * @return AesInfoEntity
     */
    @Nullable
    LocalCryptoInfoEntity get(String appId, String markHeader) throws Exception;

    /**
     * 获取加密信息
     * 信息不存在时返回一个 emptyList
     *
     * @return List<AesInfoEntity>
     */
    @NonNull
    List<LocalCryptoInfoEntity> get(String appId) throws Exception;

}
