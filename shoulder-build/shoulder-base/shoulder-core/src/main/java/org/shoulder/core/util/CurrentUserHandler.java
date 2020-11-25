package org.shoulder.core.util;

/**
 * 当前用户解析器
 *
 * @author lym
 * todo @deprecated use AppContext
 */
public interface CurrentUserHandler {

    /**
     * 获取当前用户，如果获取不到则返回默认的系统用户
     *
     * @return 当前用户
     */
    <T> T getCurrentUser();

    /**
     * 清理用户信息
     */
    void clean();
}
