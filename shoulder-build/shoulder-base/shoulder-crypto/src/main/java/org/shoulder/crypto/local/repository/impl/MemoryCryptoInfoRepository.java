package org.shoulder.crypto.local.repository.impl;

import jakarta.annotation.Nonnull;
import org.shoulder.crypto.local.entity.LocalCryptoMetaInfo;
import org.shoulder.crypto.local.repository.LocalCryptoInfoRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * 使用 hashMap 作为存储
 * <p>
 * 仅适合单元测试、或加解密运行时数据使用
 *
 * @author lym
 */
public class MemoryCryptoInfoRepository implements LocalCryptoInfoRepository {

    private final List<LocalCryptoMetaInfo> cryptoInfoList = new ArrayList<>(1);

    @Override
    public void save(@Nonnull LocalCryptoMetaInfo localCryptoMetaInfo) {
        cryptoInfoList.add(localCryptoMetaInfo);
    }

    @Override
    public LocalCryptoMetaInfo queryOneByAppIdAndHeader(@Nonnull String appId, @Nonnull String markHeader) {
        return cryptoInfoList.stream()
                .filter(c -> c.getAppId().equals(appId) && c.getHeader().equals(markHeader))
                .findFirst()
                .orElse(null);
    }

    @Override
    @Nonnull
    public List<LocalCryptoMetaInfo> queryAllByAppId(@Nonnull String appId) {
        return cryptoInfoList;
    }

}
