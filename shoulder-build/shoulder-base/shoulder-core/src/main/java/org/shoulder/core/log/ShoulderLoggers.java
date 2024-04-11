package org.shoulder.core.log;

/**
 * loggerNames
 *
 * @author lym
 */
public interface ShoulderLoggers {

    /**
     * 通用能力 / 基础能力 相关
     * 如对象转换、Json、String、Array工具类等
     */
    Logger SHOULDER_DEFAULT = LoggerFactory.getLogger("SHOULDER-DEFAULT");

    /**
     * 通用能力 / 基础能力 相关
     */
    Logger SHOULDER_ERROR = LoggerFactory.getLogger("SHOULDER-ERROR");

    /**
     * 并发 / 多线程增强相关
     */
    Logger SHOULDER_THREADS = LoggerFactory.getLogger("SHOULDER-THREADS");

    /**
     * 配置变化、启动激活了怎样的配置
     */
    Logger SHOULDER_CONFIG = LoggerFactory.getLogger("SHOULDER-CONFIG");

    /**
     * 发起请求相关增强
     */
    Logger SHOULDER_CLIENT = LoggerFactory.getLogger("SHOULDER-CLIENT");

    /**
     * 发起请求相关增强
     */
    Logger SHOULDER_CLIENT_ERROR = LoggerFactory.getLogger("SHOULDER-CLIENT-ERROR");

    /**
     * shoulder 内提供的与 web server 相关增强功能记录
     * 增强mvc、增加切面等
     */
    Logger SHOULDER_WEB = LoggerFactory.getLogger("SHOULDER-SERVER");

    /**
     * shoulder 内提供的与 web server 相关增强功能报错记录
     */
    Logger SHOULDER_WEB_ERROR = LoggerFactory.getLogger("SHOULDER-SERVER-ERROR");

    /**
     * web 安全-认证授权相关
     */
    Logger SHOULDER_SECURITY = LoggerFactory.getLogger("SHOULDER-SECURITY");

    /**
     * web 安全-认证授权相关
     */
    Logger SHOULDER_SECURITY_ERROR = LoggerFactory.getLogger("SHOULDER-SECURITY-ERROR");

    /**
     * 通用加解密相关
     * 通用加密、密钥协商等
     */
    Logger SHOULDER_CRYPTO = LoggerFactory.getLogger("SHOULDER-CRYPTO");
}
