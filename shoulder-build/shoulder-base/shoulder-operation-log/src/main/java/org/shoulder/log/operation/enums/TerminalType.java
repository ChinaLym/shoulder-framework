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
     * PC 浏览器
     */
    BROWSER(1),

    /**
     * PC 客户端
     */
    PC(2),

    /**
     * 移动 APP
     */
    APP(3),

    /**
     * 移动端浏览器 web app
     */
    WAP(4),

    /**
     * 小程序 mini APP / H5
     */
    MINA(5),

    /**
     * 未知
     */
    UNKNOWN(-1),

    ;

    private final int code;

    TerminalType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
