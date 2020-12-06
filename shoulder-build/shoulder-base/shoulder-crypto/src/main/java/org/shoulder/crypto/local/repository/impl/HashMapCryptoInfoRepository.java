package org.shoulder.crypto.local.repository.impl;

import org.shoulder.crypto.local.entity.LocalCryptoMetaInfo;
import org.shoulder.crypto.local.repository.LocalCryptoInfoRepository;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

/**
 * 使用 hashMap 作为存储
 * <p>
 * 仅适合单元测试、或加解密运行时数据使用
 *
 * @author lym
 */
public class HashMapCryptoInfoRepository implements LocalCryptoInfoRepository {

    private volatile LocalCryptoMetaInfo tempLocalCryptoMetaInfo = null;

    @Override
    public void save(@Nonnull LocalCryptoMetaInfo localCryptoMetaInfo) {
        tempLocalCryptoMetaInfo = localCryptoMetaInfo;
    }

    @Override
    public LocalCryptoMetaInfo get(@Nonnull String appId, @Nonnull String markHeader) {
        if (tempLocalCryptoMetaInfo != null && tempLocalCryptoMetaInfo.getHeader().equals(markHeader)) {
            return tempLocalCryptoMetaInfo;
        }
        // 其实不可能走到这步
        return null;
    }

    @Override
    @Nonnull
    public List<LocalCryptoMetaInfo> get(@Nonnull String appId) {
        return tempLocalCryptoMetaInfo == null ? Collections.emptyList() : Collections.singletonList(tempLocalCryptoMetaInfo);
    }

}
