/**
 * 简化在 Spring 中的使用，减少学习成本，编码量：只需要关注 {@link org.shoulder.crypto.negotiation.support.Sensitive}、
 * {@link org.shoulder.crypto.negotiation.support.SecurityRestTemplate}
 * <p>
 * <p>
 * DTO定义、todo 错误码定义
 * 发起密钥协商
 * 处理密钥协商请求
 * 处理密钥协商失败
 * 处理密钥协商异常、错误码、日志
 * <p>
 * 支持 @Sensitive 注解（主要有以下 4 部分）
 * <p>
 * 发送 http 请求前
 * 确保已经协商(SecurityRestTemplate.EnsureNegotiatedRequestCallback)
 * 把参数中敏感字段加密（SensitiveRequestEncryptMessageConverter）
 * 发送 http 请求后将返回值中安全字段解密（SensitiveResponseDecryptInterceptor）
 * <p>
 * 接收 http 请求前
 * 校验请求是否安全合法（SensitiveRequestDecryptHandlerInterceptor）
 * 解密参数中的敏感字段（SensitiveRequestDecryptAdvance）
 * 返回 http 请求前把返回值中安全字段加密（SensitiveResponseEncryptAdvice）
 */
package org.shoulder.crypto.negotiation.support;
