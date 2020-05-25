package org.shoulder.log.operation.constants;

/**
 * 操作日志所有常量
 * @author lym
 */
public class OpLogConstants {

    /**
     * 操作者的终端类型
     */
    public static class TerminalType {

        /** 表示为系统内部操作 */
        public static final String SYSTEM = "0";

        /** web 浏览器端 */
        public static final String WEB = "1";

        /** C/S 客户端 */
        public static final String CLIENT = "2";

    }


    /**
     * 日志支持国际化时前缀
     */
    public static class I18nPrefix {

        /** common */
        public static final String COMMON = "i18n.log.";

        /** action */
        public static final String ACTION = COMMON + "action.";

        /** i18nKey */
        public static final String ACTION_DETAIL = COMMON + "detail.";

        /** objectType */
        public static final String OBJECT_TYPE = COMMON + "objectType.";
    }

}
