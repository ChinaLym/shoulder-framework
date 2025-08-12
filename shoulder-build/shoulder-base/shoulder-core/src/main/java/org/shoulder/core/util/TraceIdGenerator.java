package org.shoulder.core.util;

import org.shoulder.core.context.AppContext;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;


/**
 * <li>所有流水生成必须使用该方法<br>
 * <table border="1" style="border-color: #bbccdd">
 * <tr>
 * <td>说明</td>
 * <td colspan="2">版本</td>
 * <td colspan="2">长度</td>
 * <td colspan="4">进程id</td>
 * <td colspan="13">时间戳</td>
 * <td colspan="4">序列号</td>
 * <td colspan="1">标识位</td>
 * <td colspan="32">ip4/ip6地址</td>
 * </tr>
 * <tr>
 * <td>位置</td>
 * <td>1</td>
 * <td>2</td>
 * <td>3</td>
 * <td>4</td>
 * <td>5</td>
 * <td>6</td>
 * <td>7</td>
 * <td>8</td>
 * <td colspan="13">9...21</td>
 * <td colspan="4">22....25</td>
 * <td>26</td>
 * <td colspan="32">27...58</td>
 * </tr>
 * <tr>
 * <td></td>
 * <td bgcolor="#000000" colspan="2">版本</td>
 * <td bgcolor="#bbcccc" colspan="2">长度</td>
 * <td bgcolor="#bbccdd" colspan="4">进程id</td>
 * <td bgcolor="#11eeee" colspan="13">时间戳</td>
 * <td bgcolor="#eecc88" colspan="4">序列号</td>
 * <td bgcolor="#dddd99" colspan="1">标识位</td>
 * <td bgcolor="#6655bb" colspan="32">ip4/ip6地址</td>
 * </tr>
 * </table>
 *
 * @author lym
 */
public class TraceIdGenerator {

    private static final String VERSION_DEFAULT = "00";
    private static final String LENGTH_IPV4 = "34";
    private static final String LENGTH_IPV6 = "58";
    private static final String FLAG_DEFAULT = "0";
    private static final AtomicInteger count = new AtomicInteger(1000);

    private static final Pattern TRACE_ID_IP4_PATTERN = Pattern.compile(
            "^0034[0-9a-f]{4}[0-9]{13}[0-9]{4}\\w[0-9a-f]{8}$"
    );
    private static final Pattern TRACE_ID_IPV6_PATTERN = Pattern.compile(
            "^0058[0-9a-f]{4}[0-9]{13}[0-9]{4}\\w([0-9a-f]{32}:){7}[0-9a-f]{32}$"
    );

    /**
     * 确保当前上下文存在 traceId，不存在的话生成并设置到上下文
     */
    public static String checkContextTracOrGenerateNew() {
        String traceId = AppContext.getTraceId();
        if(StringUtils.isEmpty(traceId)) {
            traceId = generateTraceWithLocalIpV4();
            AppContext.setTraceId(traceId);
        }
        return traceId;
    }

    public static String generateTraceWithLocalIpV4() {
        return generateTraceIdWithIpV4(System.currentTimeMillis(), genSequence(), FLAG_DEFAULT, AddressUtils.getIpV4Hex());
    }

    public static String generateTraceIdWithIpV4(String ipv4) {
        if (ipv4 != null && !ipv4.isEmpty() && AddressUtils.isIpv4(ipv4)) {
            return generateTraceIdWithIpV4(System.currentTimeMillis(), genSequence(), FLAG_DEFAULT, AddressUtils.toHexStr(ipv4));
        } else {
            return generateTraceWithLocalIpV4();
        }
    }

    private static String generateTraceIdWithIpV4(long timestamp, int sequence, String flag, String ipV4Hex) {
        return VERSION_DEFAULT + LENGTH_IPV4 + AddressUtils.getPidHex() + timestamp + sequence + flag + ipV4Hex;
    }

    private static String generateTraceIdWithIpV6(long timestamp, int sequence, String flag, String ipV6Hex) {
        return VERSION_DEFAULT + LENGTH_IPV6 + AddressUtils.getPidHex() + timestamp + sequence + flag + ipV6Hex;
    }


    private static int genSequence() {
        // 1000 最小的4位数，省去补0操作
        // 9000
        for (; ; ) {
            int current = count.get();
            int next = (current > 9000) ? 1000 : current + 1;
            if (count.compareAndSet(current, next)) {
                return next;
            }
        }
    }
}
