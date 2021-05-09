package org.shoulder.crypto.local.repository;

import org.shoulder.crypto.local.entity.LocalCryptoMetaInfo;
import org.springframework.lang.Nullable;

import javax.annotation.Nonnull;
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
     * @param localCryptoMetaInfo 待保存的的加密信息
     * @throws Exception when localCryptoMetaInfo persist fail.
     */
    void save(@Nonnull LocalCryptoMetaInfo localCryptoMetaInfo) throws Exception;

    /**
     * 获取特定的加密信息
     *
     * @return AesInfoEntity
     */
    @Nullable
    LocalCryptoMetaInfo get(@Nonnull String appId, @Nonnull String markHeader) throws Exception;

    /**
     * 获取加密信息
     * 信息不存在时返回一个 emptyList
     *
     * @return List<AesInfoEntity>
     */
    @Nonnull
    List<LocalCryptoMetaInfo> get(@Nonnull String appId) throws Exception;

    /**
     * 是否支持集群
     *
     * @return 默认不支持
     */
    default boolean supportCluster() {
        return false;
    }

}
