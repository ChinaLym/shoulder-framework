package org.shoulder.log.operation.constants;

/**
 * 操作者使用的终端类型
 * @author lym
 */
public enum TerminalType {

    /** 系统内部触发 */
    SYSTEM(0),

    /** 浏览器端 */
    BROWSER(1),

    /** 移动端 APP */
    APP(2),

    /** PC 客户端 */
    CLIENT(2),
    ;

    private int type;

    TerminalType(int type){
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
