package org.shoulder.log.operation.enums;

/**
 * 操作者使用的终端类型
 *
 * @author lym
 */
public enum TerminalType {

    /**
     * 系统内部触发
     */
    SYSTEM(0),

    /**
     * 浏览器
     */
    BROWSER(1),

    /**
     * PC 客户端
     */
    CLIENT(2),

    /**
     * 移动 APP
     */
    APP(3),

    /**
     * 小程序 mini APP
     */
    MINA(4),

    ;

    private int code;

    TerminalType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
