package org.shoulder.security.code.sms;

import org.shoulder.code.ValidateCodeType;
import org.shoulder.code.consts.ValidateCodeConsts;

/**
 * 短信验证码类型
 * 统一实现短信验证码相关的 getType() 方法
 *
 * @author lym
 * */
public interface SmsValidateCodeType extends ValidateCodeType {

    /**
     * 类型名称，处理器标识，请求参数名
     * @return 不能为空字符串
     */
    @Override
    default String getType(){
        return ValidateCodeConsts.SMS;
    }

}
