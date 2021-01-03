package com.example.demo1.ex;

import com.example.demo1.enums.Module;
import org.shoulder.core.exception.BusinessErrorCode;

/**
 * 假设这个项目中约定，前3位表示组件表示,中间3位代表模块标识，剩余位数表示错误码
 * 所有登录相关错误码枚举应实现该接口
 *
 * @author lym
 */
public interface LoginModuleErrorCode extends BusinessErrorCode {

    /**
     * 模块标识，用于标记这是登录模块
     */
    @Override
    default String moduleCode() {
        return Module.SIGN_IN.moduleCode;
    }

    /**
     * 子类必须实现具体码，且只需实现该方法
     */
    @Override
    String specialErrorCode();

}
