package org.shoulder.code.store.impl;

import org.apache.commons.lang3.StringUtils;
import org.shoulder.code.dto.ValidateCodeDTO;
import org.shoulder.code.store.ValidateCodeStore;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.context.request.ServletWebRequest;

import java.util.concurrent.TimeUnit;

/**
 * 基于redis的验证码存取器，避免由于没有session导致无法存取验证码的问题
 *
 * @author lym
 */
public class RedisValidateCodeRepository implements ValidateCodeStore {

    private static final String DEFAULT_KEY_PREFIX = "CAPTCHA_CODE:";

    private RedisTemplate<Object, Object> redisTemplate;

    /**
     * 请求中终端唯一码名称，默认为 deviceId
     * 在安卓中通常使用 IMEI 码值作为其值
     */
    private final String unionCodePramName;

    @SuppressWarnings("unchecked")
    public RedisValidateCodeRepository(RedisTemplate redisTemplate, String unionCodePramName) {
        this.redisTemplate = redisTemplate;
        this.unionCodePramName = unionCodePramName;
    }

    @Override
    public void save(ServletWebRequest request, ValidateCodeDTO code, String type) {
        // 只存值与类型，不存图片等
        ValidateCodeDTO codeCopy = new ValidateCodeDTO(code.getCode(), code.getExpireTime());
        redisTemplate.opsForValue().set(buildKey(request, type), codeCopy, 30, TimeUnit.MINUTES);
    }


    @Override
    public ValidateCodeDTO get(ServletWebRequest request, String type) {
        Object value = redisTemplate.opsForValue().get(buildKey(request, type));
        if (value == null) {
            return null;
        }
        return (ValidateCodeDTO) value;
    }


    @Override
    public void remove(ServletWebRequest request, String type) {
        redisTemplate.delete(buildKey(request, type));
    }

    /**
     * 构造在 redis 中存储验证码的 key
     *
     * @param request request + response
     * @param type    验证码类型
     * @return 在 redis 中存储验证码的 key
     */
    protected String buildKey(ServletWebRequest request, String type) {
        String unionCode = request.getHeader(unionCodePramName);
        if (StringUtils.isBlank(unionCode)) {
            throw new IllegalStateException("please add the parameter(" + unionCodePramName + ") in your requests!");
        }
        return DEFAULT_KEY_PREFIX + unionCode + ":" + type;
    }

}
