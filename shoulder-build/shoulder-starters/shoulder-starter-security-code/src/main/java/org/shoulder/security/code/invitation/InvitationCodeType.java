package org.shoulder.security.code.invitation;

import org.shoulder.code.ValidateCodeType;
import org.shoulder.code.consts.ValidateCodeConsts;

/**
 * invitation验证码类型
 * 统一实现invitation验证码相关的 getType() 方法
 *
 * @author lym
 */
public interface InvitationCodeType extends ValidateCodeType {

    /**
     * 类型名称，处理器标识，请求参数名
     *
     * @return 不能为空字符串
     */
    @Override
    default String getType() {
        return ValidateCodeConsts.INVITATION;
    }

}
