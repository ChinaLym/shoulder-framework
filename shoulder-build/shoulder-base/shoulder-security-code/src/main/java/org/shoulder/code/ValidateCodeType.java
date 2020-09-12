package org.shoulder.code;

/**
 * 验证码类型
 *
 * @author lym
 */
public interface ValidateCodeType {

    /**
     * 类型名称，处理器标识，请求参数名
     *
     * @return 不能为空字符串
     */
    String getType();

}
