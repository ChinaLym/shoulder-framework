package org.shoulder.core.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.shoulder.core.exception.BaseRuntimeException;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.log.ShoulderLoggers;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "^(?:(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])\\.){3}(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])$");

    private static final Pattern IPV6_PATTERN = Pattern.compile(
            "^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$|" +
                    "^:((?::[0-9a-fA-F]{1,4}){1,7}|(?!.+:)[^:]*)$|" + // 修正了连续冒号的处理
                    "^[^:]*:(?:(?:(?::[0-9a-fA-F]{1,4}){1,6})|(?:::[^:]*)+):(?:[0-9a-fA-F]{1,4}|(?![0-9a-fA-F:]))$|" + // 修正了内部压缩零段的处理
                    "^[^:]*::(?:[0-9a-fA-F]{1,4}|(?![0-9a-fA-F:]))(?:(?:(?::[0-9a-fA-F]{1,4}){1,5})|(?:::[^:]*)+)?$|" + // 修正了尾部压缩零段的处理
                    "^[^:]*:(?:(?:(?::[0-9a-fA-F]{1,4}){0,1})|(?:::[^:]*)+):(?:[0-9a-fA-F]{1,4}|(?![0-9a-fA-F:]))(?:(?:(?::[0-9a-fA-F]{1,4}){0,2})|(?:::[^:]*)+)?$|" + // 修正了中间压缩零段的处理
                    "^[^:]*:(?:(?:(?::[0-9a-fA-F]{1,4}){0,3})|(?:::[^:]*)+):(?:[0-9a-fA-F]{1,4}|(?![0-9a-fA-F:]))(?:(?:(?::[0-9a-fA-F]{1,4}){0,4})|(?:::[^:]*)+)?$|" +
                    "^[^:]*:(?:(?:(?::[0-9a-fA-F]{1,4}){0,5})|(?:::[^:]*)+):(?:[0-9a-fA-F]{1,4}|(?![0-9a-fA-F:]))(?:(?:(?::[0-9a-fA-F]{1,4}){0,3})|(?:::[^:]*)+)?$|" +
                    "^[^:]*:(?:(?:(?::[0-9a-fA-F]{1,4}){0,6})|(?:::[^:]*)+):(?:[0-9a-fA-F]{1,4}|(?![0-9a-fA-F:]))(?:(?::[0-9a-fA-F]{1,4})|(?:::[^:]*)+)?$"
    );

    private static final Pattern IPV6_NO_COMPRESSION_PATTERN = Pattern.compile(
            "^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$"
    );

    private static final Pattern PORT_PATTERN = Pattern.compile("^([0-9]{1,5})$");
    private static final Pattern IPV6_ZERO_LEADING_PATTERN = Pattern.compile("(?i)^0+$");
    public static final String LOCAL_HOST_IP4 = "127.0.0.1";
    public static final String LOCAL_HOST_IP6 = "0:0:0:0:0:0:0:1";
    private static String LOCAL_IP_CACHE = LOCAL_HOST_IP4;
    private static String LOCAL_IPV4_HEX = "ffffffff";
    private static String LOCAL_MAC_CACHE = "00-00-00-00-00-00";
    private static String LOCAL_HOSTNAME_CACHE = "unknown";
    private static int LOCAL_PID = 0;
    private static String LOCAL_PID_HEX = "0000";


    static {
        try {
            InetAddress inetAddress = getLocalNetAddress();
            LOCAL_IP_CACHE = inetAddress.getHostAddress();
            LOCAL_MAC_CACHE = getMac(inetAddress);
            LOCAL_HOSTNAME_CACHE = getHostName(inetAddress);
            LOCAL_IPV4_HEX = AddressUtils.toHexStr(LOCAL_IP_CACHE);
            LOCAL_PID = getCurrentPid();
            LOCAL_PID_HEX = convertPidToHex(LOCAL_PID);
        } catch (Exception e) {
            log.warn("can't find local network address info.", e);
        }
    }

    private static String getHostName(InetAddress inetAddress) {
        try {
            String hostname = inetAddress.getHostName();
            if (StringUtils.isBlank(hostname)) {
                hostname = System.getProperty("os.name").toLowerCase().contains("windows") ?
                        System.getenv("COMPUTERNAME") : System.getenv("HOSTNAME");
            }
            return hostname;
        } catch (Exception e) {
            return "unknown";
        }
    }

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
     * 获取本机mac
     */
    public static String getHostname() {
        return LOCAL_HOSTNAME_CACHE;
    }

    /**
     * 获取本机IP
     */
    public static String getIp() {
        return LOCAL_IP_CACHE;
    }

    /**
     * Retrieve the first validated local ip address(the Public and LAN ip addresses are validated).
     *
     * @return the local address
     * @throws SocketException the socket exception
     */
    public static InetAddress getLocalNetAddress() throws SocketException {
        // enumerates all network interfaces
        List<NetworkInterface> enu = Collections.list(NetworkInterface.getNetworkInterfaces());
        List<NetworkInterface> filtered = new ArrayList<>(enu.size());

        List<String> preferredPrefixes = List.of("eth", "eno", "ens", "enp", "enx", "wl");
        Set<String> excludedPatterns = Set.of(
                "vboxnet", "vmnet", "vmxnet", "vmnic", "Hyper-V",
                "docker", "br-", "veth", "cni", "flannel.", "cali",
                "lo", "ppp", "tun", "tap", "bridge", "vlan", "bond"
        );

        for (NetworkInterface ni : enu) {
            boolean include = !ni.isVirtual() || ni.isLoopback() || !ni.isPointToPoint() && ni.isUp();
            boolean exclude = excludedPatterns.stream().anyMatch(pattern -> ni.getName().contains(pattern) || ni.getDisplayName().contains(pattern));
            if (!include || exclude) {
                continue;
            }
            List<InetAddress> addressEnumeration = ni.inetAddresses().toList();
            for (InetAddress address : addressEnumeration) {
                // 跳过多播、回环、链路本地、任意地址 todo address.isMulticastAddress()、isSiteLocalAddress
                if (address.isLinkLocalAddress() || address.isLoopbackAddress() || address.isAnyLocalAddress()
                        // not ipv6
                        || address.getHostAddress().contains(":")) {
                    continue;
                }
                filtered.add(ni);
            }
        }

        List<NetworkInterface> sortedNetworkInterfaceList = filtered.stream().sorted((ni1, ni2) -> {
            String name1 = ni1.getName().toLowerCase();
            String name2 = ni2.getName().toLowerCase();

            int score1 = getPriorityScoreFromNetworkInterfaceName(name1, preferredPrefixes);
            int score2 = getPriorityScoreFromNetworkInterfaceName(name2, preferredPrefixes);
            if (score1 != score2) {
                return score1 - score2;
            }

            return extractNumberFromNetworkInterfaceName(name1) - extractNumberFromNetworkInterfaceName(name2);
        }).toList();
        AssertUtils.notEmpty(sortedNetworkInterfaceList, CommonErrorCodeEnum.ILLEGAL_STATUS, "No validated local address!");

        InetAddress finalResult = selectOptimalIPAddress(sortedNetworkInterfaceList.get(0).inetAddresses().toList(), true);

        ShoulderLoggers.SHOULDER_CONFIG.warn("found address {}, from {}", finalResult, sortedNetworkInterfaceList.stream()
                .map(ni -> "{ \"" + ni.getName() + "\" : [" + ni.inetAddresses().toList().stream().map(InetAddress::getHostAddress).collect(Collectors.joining(", ")) + "]}\n")
                .collect(Collectors.joining(", ")));

        return finalResult;
    }

    /**
     * prefixes 中越靠前的优先级越靠前
     */
    private static int getPriorityScoreFromNetworkInterfaceName(String name, List<String> prefixes) {
        for (int i = 0; i < prefixes.size(); i++) {
            if (name.startsWith(prefixes.get(i))) {
                return i;
            }
        }
        return 0;
    }

    private static int extractNumberFromNetworkInterfaceName(String name) {
        try {
            String[] parts = name.split("\\D+");
            if (parts.length > 1) {
                return Integer.parseInt(parts[1]);
            }
        } catch (Exception e) {
            // ignore
        }
        return Integer.MAX_VALUE;
    }

    /**
     * 选择合适的IP地址
     *
     * @param preferIPv4 是否优先选择IPv4地址
     * @return 最优IP地址，如果未找到则返回null
     */
    public static InetAddress selectOptimalIPAddress(List<InetAddress> intnetAddressList, boolean preferIPv4) throws SocketException {
        // 按优先级排序
        return intnetAddressList.stream().min((a1, a2) -> {
            // 优先级：IPv4 > IPv6
            boolean isIPv4_1 = a1 instanceof Inet4Address;
            boolean isIPv4_2 = a2 instanceof Inet4Address;

            if (preferIPv4) {
                if (isIPv4_1 && !isIPv4_2) return -1; // IPv4优先
                if (!isIPv4_1 && isIPv4_2) return 1;  // IPv4优先
            }

            // 如果类型相同，按是否为私有地址排序（公网优先）
            boolean isSiteLocal_1 = a1.isSiteLocalAddress();
            boolean isSiteLocal_2 = a2.isSiteLocalAddress();

            if (!isSiteLocal_1 && isSiteLocal_2) return -1; // 公网优先
            if (isSiteLocal_1 && !isSiteLocal_2) return 1;  // 公网优先

            return 0;
        }).orElseThrow(() -> new BaseRuntimeException(CommonErrorCodeEnum.ILLEGAL_STATUS, "No validated InetAddress!"));
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
     * get current pid,max pid 32 bit systems 32768, for 64 bit 4194304
     * <p>
     * <a href="http://unix.stackexchange.com/questions/16883/what-is-the-maximum-value-of-the-pid-of-a-process">stackexchange</a>
     * <p>
     * <a href="http://stackoverflow.com/questions/35842/how-can-a-java-program-get-its-own-process-id">stackoverflow</a>
     *
     * @return pid
     */
    private static int getCurrentPid() {
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

    private static String convertPidToHex(int pid) {
        // unsign short 0 to 65535
        if (pid < 0) {
            pid = 0;
        }
        if (pid > 65535) {
            String strPid = Integer.toString(pid);
            strPid = strPid.substring(strPid.length() - 4);
            pid = Integer.parseInt(strPid);
        }
        StringBuilder str = new StringBuilder(Integer.toHexString(pid));
        while (str.length() < 4) {
            str.insert(0, "0");
        }
        return str.toString();
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

    public static String getIpV4Hex() {
        return LOCAL_IPV4_HEX;
    }

    public static int getPid() {
        return LOCAL_PID;
    }

    public static String getPidHex() {
        return LOCAL_PID_HEX;
    }

    public static String compressIPv6(String ipv6) {
        try {
            // 检查并直接返回合法的IPv6地址（可能已是压缩格式）
            return InetAddress.getByName(ipv6).getHostAddress();
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Invalid IPv6 address: " + ipv6, e);
        }

    }

    public static String decompressIPv6Manually(String compressedIpv6) {
        StringBuilder sb = new StringBuilder(39);
        int colonCount = 0;
        int lastColonIndex = -1;

        for (int i = 0; i < compressedIpv6.length(); i++) {
            char ch = compressedIpv6.charAt(i);
            if (ch == ':') {
                colonCount++;
                if (colonCount > 7 || (i == 0 && i + 1 < compressedIpv6.length() && compressedIpv6.charAt(i + 1) == ':')) {
                    throw new IllegalArgumentException("Invalid IPv6 address: " + compressedIpv6);
                }
                if (colonCount != 7 &&
                        !IPV6_ZERO_LEADING_PATTERN.matcher(
                                compressedIpv6.substring(lastColonIndex + 1, i)
                        ).matches()) {
                    sb.append(':');
                }
                lastColonIndex = i;
            } else {
                sb.append(ch);
            }
        }

        // 处理最后一部分，确保地址有8组
        while (sb.toString().split(":").length < 8) {
            sb.insert(lastColonIndex + 1, ":0000");
        }

        return sb.toString();
    }


}
