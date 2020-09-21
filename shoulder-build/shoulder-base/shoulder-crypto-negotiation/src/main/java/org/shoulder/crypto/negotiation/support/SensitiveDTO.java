package org.shoulder.crypto.negotiation.support;

/**
 * 标志性接口，既可作为安全传输参数，又能作为安全响应。
 * 注意：支持但不推荐使用同一个DTO即作为参数又作为返回值
 *
 * @author lym
 */
public interface SensitiveDTO extends SensitiveResponse, SensitiveParam {
}
