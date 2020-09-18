package org.shoulder.core.context;

import org.apache.commons.collections4.MapUtils;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;
import org.shoulder.core.util.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 当前应用（请求）上下文中的值，作为 Holder 的角色，维护当前请求中一些常用的数据。
 * 推荐在调用的地方记录 debug 日志
 *
 * @author lym
 */
public class AppContext {

    private static final Logger log = LoggerFactory.getLogger(AppContext.class);

    private static final ThreadLocal<Map<String, String>> THREAD_LOCAL = ThreadLocal.withInitial(() -> new HashMap<>(ShoulderContextKey.KEY_NUM));

    /**
     * userId
     */
    public static String getUserId() {
        return get(ShoulderContextKey.JWT_KEY_USER_ID);
    }

    /**
     * 设置用户标识
     *
     * @param userId 用户标识
     */
    public static void setUserId(Object userId) {
        log.trace("setUserId ({})", userId);
        set(ShoulderContextKey.JWT_KEY_USER_ID, String.valueOf(userId));
    }

    /**
     * 设置用户标识
     *
     * @param userId 用户标识
     */
    public static void setUserId(String userId) {
        set(ShoulderContextKey.JWT_KEY_USER_ID, userId);
    }

    /**
     * 用户名 name
     *
     * @return 账户信息
     */
    public static String getAccount() {
        return get(ShoulderContextKey.JWT_KEY_ACCOUNT);
    }

    /**
     * 设用户账户
     *
     * @param account 设用户账户
     */
    public static void setAccount(String account) {
        log.trace("setAccount ({})", account);
        set(ShoulderContextKey.JWT_KEY_ACCOUNT, account);
    }

    /**
     * 当前用户名称
     *
     * @return 账户信息
     */
    public static String getName() {
        return get(ShoulderContextKey.JWT_KEY_NAME);
    }

    /**
     * 当前用户名称
     *
     * @param name 用户名称
     */
    public static void setName(String name) {
        log.trace("setName ({})", name);
        set(ShoulderContextKey.JWT_KEY_NAME, name);
    }

    /**
     * 获取语言标识
     *
     * @return 语言标识
     */
    public static Locale getLocale() {
        return StringUtils.parseLocale(get(ShoulderContextKey.Locale));
    }

    /**
     * 设置语言标识
     *
     * @param locale 语言标识
     */
    public static void setLocale(@NonNull Locale locale) {
        log.trace("set Locale ({})", locale);
        set(ShoulderContextKey.Locale, locale.toString());
    }

    /**
     * 获取认证 token
     *
     * @return token
     */
    public static String getToken() {
        return get(ShoulderContextKey.HEADER_TOKEN);
    }

    /**
     * 设置认证 token
     *
     * @param token 认证 token
     */
    public static void setToken(String token) {
        log.trace("setToken ({})", token);
        set(ShoulderContextKey.HEADER_TOKEN, token);
    }

    /**
     * 获取租户标识
     *
     * @return 租户标识
     */
    public static String getTenantId() {
        return get(ShoulderContextKey.TENANT);
    }

    /**
     * 设置租户标识
     *
     * @param tenantId 租户标识
     */
    public static void setTenant(String tenantId) {
        log.trace("setTenant ({})", tenantId);
        set(ShoulderContextKey.TENANT, tenantId);
    }


    /**
     * 获取链路追踪标识
     *
     * @return traceId
     */
    public static String getTranceId() {
        return get(ShoulderContextKey.GRAY_VERSION);
    }

    /**
     * 设置链路追踪标识
     *
     * @param tranceId 链路追踪标识
     */
    public static void setTranceId(String tranceId) {
        set(ShoulderContextKey.GRAY_VERSION, tranceId);
    }

    /**
     * 从上下文中获取值
     *
     * @param key key
     * @return 值
     */
    @Nullable
    public static String get(String key) {
        Map<String, String> map = THREAD_LOCAL.get();
        if (MapUtils.isNotEmpty(map)) {
            return null;
        }
        return map.get(key);
    }

    /**
     * 向上下文中设置值，用于扩展
     *
     * @param key   key，若 key 为空，则直接返回
     * @param value value
     */
    public static void set(String key, @Nullable String value) {
        if (StringUtils.isEmpty(key)) {
            return;
        }
        Map<String, String> map = THREAD_LOCAL.get();
        if (map == null) {
            map = new HashMap<>();
            THREAD_LOCAL.set(map);
        }
        map.put(key, value);
    }

    /**
     * 清理上下文信息
     */
    public static void clean() {
        THREAD_LOCAL.remove();
    }

    /**
     * 清理上下文信息
     */
    public static String remove(String key) {
        return THREAD_LOCAL.get() == null ? null : THREAD_LOCAL.get().remove(key);
    }

    /**
     * 以参数重置全部上下文，不推荐使用
     *
     * @param contextMap 上下文属性
     */
    public static void setAttributes(Map<String, String> contextMap) {
        THREAD_LOCAL.set(contextMap);
    }

}
