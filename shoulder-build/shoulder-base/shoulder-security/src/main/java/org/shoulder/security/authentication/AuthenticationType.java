package org.shoulder.security.authentication;

/**
 * 认证方式
 *
 * @author lym
 */
public enum AuthenticationType {

    /**
     * 通过 session 认证
     */
    SESSION,

    /**
     * 通过 token 认证
     */
    TOKEN;

}
