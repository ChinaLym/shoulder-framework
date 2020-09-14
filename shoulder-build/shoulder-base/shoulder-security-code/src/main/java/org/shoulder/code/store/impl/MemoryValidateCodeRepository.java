package org.shoulder.code.store.impl;

import org.shoulder.code.dto.ValidateCodeDTO;
import org.shoulder.code.store.ValidateCodeStore;
import org.shoulder.core.context.AppContext;
import org.springframework.web.context.request.ServletWebRequest;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 基于JVM 内存的验证码存取器，适用于 token 认证，且单机部署、且没有 redis 且没有自己实现的场景
 * 一般不会有人这么设计系统架构，但万一呢，本类作为临时bean实现。若真的出现这种场景，推荐自行替代，本类有以下弊端：
 * - 不能支持同一用户同时登录，且同时请求带同一类型验证码的资源——极端情况影响用户体验
 * - 不带过期时间，无清理机制——可能导致OOM
 *
 * @author lym
 */
public class MemoryValidateCodeRepository implements ValidateCodeStore {

    private final ConcurrentMap<String, ValidateCodeDTO> cache = new ConcurrentHashMap<>();

    @Override
    public void save(ServletWebRequest request, ValidateCodeDTO code, String validateCodeType) {
        cache.put(buildCacheKey(validateCodeType), code);
    }


    @Override
    public ValidateCodeDTO get(ServletWebRequest request, String validateCodeType) {
        return cache.get(buildCacheKey(validateCodeType));
    }

    @Override
    public void remove(ServletWebRequest request, String codeType) {
        cache.remove(buildCacheKey(codeType));
    }

    /**
     * 验证码放入 session 的 key
     */
    protected String buildCacheKey(String validateCodeType) {
        return AppContext.getTenantId() + ":" + AppContext.getUserId() + ":" + validateCodeType.toUpperCase();
    }

}
