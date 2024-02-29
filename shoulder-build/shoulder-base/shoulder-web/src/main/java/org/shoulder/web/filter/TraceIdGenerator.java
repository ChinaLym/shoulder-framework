package org.shoulder.web.filter;

import org.shoulder.core.util.AddressUtils;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
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
class TraceIdGenerator {

    private static String IP_16 = "ffffffff";
    private static String IP_int = "255255255255";
    private static String PID = "0000";
    private static char PID_FLAG = 'd';

    private static final String regex = "\\b((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\b";
    private static final Pattern pattern = Pattern.compile(regex);
    private static AtomicInteger count = new AtomicInteger(1000);

    static {
        try {
            String ipAddress = AddressUtils.getIp();
            if (ipAddress != null) {
                IP_16 = getIP_16(ipAddress);
                IP_int = getIP_int(ipAddress);
            }

            PID = getHexPid(getPid());
        } catch (Throwable e) {
        }
    }

    static String getHexPid(int pid) {
        // unsign short 0 to 65535
        if (pid < 0) {
            pid = 0;
        }
        if (pid > 65535) {
            String strPid = Integer.toString(pid);
            strPid = strPid.substring(strPid.length() - 4, strPid.length());
            pid = Integer.parseInt(strPid);
        }
        String str = Integer.toHexString(pid);
        while (str.length() < 4) {
            str = "0" + str;
        }
        return str;
    }

    /**
     * get current pid,max pid 32 bit systems 32768, for 64 bit 4194304
     * http://unix.stackexchange.com/questions/16883/what-is-the-maximum-value-of-the-pid-of-a-process
     * <p>
     * http://stackoverflow.com/questions/35842/how-can-a-java-program-get-its-own-process-id
     *
     * @return
     */
    static int getPid() {
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        String name = runtime.getName();
        int pid;
        try {
            pid = Integer.parseInt(name.substring(0, name.indexOf('@')));
        } catch (Exception e) {
            pid = 0;
        }
        return pid;
    }

    private static String getTraceId(String ip, long timestamp, int nextId) {
        StringBuilder appender = new StringBuilder(32);
        appender.append(ip).append(timestamp).append(nextId).append(PID_FLAG).append(PID);
        return appender.toString();
    }

    static String generate() {
        return getTraceId(IP_16, System.currentTimeMillis(), getNextId());
    }

    static String generate(String ip) {
        if (ip != null && !ip.isEmpty() && validate(ip)) {
            return getTraceId(getIP_16(ip), System.currentTimeMillis(), getNextId());
        } else {
            return generate();
        }
    }

    static String generateIpv4Id() {
        return IP_int;
    }

    static String generateIpv4Id(String ip) {
        if (ip != null && !ip.isEmpty() && validate(ip)) {
            return getIP_int(ip);
        } else {
            return IP_int;
        }
    }

    private static boolean validate(String ip) {
        try {
            return pattern.matcher(ip).matches();
        } catch (Throwable e) {
            return false;
        }
    }

    private static String getIP_16(String ip) {
        String[] ips = ip.split("\\.");
        StringBuilder sb = new StringBuilder();
        for (String column : ips) {
            String hex = Integer.toHexString(Integer.parseInt(column));
            if (hex.length() == 1) {
                sb.append('0').append(hex);
            } else {
                sb.append(hex);
            }

        }
        return sb.toString();
    }

    private static String getIP_int(String ip) {
        return ip.replace(".", "");
    }

    private static int getNextId() {
        for (; ; ) {
            int current = count.get();
            int next = (current > 9000) ? 1000 : current + 1;
            if (count.compareAndSet(current, next)) {
                return next;
            }
        }
    }
}
