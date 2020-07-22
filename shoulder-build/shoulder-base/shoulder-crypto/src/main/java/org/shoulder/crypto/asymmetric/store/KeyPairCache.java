package org.shoulder.crypto.asymmetric.store;

import org.shoulder.crypto.asymmetric.dto.KeyPairDto;
import org.shoulder.crypto.asymmetric.exception.NoSuchKeyPairException;
import org.springframework.lang.NonNull;

/**
 * 密钥对存储
 * 		如果分布式部署，需要共享存储
 *
 * @author lym
 */
public interface KeyPairCache {
	/**
	 * 存储密钥对
	 * @param id		id
	 * @param keyPairDto	密钥对
	 */
	void set(String id, @NonNull KeyPairDto keyPairDto);

	/**
	 * 获取密钥对
	 * @param id 	id
	 * @return 		密钥对
	 * @throws NoSuchKeyPairException 密钥对缺失
	 */
	@NonNull
	KeyPairDto get(String id) throws NoSuchKeyPairException;

	/**
	 * 销毁
	 */
	void destroy();
}
