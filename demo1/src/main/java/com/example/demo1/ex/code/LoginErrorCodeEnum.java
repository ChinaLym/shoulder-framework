package com.example.demo1.ex.code;

import com.example.demo1.ex.LoginModuleErrorCode;

public enum LoginErrorCodeEnum implements LoginModuleErrorCode {

    /**
     * 登录相关错误
     */
    USER_NOT_EXISTS("00", "user not exists"),

    /**
     * 用户已经被锁定
     */
    USER_LOCKED("01", "user locked"),


    ;

    LoginErrorCodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    String code;

    String message;

    @Override
    public String specialErrorCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
