package org.shoulder.security.code.img;

import org.shoulder.code.ValidateCodeType;
import org.shoulder.code.consts.ValidateCodeConsts;

/**
 * 图片验证码类型
 * 统一实现图片验证码相关的 getType() 方法
 *
 * @author lym
 */
public interface ImageValidateCodeType extends ValidateCodeType {

    /**
     * 类型名称，处理器标识，请求参数名
     *
     * @return 不能为空字符串
     */
    @Override
    default String getType() {
        return ValidateCodeConsts.IMAGE;
    }

}
