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
    public void save(@Nonnull LocalCryptoMetaInfo aesInfo) {
        tempLocalCryptoMetaInfo = aesInfo;
    }

    @Override
    public LocalCryptoMetaInfo get(String appId, String markHeader) {
        return tempLocalCryptoMetaInfo;
    }

    @Override
    @Nonnull
    public List<LocalCryptoMetaInfo> get(String appId) {
        return tempLocalCryptoMetaInfo == null ? Collections.emptyList() : Collections.singletonList(tempLocalCryptoMetaInfo);
    }

}
