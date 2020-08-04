package org.shoulder.crypto.local.repository.impl;

import org.shoulder.crypto.local.entity.LocalCryptoInfoEntity;
import org.shoulder.crypto.local.repository.LocalCryptoInfoRepository;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 使用 hashMap 作为存储
 * <p>
 * 仅适合单元测试、或加解密运行时数据使用
 *
 * @author lym
 */
public class HashMapCryptoInfoRepository implements LocalCryptoInfoRepository {

    private final Map<String, List<LocalCryptoInfoEntity>> storage = new ConcurrentHashMap<>(1);

    @Override
    public void save(@NonNull LocalCryptoInfoEntity aesInfo) {
        List<LocalCryptoInfoEntity> algorithmList = storage.computeIfAbsent(aesInfo.getAppId(),
            key -> new LinkedList<>());
        algorithmList.add(aesInfo);
    }

    @Override
    public LocalCryptoInfoEntity get(String appId, String markHeader) {
        List<LocalCryptoInfoEntity> algorithmList = storage.get(appId);
        if (CollectionUtils.isEmpty(algorithmList)) {
            return null;
        }
        for (LocalCryptoInfoEntity localCryptoInfoEntity : algorithmList) {
            if (localCryptoInfoEntity.getHeader().equals(markHeader)) {
                return localCryptoInfoEntity;
            }
        }
        return null;
    }

    @Override
    @NonNull
    public List<LocalCryptoInfoEntity> get(String appId) {
        return storage.get(appId);
    }

}
