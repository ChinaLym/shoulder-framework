package org.shoulder.crypto.negotiation.support;

/**
 * 简化在 Spring 中的使用，减少学习成本，编码量：只需要关注 {@link org.shoulder.crypto.negotiation.support.Sensitive}、
 * {@link org.shoulder.crypto.negotiation.support.SecurityRestTemplate}
 *
 *
 * DTO定义、todo 错误码定义
 * 发起秘钥协商
 * 处理秘钥协商请求
 * 处理秘钥协商失败
 * 处理秘钥协商异常、错误码、日志
 *
 * 支持 @Sensitive 注解（主要有以下 4 部分）
 * <p>
 * 发送 http 请求前
 *      确保已经协商(SecurityRestTemplate.EnsureNegotiatedRequestCallback)
 *      把参数中敏感字段加密（SensitiveRequestEncryptMessageConverter）
 * 发送 http 请求后将返回值中安全字段解密（SensitiveResponseDecryptInterceptor）
 * <p>
 * 接收 http 请求前校验请求是否安全合法，并把参数中安全字段解密（SensitiveRequestDecryptHandlerInterceptor）
 * 返回 http 请求前把返回值中安全字段加密（SensitiveResponseEncryptAdvice）
 */

