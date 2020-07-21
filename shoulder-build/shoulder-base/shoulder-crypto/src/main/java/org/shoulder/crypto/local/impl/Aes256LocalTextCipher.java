package org.shoulder.crypto.local.impl;

import org.shoulder.core.constant.ByteSpecification;
import org.shoulder.core.util.ByteUtils;
import org.shoulder.crypto.aes.AesUtil;
import org.shoulder.crypto.aes.exception.AesCryptoException;
import org.shoulder.crypto.digest.Sha256Utils;
import org.shoulder.crypto.local.JudgeAbleLocalTextCipher;
import org.shoulder.crypto.local.entity.LocalCryptoInfoEntity;
import org.shoulder.crypto.local.exception.HeaderNotMatchDecryptException;
import org.shoulder.crypto.local.repository.LocalCryptoInfoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * 本地数据加解密
 *
 * 加解密算法 密钥分级存储的 AES256，类型：AES/CBC/PKCS5Padding.
 *
 * 	<p> 理论：
 * 		加解密：将 dataKey作为秘钥, dataIv作为偏移向量，使用 AES256 算法将敏感数据 data 加解密。即由本类职责。
 * 		由上可知，加解密依赖 dataKey 和 dataIv	，因此需要保证每次启动时可以拿到 dataKey 与 dataIv，且dataKey必须被保护。
 * 	 dataKey 与 dataIv 的存取方式：该职责由 {@link LocalCryptoInfoRepository} 和 {@link LocalCryptoInfoEntity}负责实现。
 * 		数据秘钥 dataKey 的保护：Aes256(dataKey, SHA256(rootKey, random), rootKeyIv)，保护方式同敏感数据，由另一个秘钥 rootKey 和一个加密向量保护，同样为
 * 		Aes256算法。
 *  rootKey、rootKeyIv 的保存一样由 {@link LocalCryptoInfoRepository} 和 {@link LocalCryptoInfoEntity}负责实现。
 * 		rootKey 的保护：持久化的值为 SHA256(rootKey, random) 而不是 rootKey 本身
 * 		rootKey 的生成：32个字符即 256位，详见 {@link Aes256LocalTextCipher#generateRootKey}
 *
 * 	<ul>
 * 	    <li> 敏感数据（data）：需要被加密的数据
 *
 * 		<li> 数据密钥：用于保护本地敏感信息。因为是基于AES算法，数据密钥仅在项目部署时生成一次（一个随机数），后续不能变更。因此需要持久化，由 AesInfoRepository 完成持久化。
 *
 * 		<li>根密钥：用于保护数据密钥 —— 持久化数据密钥时，通过根密钥加密数据密钥。
 *
 * 		<li>密钥向量：用于加密数据秘钥、加密敏感数据
 *
 * </ul>
 *
 * @author lym
 */
public class Aes256LocalTextCipher implements JudgeAbleLocalTextCipher {

	private final static Logger log = LoggerFactory.getLogger(Aes256LocalTextCipher.class);

	private String appId;

	/** 长度为6的加密标记，与加密版本挂钩，该字段的存在支持升级版本。AES256 2^8 */
	private static final String ALGORITHM_HEADER = "${a8} ";

	/** 秘钥持久化依赖：用于获取持久化的加密信息 */
	private final LocalCryptoInfoRepository aesInfoDao;

	/** 保护数据密钥的 iv */
	private static final byte[] DATA_KEY_IV = "shoulder:Cn-Lym!".getBytes(CHARSET_UTF_8);

	private static final byte[] ROOT_KEY_FINAL_PART = "shoulderFramework:CN-Lym".getBytes(CHARSET_UTF_8);
	private static final int aesKeyLength = 256;
	private static final int needLength = aesKeyLength - ROOT_KEY_FINAL_PART.length;


	public Aes256LocalTextCipher(LocalCryptoInfoRepository aesInfoRepository, String appId) {
		this.aesInfoDao = aesInfoRepository;
		this.appId = appId;
	}

	// ======================================== 对外接口 ============================================


	@Override
	public String encrypt(@NonNull String text) throws AesCryptoException {
		ensureEncryption();
		AesInfoCache cacheInfo = CacheManager.getAesInfoCache(ALGORITHM_HEADER);
		byte[] encryptResult = AesUtil.encrypt(text.getBytes(CHARSET_UTF_8), cacheInfo.dataKey,
				cacheInfo.dateIv);
		return ALGORITHM_HEADER + ByteSpecification.encodeToString(encryptResult);
	}

	@Override
	public String decrypt(@NonNull String cipherText) throws AesCryptoException, HeaderNotMatchDecryptException {
		ensureEncryption();
        String[] cipherTextAndHeader = splitHeader(cipherText);
        String cipherTextHeader = cipherTextAndHeader[0];
        String realCipherText = cipherTextAndHeader[1];
        AesInfoCache cacheInfo = CacheManager.getAesInfoCache(cipherTextHeader);
        if(cacheInfo == null){
            throw new HeaderNotMatchDecryptException("cipher's markHeader is " + cipherTextHeader);
        }
		byte[] decryptData = AesUtil.decrypt(Base64.getDecoder().decode(realCipherText), cacheInfo.dataKey, cacheInfo.dateIv);
		return new String(decryptData, CHARSET_UTF_8);
	}

    /**
     * 分割为 头部、真正密文
     * @param cipherText 存储的密文
     * @return result[0] Header result[1]realCipherText
     */
	private String[] splitHeader(String cipherText){
        String header = cipherText.substring(0, 6);
        String realCipher = cipherText.substring(6);
		return new String[]{header, realCipher};
    }

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

	/**
	 * 确保加密前所需的变量已经初始化：检查加密配件缓存是否已经初始化
	 * 若内存{@code this.cache}中秘钥信息为空：
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


	// =============================== 初始化流程 ====================================

	/**
	 * 加载持久化的加密所需信息（目前从数据库），会将解密秘钥，缓存至内存
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

	// ============================ 初始化所需的数据生成算法 =====================================

	/**
	 * 初始化 db 加密信息表
	 * <p>
	 * 向量以16进制字符串编码后持久化。
	 * 数据秘钥本质为随机数，但需要根秘钥、根加密向量的保护
	 *
	 * @return 本应用加密记录
	 */
	private LocalCryptoInfoEntity generateSecurity() throws AesCryptoException {
		byte[] rootKeyRandomPart = ByteUtils.randomBytes(needLength);
		String rootKeyRandomPartStr = ByteSpecification.encodeToString(rootKeyRandomPart);
		byte[] rootKey = generateRootKey(rootKeyRandomPart);
		// 用于加密数据秘钥的 iv 向量，写死
        byte[] dataKey = generateDataKey();
        byte[] dataKeyIv = generateDataKeyIv();
		String dbDataKey = ByteSpecification.encodeToString(AesUtil.encrypt(dataKey, rootKey, DATA_KEY_IV));

		String iv = ByteSpecification.encodeToString(dataKeyIv);
		return new LocalCryptoInfoEntity(
				UUID.randomUUID().toString(), appId,
				dbDataKey, rootKeyRandomPartStr, iv,
				ALGORITHM_HEADER, new Date()
		);
	}

	/**
	 * 生成 rootKey ： SHA256(rootKeyParts)
	 *	根密钥总长度为 256 位，其中一部分写死在代码中。另一部分启动时随机生成，以确保每个应用中不出现重复。
	 *	 因为 AES256算法 要求秘钥部件为总长度等于256位( Java 中一个 byte 为 8位)
	 *	 这里需要 64 位
	 *
	 * @param randomPart rootKey 的随机部分
	 * @return rootKey
	 */
	private static byte[] generateRootKey(byte[] randomPart) {
		assert randomPart.length == needLength;

		byte[] rootKey = new byte[256];
		ByteUtils.copy(ROOT_KEY_FINAL_PART, 0, rootKey, 0, rootKey.length);
		ByteUtils.copy(randomPart, 0, rootKey, ROOT_KEY_FINAL_PART.length, needLength);

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
	 * @return 数据秘钥
	 */
	private static byte[] generateDataKey() {
		return ByteUtils.randomBytes(32);
	}



	// ======================================== 缓存 =============================================

	/**
	 * 数据秘钥缓存
	 * @author lym
	 */
	private static class CacheManager {

		/** 是否初始化完毕 */
		private static boolean initialized = false;

        /** 用于数据加密的轻量级缓存 */
        private static Map<String, AesInfoCache> cacheMap;


        private static AesInfoCache getAesInfoCache(String markHeader){
            return cacheMap.get(markHeader);
        }

		/**
		 * 添加到缓存
		 * @param aesInfoEntity 不为 null
		 * @throws AesCryptoException 转化失败
		 */
        private static void addToCacheMap(LocalCryptoInfoEntity aesInfoEntity) throws AesCryptoException {
            addToCacheMap(Collections.singletonList(aesInfoEntity));
        }

		/**
		 * 添加到缓存
		 * @param aesInfoList 不为空
		 * @throws AesCryptoException 转化失败
		 */
		private static void addToCacheMap(List<LocalCryptoInfoEntity> aesInfoList) throws AesCryptoException {
            if(cacheMap == null){
                cacheMap = new HashMap<>(aesInfoList.size());
            }
            for (LocalCryptoInfoEntity aesInfo : aesInfoList ) {
                cacheMap.put(aesInfo.getHeader(), convertToCache(aesInfo));
            }
            initialized = true;
        }

        private static AesInfoCache convertToCache(LocalCryptoInfoEntity entity) throws AesCryptoException {
			String rootKeyRandomPartStr = entity.getRootKeyPart();
			byte[] rootKeyRandomPart = ByteSpecification.decodeToBytes(rootKeyRandomPartStr);

            byte[] rootKey = generateRootKey(rootKeyRandomPart);
            byte[] cipherDataKey = ByteSpecification.decodeToBytes(entity.getDataKey());

            byte[] dataKey = AesUtil.decrypt(cipherDataKey, rootKey, DATA_KEY_IV);
            byte[] dataIv = ByteSpecification.decodeToBytes(entity.getIv());

            return new AesInfoCache(dataKey, dataIv);
        }

        // keep singleTon ------------------------

		private CacheManager() {}
	}

	public static class AesInfoCache {
		/** 缓存数据秘钥 */
		private byte[] dataKey;
		/** 缓存加密敏感数据的 iv */
		private byte[] dateIv;
		/** 缓存 markHeader */

        public AesInfoCache(byte[] dataKey, byte[] dateIv) {
            this.dataKey = dataKey;
            this.dateIv = dateIv;
		}
	}

}
