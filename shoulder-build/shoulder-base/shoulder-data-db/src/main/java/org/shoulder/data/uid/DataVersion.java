package org.shoulder.data.uid;

/**
 * 数据版本位定义。<br>
 * <li>全局默认: 正常="1"；FO="9"
 * <li>预留位："0"
 * <li>其他自行使用 "2"、"3"、"4"、"5"、"6"、"7"、"8"
 *
 * @author lym
 */
public interface DataVersion {

    /**
     * 默认
     */
    String DEFAULT = "1";

    /**
     * FAIL_OVER 专用
     */
    String FAIL_OVER = "9";

    /**
     * 预留版本：0
     */
    String RESERVE = "0";

    String DV_2 = "2";
    String DV_3 = "3";
    String DV_4 = "4";
    String DV_5 = "5";
    String DV_6 = "6";
    String DV_7 = "7";
    String DV_8 = "8";
}
