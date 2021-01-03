package com.example.demo1.enums;

/**
 * 本工程的模块
 *
 * @author lym
 */
public enum Module {

    /**
     * 登录模块
     */
    SIGN_IN("000"),

    /**
     * 注册模块
     */
    SIGN_UP("001"),

    ;

    Module(String code) {
        moduleCode = code;
    }

    public final String moduleCode;


}
