package com.example.demo1.ex.code;

import org.shoulder.core.exception.ErrorCode;


/**
 * 第二个错误码枚举示例
 *
 * @author lym
 */
public enum DemoErrorCodeEnum2 implements ErrorCode {

    /**
     * @desc 报名者年龄不符合要求
     * 转为异常抛出时，记录 info 级别日志，若接口中抛出未捕获，返回客户端 400 状态码
     */
    XXX(200001, "age out of range"),

    YYY(200002, "third service error"),

    ;

    private String code;

    private String message;

    /**
     * 转为异常抛出时，默认记录 warn 日志，返回 500 状态码
     */
    DemoErrorCodeEnum2(long code, String message) {
        String hex = Long.toHexString(code);
        this.code = "0x" + "0".repeat(Math.max(0, 8 - hex.length())) + hex;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

}
