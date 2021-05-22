package org.shoulder.core.context;

import org.apache.commons.collections4.MapUtils;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;
import org.shoulder.core.util.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.security.Principal;
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

    private static final ThreadLocal<Map<String, Object>> THREAD_LOCAL = ThreadLocal.withInitial(() -> new HashMap<>(ShoulderContextKey.KEY_NUM));

    /**
     * 获取 用户标识
     */
    public static String getUserId() {
        Object userId = get(ShoulderContextKey.USER_ID);
        return userId == null ? null : String.valueOf(userId);
    }

    /**
     * 设置用户标识
     *
     * @param userId 用户标识
     */
    public static void setUserId(Object userId) {
        log.trace("setUserId ({})", userId);
        set(ShoulderContextKey.USER_ID, userId);
    }


    /**
     * 当前用户名称
     *
     * @return 账户信息
     */
    public static String getUserName() {
        return (String) get(ShoulderContextKey.USER_NAME);
    }

    /**
     * 当前用户名称
     *
     * @param name 用户名称
     */
    public static void setUserName(String name) {
        log.trace("setName ({})", name);
        set(ShoulderContextKey.USER_NAME, name);
    }


    /**
     * 获取认证凭证
     *
     * @return 认证凭证
     */
    public static Principal getAuthentication() {
        return (Principal) get(ShoulderContextKey.AUTHENTICATION);
    }

    /**
     * 设置认证凭证
     *
     * @param principal 认证凭证
     */
    public static void setAuthentication(Principal principal) {
        log.trace("setToken ({})", principal);
        set(ShoulderContextKey.AUTHENTICATION, principal);
    }


    /**
     * 获取语言标识
     *
     * @return 语言标识
     */
    public static Locale getLocale() {
        return (Locale) get(ShoulderContextKey.LOCALE);
    }

    /**
     * 设置语言标识
     *
     * @param locale 语言标识
     */
    public static void setLocale(@Nonnull Locale locale) {
        log.trace("setLocale ({})", locale);
        set(ShoulderContextKey.LOCALE, locale);
    }

    /**
     * 获取租户标识
     *
     * @return 租户标识
     */
    public static String getTenantCode() {
        return (String) get(ShoulderContextKey.TENANT);
    }

    /**
     * 设置租户标识
     *
     * @param tenantId 租户标识
     */
    public static void setTenantCode(String tenantId) {
        log.trace("setTenant ({})", tenantId);
        set(ShoulderContextKey.TENANT, tenantId);
    }


    /**
     * 获取链路追踪标识
     *
     * @return traceId
     */
    public static String getTranceId() {
        return (String) get(ShoulderContextKey.TRACE_ID);
    }

    /**
     * 设置链路追踪标识
     *
     * @param tranceId 链路追踪标识
     */
    public static void setTranceId(String tranceId) {
        set(ShoulderContextKey.TRACE_ID, tranceId);
    }

    /**
     * 从上下文中获取值
     *
     * @param key key
     * @return 值
     */
    @Nullable
    public static Object get(String key) {
        Map<String, Object> map = THREAD_LOCAL.get();
        if (MapUtils.isEmpty(map)) {
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
    public static void set(String key, @Nullable Object value) {
        if (StringUtils.isEmpty(key)) {
            return;
        }
        Map<String, Object> map = THREAD_LOCAL.get();
        if (map == null) {
            map = new HashMap<>();
            THREAD_LOCAL.set(map);
        }
        if (value == null) {
            map.remove(key);
        } else {
            map.put(key, value);
        }
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
    public static Object remove(String key) {
        return THREAD_LOCAL.get() == null ? null : THREAD_LOCAL.get().remove(key);
    }

    /**
     * 以参数重置全部上下文，不推荐使用
     *
     * @param contextMap 上下文属性
     */
    public static void setAttributes(Map<String, Object> contextMap) {
        THREAD_LOCAL.set(contextMap);
    }

    public static Map<String, Object> getAll() {
        return THREAD_LOCAL.get();
    }

    public static void set(Map<String, Object> attributes) {
        THREAD_LOCAL.set(attributes);
    }

}
