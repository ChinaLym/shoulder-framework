package org.shoulder.core.context;

import lombok.extern.shoulder.SLog;
import org.shoulder.core.util.StringUtils;

import java.util.HashMap;
import java.util.Map;


/**
 * 当前上下文中的值保存
 *
 * @author lym
 */
@SLog
public class BaseContextHolder {

    private static final ThreadLocal<Map<String, String>> THREAD_LOCAL = ThreadLocal.withInitial(() -> new HashMap<>(ShoulderContextKey.KEY_NUM));

    private static String serviceId;

    public static String getServiceId() {
        return serviceId;
    }
    public static void setServiceId(String serviceId) {
        BaseContextHolder.serviceId = serviceId;
    }


    public static void setLocalMap(Map<String, String> threadLocalMap) {
        THREAD_LOCAL.set(threadLocalMap);
    }

    /**
     * userId
     */
    public static Long getUserId() {
        return Long.valueOf(getLocalMap().get(ShoulderContextKey.JWT_KEY_USER_ID));
    }
    public static void setUserId(Long userId) {
        set(ShoulderContextKey.JWT_KEY_USER_ID, userId);
    }
    public static void setUserId(String userId) {
        set(ShoulderContextKey.JWT_KEY_USER_ID, userId);
    }

    /**
     * 用户名 name
     */
    public static String getAccount() {
        return getLocalMap().get(ShoulderContextKey.JWT_KEY_ACCOUNT);
    }
    public static void setAccount(String name) {
        set(ShoulderContextKey.JWT_KEY_ACCOUNT, name);
    }


    /**
     * 登录的账号
     */
    public static String getName() {
        return getLocalMap().get(ShoulderContextKey.JWT_KEY_NAME);
    }
    public static void setName(String account) {
        set(ShoulderContextKey.JWT_KEY_NAME, account);
    }

    /**
     * 获取认证 token
     */
    public static String getToken() {
        return getLocalMap().get(ShoulderContextKey.HEADER_TOKEN);
    }
    public static void setToken(String token) {
        set(ShoulderContextKey.HEADER_TOKEN, token);
    }

    /**
     * 获取租户标识
     */
    public static String getTenant() {
        return getLocalMap().get(ShoulderContextKey.TENANT);
    }
    public static void setTenant(String val) {
        set(ShoulderContextKey.TENANT, val);
    }

    /**
     * 灰度发布版本
     */
    public static String getGrayVersion() {
        return getLocalMap().get(ShoulderContextKey.GRAY_VERSION);
    }
    public static void setGrayVersion(String val) {
        set(ShoulderContextKey.GRAY_VERSION, val);
    }

    /**
     * 链路追踪
     */
    public static String getTranceId() {
        return getLocalMap().get(ShoulderContextKey.GRAY_VERSION);
    }
    public static void setTranceId(String tranceId) {
        set(ShoulderContextKey.GRAY_VERSION, tranceId);
    }

    public static Map<String, String> getLocalMap() {
        return THREAD_LOCAL.get();
    }

    public static void set(String key, Object value) {
        Map<String, String> map = getLocalMap();
        map.put(key, value == null ? StringUtils.EMPTY : value.toString());
    }

    public static void clean() {
        THREAD_LOCAL.remove();
    }
}
