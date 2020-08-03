package org.shoulder.crypto.negotiation;
// 密钥交换，默认基于ecc非对称加密( ecdh 算法)，可配置为 RSA2048( dh 算法)

/**
 * 主要 4 部分实现自动化（依赖 jackson ）
 * 发送 http 请求前先确保已经协商，把参数中安全字段加密（序列化）
 * 发送 http 请求后将返回值中安全字段解密（反序列化）
 * <p>
 * 接收 http 请求前把参数中安全字段解密（反序列化）
 * 返回 http 请求前把返回值中安全字段加密（序列化）
 */

