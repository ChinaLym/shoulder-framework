package org.shoulder.core.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 获取本机 IP、MAC 等
 *
 * @author lym
 */
@Slf4j
public class AddressUtils {

    /**
     * (250~255 | 200~249 | 100~199 | 0~99 + .) * 3 + (250~255 | 200~249 | 100~199 | 0~99)
     * https://www.safaribooksonline.com/library/view/regular-expressions-cookbook/9781449327453/ch08s16.html
     */
    private static final Pattern IPV4_PATTERN = Pattern.compile("^(?:(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])\\.){3}(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])$");
    private static final Pattern PORT_PATTERN = Pattern.compile("^([0-9]{1,5})$");
    private static String LOCAL_IP_CACHE;
    private static String LOCAL_MAC_CACHE;

    public static boolean isIpv4(String ip) {
        if (StringUtils.isEmpty(ip)) {
            return false;
        }
        Matcher matcher = IPV4_PATTERN.matcher(ip);
        return matcher.matches();
    }

    public static boolean isPort(int port) {
        return port > 0 && port <= 65535;
    }

    public static boolean isPort(String port) {
        boolean validStr = port != null && !port.isEmpty() && port.length() <= 5 && PORT_PATTERN.matcher(port).matches();
        if (!validStr) {
            return false;
        }
        return isPort(Integer.parseInt(port));
    }

    /**
     * 获取本机mac
     */
    public static String getMac() {
        return LOCAL_MAC_CACHE;
    }

    /**
     * 获取本机IP
     */
    public static String getIp() {
        return LOCAL_IP_CACHE;
    }

    static {
        try {
            InetAddress inetAddress = getLocalNetAddress();
            LOCAL_IP_CACHE = inetAddress.getHostAddress();
            LOCAL_MAC_CACHE = getMac(inetAddress);
        } catch (Exception e) {
            log.warn("can't find local network address info.", e);
            // 任意异常，使用 127.0.0.1
            LOCAL_IP_CACHE = "127.0.0.1";
            LOCAL_MAC_CACHE = "00-00-00-00-00-00";
        }
    }

    /**
     * Retrieve the first validated local ip address(the Public and LAN ip addresses are validated).
     *
     * @return the local address
     * @throws SocketException the socket exception
     */
    public static InetAddress getLocalNetAddress() throws SocketException {
        // enumerates all network interfaces
        Enumeration<NetworkInterface> enu = NetworkInterface.getNetworkInterfaces();

        while (enu.hasMoreElements()) {
            NetworkInterface ni = enu.nextElement();
            if (ni.isLoopback()) {
                // 本地环回地址
                continue;
            }
            Enumeration<InetAddress> addressEnumeration = ni.getInetAddresses();
            while (addressEnumeration.hasMoreElements()) {
                InetAddress address = addressEnumeration.nextElement();
                // ignores all invalidated addresses
                if (address.isLinkLocalAddress() || address.isLoopbackAddress() || address.isAnyLocalAddress()) {
                    continue;
                }
                return address;
            }
        }

        throw new RuntimeException("No validated local address!");
    }

    /**
     * 获取本机MAC地址，推荐从缓存中获取
     *
     * @return mac：2C-4D-54-E5-86-0E
     */
    private static String getMac(InetAddress inetAddress) {
        try {
            byte[] mac = NetworkInterface.getByInetAddress(inetAddress).getHardwareAddress();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                if (i != 0) {
                    sb.append("-");
                }
                //mac[i] & 0xFF 是为了把byte转化为正整数
                String s = Integer.toHexString(mac[i] & 0xFF);
                sb.append(s.length() == 1 ? 0 + s : s);
            }
            String macAddress = sb.toString().toUpperCase();
            LOCAL_MAC_CACHE = macAddress;
            return macAddress;
        } catch (Exception e) {
            return "00-00-00-00-00-00";
        }
    }


    /**
     * 将 ipv4 转为 hexStr（压缩）
     * 以 . 分隔，每段转为两个 0-f 的 hex 字母
     *
     * @param ipv4 ipv4
     * @return hexString （8个字符的字符串）
     */
    public static String toHexStr(String ipv4) {
        int[] ipSegments = parseToIntArray(ipv4);
        StringBuilder hexIp = new StringBuilder(8);
        for (int ipSegment : ipSegments) {
            String hexSegment = Integer.toHexString(ipSegment);
            if (hexSegment.length() == 1) {
                hexIp.append("0");
            }
            hexIp.append(hexSegment);
        }
        return hexIp.toString();
    }


    /**
     * 将 ipv4 转为 long（压缩）
     *
     * @param ipv4 ipv4
     * @return long
     */
    public static long toLong(String ipv4) {
        int[] ip = parseToIntArray(ipv4);
        return ((long) ip[0] << 24) + ((long) ip[1] << 16) + ((long) ip[2] << 8) + ip[3];
    }

    /**
     * 将 ipv4 转为 int（压缩）
     *
     * @param ipv4 ipv4
     * @return int
     */
    public static int toInt(String ipv4) {
        int[] ip = parseToIntArray(ipv4);
        return (ip[0] << 24) + (ip[1] << 16) + (ip[2] << 8) + ip[3];
    }

    /**
     * 将 long 转为 ipv4 字符串（解压）
     *
     * @param ipv4 ipv4
     * @return ipv4Str
     */
    public static String parseIPv4(long ipv4) {
        final long mask = 255;

        return ((ipv4 >>> 24) & mask) +
                "." +
                ((ipv4 >>> 16) & mask) +
                "." +
                ((ipv4 >>> 8) & mask) +
                "." +
                (ipv4 & mask);
    }

    /**
     * 将 int 转为 ipv4 字符串（解压）
     *
     * @param ipv4 ipv4
     * @return ipv4Str
     */
    public static String parseIPv4(int ipv4) {
        final int mask = 255;

        return ((ipv4 >>> 24) & mask) +
                "." +
                ((ipv4 >>> 16) & mask) +
                "." +
                ((ipv4 >>> 8) & mask) +
                "." +
                (ipv4 & mask);
    }


    /**
     * 将 hexStrIp 转为 ipv4 字符串（解压）
     *
     * @param hexIp ipv4
     * @return ipv4Str
     */
    public static String parseIPv4FromHex(String hexIp) {
        if (hexIp == null || hexIp.length() != 8) {
            throw new IllegalArgumentException("invalid hexIp: " + hexIp);
        }
        StringBuilder ipv4Str = new StringBuilder(15);
        for (int i = 0; i < 4; i++) {
            int ipSegment = Integer.parseUnsignedInt(hexIp.substring(i << 1, (i << 1) + 2), 16);
            ipv4Str.append(ipSegment);
            if (i < 3) {
                ipv4Str.append(".");
            }
        }
        return ipv4Str.toString();
    }


    /**
     * 将 ipv4 字符串形式转为 int[]
     *
     * @param ipv4 ipv4
     * @return 长度必定为 4
     */
    public static int[] parseToIntArray(String ipv4) {
        int[] ip = new int[4];
        int position1 = ipv4.indexOf(".");
        int position2 = ipv4.indexOf(".", position1 + 1);
        int position3 = ipv4.indexOf(".", position2 + 1);
        ip[0] = Integer.parseInt(ipv4.substring(0, position1));
        ip[1] = Integer.parseInt(ipv4.substring(position1 + 1, position2));
        ip[2] = Integer.parseInt(ipv4.substring(position2 + 1, position3));
        ip[3] = Integer.parseInt(ipv4.substring(position3 + 1));
        return ip;
    }

    /**
     * 在一个区间内
     *
     * @param ip      要检测的ip
     * @param ipStart ip起始
     * @param ipEnd   ip结束
     * @return 是否在该区间内，闭区间
     */
    public static boolean isBetweenInterval(long ip, String ipStart, String ipEnd) {
        long start = toLong(ipStart);
        long end = toLong(ipEnd);
        return isBetweenInterval(ip, start, end);
    }

    public static boolean isBetweenInterval(int ip, String ipStart, String ipEnd) {
        final long mask = (1L << 32) - 1;
        return isBetweenInterval(ip & mask, ipStart, ipEnd);
    }

    public static boolean isBetweenInterval(long ip, long start, long end) {
        return ip >= start && ip <= end;
    }

    public static boolean isBetweenInterval(String ip, String start, String end) {
        return isBetweenInterval(toLong(ip), start, end);
    }

    public static boolean isBetweenHexInterval(String hexIp, String hexStart, String hexEnd) {
        return hexIp.compareTo(hexStart) >= 0 && hexIp.compareTo(hexEnd) <= 0;
    }

    public static boolean isBetweenIntervalHex(String hexIp, String start, String end) {
        return isBetweenInterval(parseIPv4FromHex(hexIp), start, end);
    }


}
