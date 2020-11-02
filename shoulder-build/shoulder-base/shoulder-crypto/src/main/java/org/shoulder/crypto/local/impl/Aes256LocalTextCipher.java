package org.shoulder.crypto.local.impl;

import org.shoulder.core.constant.ByteSpecification;
import org.shoulder.core.util.ByteUtils;
import org.shoulder.crypto.aes.AesUtil;
import org.shoulder.crypto.aes.exception.AesCryptoException;
import org.shoulder.crypto.digest.Sha256Utils;
import org.shoulder.crypto.exception.CipherRuntimeException;
import org.shoulder.crypto.exception.CryptoErrorCodeEnum;
import org.shoulder.crypto.local.JudgeAbleLocalTextCipher;
import org.shoulder.crypto.local.entity.LocalCryptoInfoEntity;
import org.shoulder.crypto.local.repository.LocalCryptoInfoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;

import java.nio.charset.Charset;
import java.util.*;

/**
 * 本地数据加解密
 * <p>
 * 加解密算法 密钥分级存储的 AES256，类型：AES/CBC/PKCS5Padding.
 *
 * <p> 理论：
 * 加解密：将 dataKey作为密钥, dataIv作为偏移向量，使用 AES256 算法将敏感数据 data 加解密。即由本类职责。
 * 由上可知，加解密依赖 dataKey 和 dataIv	，因此需要保证每次启动时可以拿到 dataKey 与 dataIv，且dataKey必须被保护。
 * dataKey 与 dataIv 的存取方式：该职责由 {@link LocalCryptoInfoRepository} 和 {@link LocalCryptoInfoEntity}负责实现。
 * 数据密钥 dataKey 的保护：Aes256(dataKey, SHA256(rootKey, random), rootKeyIv)，保护方式同敏感数据，由另一个密钥 rootKey 和一个加密向量保护，同样为
 * Aes256算法。
 * rootKey、rootKeyIv 的保存一样由 {@link LocalCryptoInfoRepository} 和 {@link LocalCryptoInfoEntity}负责实现。
 * rootKey 的保护：持久化的值为 SHA256(rootKey, random) 而不是 rootKey 本身
 * rootKey 的生成：32个字符即 256位，详见 {@link Aes256LocalTextCipher#generateDataKeyProtectKey}
 *
 * <ul>
 * <li> 敏感数据（data）：需要被加密的数据
 *
 * <li> 数据密钥：用于保护本地敏感信息。因为是基于AES算法，数据密钥仅在项目部署时生成一次（一个随机数），后续不能变更。因此需要持久化，由 AesInfoRepository 完成持久化。
 *
 * <li>根密钥：用于保护数据密钥 —— 持久化数据密钥时，通过根密钥加密数据密钥。
 *
 * <li>密钥向量：用于加密数据密钥、加密敏感数据
 * <p>
 * 改成 abstract 以更好的扩展？无需求，暂不
 *
 * @author lym
 */
public class Aes256LocalTextCipher implements JudgeAbleLocalTextCipher {

    /**
     * 长度为6的加密标记，与加密版本挂钩，该字段的存在支持升级版本。AES256 2^8
     */
    public static final String ALGORITHM_HEADER = "${a8} ";
    private final static Logger log = LoggerFactory.getLogger(Aes256LocalTextCipher.class);
    private static final Charset CHAR_SET = ByteSpecification.STD_CHAR_SET;
    /**
     * 保护数据密钥的 iv 16 * 8
     */
    private static final byte[] DATA_KEY_IV = "shoulder:Cn-Lym!".getBytes(CHAR_SET);
    /**
     * 根密钥固定部分
     */
    private static final byte[] ROOT_KEY_FINAL_PART = "shoulderFramework:CN-Lym".getBytes(CHAR_SET);
    /**
     * Aes 密钥长度
     */
    private static final int AES_KEY_LENGTH = 256;
    /**
     * 根密钥随机部分长度
     */
    private static final int ROOT_KEY_RANDOM_LENGTH = AES_KEY_LENGTH - ROOT_KEY_FINAL_PART.length;
    /**
     * 密钥持久化依赖：用于获取持久化的加密信息
     */
    private final LocalCryptoInfoRepository aesInfoDao;
    private String appId;


    public Aes256LocalTextCipher(LocalCryptoInfoRepository aesInfoRepository, String appId) {
        this.aesInfoDao = aesInfoRepository;
        this.appId = appId;
    }

    // ======================================== 对外接口 ============================================

    /**
     * 根据 rootKey 生成数据密钥的加密密钥 ： SHA256(rootKeyParts)
     * - 根密钥总长度为 256 位，其中一部分写死在代码中。另一部分启动时随机生成，由随机部分保证每个项目的根密钥不同，即保证了即使不同项目中随机出相同数据密钥（极端情况/小概率事件），密文仍然不同！
     * - 使用 SHA256 获取 rootKey 的摘要信息，临时随机生成 rootKey 用完即回收，结合 Sha256单向摘要算法，保证后续无法通过任何手段还原 rootKey 明文
     *
     * @param randomBytes rootKey 的随机部分
     * @return 数据密钥的加密密钥，用于保护真正的数据密钥明文，长度为 16 位 / 256 bit。
     */
    private static byte[] generateDataKeyProtectKey(byte[] randomBytes) {
        assert randomBytes.length == ROOT_KEY_RANDOM_LENGTH;

        byte[] rootKey = new byte[256];
        ByteUtils.copy(ROOT_KEY_FINAL_PART, 0, rootKey, 0, ROOT_KEY_FINAL_PART.length);
        ByteUtils.copy(randomBytes, 0, rootKey, ROOT_KEY_FINAL_PART.length, ROOT_KEY_RANDOM_LENGTH);

        return Sha256Utils.digest(rootKey);
    }

    /**
     * 生成加密 数据密钥 的aes向量 dataIv
     */
    private static byte[] generateDataKeyIv() {
        return ByteUtils.randomBytes(16);
    }

    /**
     * 生成数据密钥
     *
     * @return 数据密钥
     */
    private static byte[] generateDataKey() {
        return ByteUtils.randomBytes(32);
    }

    @Override
    public String encrypt(@NonNull String text) {
        ensureEncryption();
        AesInfoCache cacheInfo = CacheManager.getAesInfoCache(ALGORITHM_HEADER);
        try {
            byte[] encryptResult = AesUtil.encrypt(text.getBytes(CHAR_SET), cacheInfo.dataKey,
                cacheInfo.dateIv);
            return ALGORITHM_HEADER + ByteSpecification.encodeToString(encryptResult);
        } catch (AesCryptoException e) {
            throw CryptoErrorCodeEnum.ENCRYPT_FAIL.toException(e);
        }
    }

    @Override
    public String decrypt(@NonNull String cipherText) {
        ensureEncryption();
        String[] cipherTextAndHeader = splitHeader(cipherText);
        String cipherTextHeader = cipherTextAndHeader[0];
        String realCipherText = cipherTextAndHeader[1];
        AesInfoCache cacheInfo = CacheManager.getAesInfoCache(cipherTextHeader);
        if (cacheInfo == null) {
            throw new CipherRuntimeException(CryptoErrorCodeEnum.ENCRYPT_FAIL.getCode(),
                "cipher's markHeader is {}", cipherTextHeader);
        }
        try {
            byte[] decryptData = AesUtil.decrypt(Base64.getDecoder().decode(realCipherText), cacheInfo.dataKey, cacheInfo.dateIv);
            return new String(decryptData, CHAR_SET);
        } catch (AesCryptoException e) {
            throw CryptoErrorCodeEnum.DECRYPT_FAIL.toException(e);
        }
    }

    /**
     * 分割为 头部、真正密文
     *
     * @param cipherText 存储的密文
     * @return result[0] Header result[1]realCipherText
     */
    private String[] splitHeader(String cipherText) {
        String header = cipherText.substring(0, 6);
        String realCipher = cipherText.substring(6);
        return new String[]{header, realCipher};
    }


    // =============================== 初始化流程 ====================================

    @Override
    public boolean support(String cipherText) {
        String header = cipherText.substring(0, 6);
        return CacheManager.getAesInfoCache(header) != null;
    }

    /**
     * 优先级为默认优先级 0
     */
    @Override
    public int getOrder() {
        return 0;
    }

    // ============================ 初始化所需的数据生成算法 =====================================

    /**
     * 确保加密前所需的变量已经初始化：检查加密配件缓存是否已经初始化
     * 若内存{@code this.cache}中密钥信息为空：
     * 1. 尝试从数据库拿				 （非首次启动时）
     * 2. 若第一步没拿到则进行初始化，将必要信息保存至 DB （首次启动时）
     */
    @Override
    public void ensureEncryption() {
        if (CacheManager.initialized) {
            return;
        }
        synchronized (this) {
            // memory block by lazy set ensure security DCL without volatile
            if (CacheManager.initialized) {
                return;
            }
            try {
                log.info("LocalCrypto Initializing....");
                if (!loadSecurityInfo()) {
                    initSecurityInfo();
                }
                log.info("LocalCrypto-init-SUCCESS!");
            } catch (Exception e) {
                log.error("LocalCrypto NOT Available!", e);
            }
        }
    }

    /**
     * 加载持久化的加密所需信息（目前从数据库），会将解密密钥，缓存至内存
     *
     * @return 是否加载成功
     */
    private boolean loadSecurityInfo() throws AesCryptoException {
        List<LocalCryptoInfoEntity> aesInfos;
        try {
            // get All aesInfo
            aesInfos = aesInfoDao.get(appId);
            if (CollectionUtils.isEmpty(aesInfos)) {
                log.info("LocalCrypto-load fail for load nothing. Maybe this is the app first launch.");
                return false;
            }
        } catch (Exception e) {
            log.warn("LocalCrypto-load FAIL!", e);
            return false;
        }
        CacheManager.addToCacheMap(aesInfos);
        if (CacheManager.getAesInfoCache(ALGORITHM_HEADER) == null) {
            log.info("LocalCrypto-load fail for not exist special markHeader. Maybe the algorithm has upgrade.");
            return false;
        }
        log.info("LocalCrypto-load success!");
        return true;
    }

    /**
     * 初始化本应用的加密信息，并保存至数据库的加密固件表中
     * 同时会填充缓存
     */
    private void initSecurityInfo() throws Exception {
        try {
            log.info("LocalCrypto-init:Try create new LocalCrypto BaseInfo...");
            LocalCryptoInfoEntity localCryptoInfoEntity = generateSecurity();
            aesInfoDao.save(localCryptoInfoEntity);
            CacheManager.addToCacheMap(localCryptoInfoEntity);
            log.info("LocalCrypto-init:Create new LocalCrypto BaseInfo success!");
        } catch (Exception e) {
            log.error("LocalCrypto-init:Persistent BaseInfo Fail!", e);
            throw e;
        }
    }

    /**
     * 初始化 db 加密信息表
     * <p>
     * 向量以16进制字符串编码后持久化。
     * 数据密钥本质为随机数，但需要根密钥、根加密向量的保护
     *
     * @return 本应用加密记录
     */
    private LocalCryptoInfoEntity generateSecurity() throws AesCryptoException {
        byte[] rootKeyRandomPart = ByteUtils.randomBytes(ROOT_KEY_RANDOM_LENGTH);
        String rootKeyRandomPartStr = ByteSpecification.encodeToString(rootKeyRandomPart);
        byte[] rootKey = generateDataKeyProtectKey(rootKeyRandomPart);
        // 用于加密数据密钥的 initVector 向量，写死
        byte[] dataKey = generateDataKey();
        byte[] dataKeyIv = generateDataKeyIv();
        String dbDataKey = ByteSpecification.encodeToString(AesUtil.encrypt(dataKey, rootKey, DATA_KEY_IV));
        String initVector = ByteSpecification.encodeToString(dataKeyIv);

        LocalCryptoInfoEntity entity = new LocalCryptoInfoEntity();
        entity.setAppId(appId);
        entity.setHeader(ALGORITHM_HEADER);
        entity.setDataKey(dbDataKey);
        entity.setRootKeyPart(rootKeyRandomPartStr);
        entity.setVector(initVector);
        entity.setCreateTime(new Date());
        return entity;
    }


    // ======================================== 缓存 =============================================

    /**
     * 数据密钥缓存
     * 考虑是否默认去除缓存，以达到更好的安全性
     *
     * @author lym
     */
    private static class CacheManager {

        /**
         * 是否初始化完毕
         */
        private static boolean initialized = false;

        /**
         * 用于数据加密的轻量级缓存，一般场景只有1个加密密钥
         */
        private static Map<String, AesInfoCache> cacheMap = new HashMap<>(1);


        private CacheManager() {
        }

        private static AesInfoCache getAesInfoCache(String markHeader) {
            return cacheMap.get(markHeader);
        }

        /**
         * 添加到缓存
         *
         * @param aesInfoEntity 不为 null
         * @throws AesCryptoException 转化失败
         */
        private static void addToCacheMap(LocalCryptoInfoEntity aesInfoEntity) throws AesCryptoException {
            addToCacheMap(Collections.singletonList(aesInfoEntity));
        }

        /**
         * 添加到缓存
         *
         * @param aesInfoList 不为空
         * @throws AesCryptoException 转化失败
         */
        private static void addToCacheMap(List<LocalCryptoInfoEntity> aesInfoList) throws AesCryptoException {
            for (LocalCryptoInfoEntity aesInfo : aesInfoList) {
                cacheMap.put(aesInfo.getHeader(), convertToCache(aesInfo));
            }
            initialized = true;
        }

        // keep singleTon ------------------------

        private static AesInfoCache convertToCache(LocalCryptoInfoEntity entity) throws AesCryptoException {
            String rootKeyRandomPartStr = entity.getRootKeyPart();
            byte[] rootKeyRandomPart = ByteSpecification.decodeToBytes(rootKeyRandomPartStr);

            byte[] rootKey = generateDataKeyProtectKey(rootKeyRandomPart);
            byte[] cipherDataKey = ByteSpecification.decodeToBytes(entity.getDataKey());

            byte[] dataKey = AesUtil.decrypt(cipherDataKey, rootKey, DATA_KEY_IV);
            byte[] dataIv = ByteSpecification.decodeToBytes(entity.getVector());

            return new AesInfoCache(dataKey, dataIv);
        }
    }

    public static class AesInfoCache {
        /**
         * 缓存数据密钥
         */
        private byte[] dataKey;
        /**
         * 缓存加密敏感数据的 iv
         */
        private byte[] dateIv;

        /**
         * 缓存 markHeader
         */

        public AesInfoCache(byte[] dataKey, byte[] dateIv) {
            this.dataKey = dataKey;
            this.dateIv = dateIv;
        }
    }

}
