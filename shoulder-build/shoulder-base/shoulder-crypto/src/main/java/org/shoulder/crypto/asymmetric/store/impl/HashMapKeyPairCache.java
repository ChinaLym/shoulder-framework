package org.shoulder.crypto.asymmetric.store.impl;

import org.shoulder.crypto.asymmetric.dto.KeyPairDto;
import org.shoulder.crypto.asymmetric.exception.NoSuchKeyPairException;
import org.shoulder.crypto.asymmetric.store.KeyPairCache;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

import javax.annotation.PostConstruct;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * RSA 秘钥存储-本地存储，适合单机部署场景
 * @author lym
 */
public class HashMapKeyPairCache implements KeyPairCache {

	protected ConcurrentMap<String, KeyPairDto> store = new ConcurrentHashMap<>();

	@PostConstruct
	public void init() {
		LoggerFactory.getLogger(HashMapKeyPairCache.class).debug("LocalKeyPairCache init.");
	}

	@Override
	public void set(String id, @NonNull KeyPairDto keyPair) {
		store.put(id, keyPair);
	}

	@Override
	public KeyPairDto get(String id) throws NoSuchKeyPairException {
		KeyPairDto keyPair = store.get(id);
		if(keyPair != null){
			return keyPair;
		}else {
			throw new NoSuchKeyPairException("can't found keyPair id=" + id);
		}
	}

	@Override
	public void destroy() {
		store.clear();
	}

}