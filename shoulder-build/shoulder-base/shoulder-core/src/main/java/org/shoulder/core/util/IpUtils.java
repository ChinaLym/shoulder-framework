package org.shoulder.core.util;

import org.apache.commons.lang3.StringUtils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 获取本机 IP、MAC 等
 *
 * @author lym
 */
public class IpUtils {

    private static String LOCAL_IP = getIP();
    private static String LOCAL_MAC = getMAC();

    /**
     * (250~255 | 200~249 | 100~199 | 0~99 + .) * 3 + (250~255 | 200~249 | 100~199 | 0~99)
     * https://www.safaribooksonline.com/library/view/regular-expressions-cookbook/9781449327453/ch08s16.html
     */
    private static final Pattern IPV4_PATTERN = Pattern.compile("^(?:(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])\\.){3}(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])$");

    public static boolean validateIpv4(String ip) {
        if (StringUtils.isEmpty(ip)) {
            return false;
        }
        Matcher matcher = IPV4_PATTERN.matcher(ip);
        return matcher.matches();
    }

    /**
     * 获取本机mac
     */
    public static String getMACFromCache() {
        return LOCAL_MAC;
    }

    /**
     * 获取本机IP
     */
    public static String getIPFromCache() {
        return LOCAL_IP;
    }

    /**
     * 获取本机ip
     *
     * @return ip ：10.10.10.10
     */
    public static String getIP() {
        try {
            String address = InetAddress.getLocalHost().getHostAddress();
            LOCAL_IP = address;
            return address;
        } catch (Exception e) {
            return "127.0.0.1";
        }
    }

    /**
     * 获取本机主机名
     *
     * @return xxxx
     */
    public static String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "unknown";
        }
    }

    /**
     * 获取本机MAC地址，推荐从缓存中获取
     *
     * @return mac：2C-4D-54-E5-86-0E
     */
    private static String getMAC() {
        try {
            byte[] mac = NetworkInterface.getByInetAddress(InetAddress.getLocalHost()).getHardwareAddress();
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
            LOCAL_MAC = macAddress;
            return macAddress;
        } catch (Exception e) {
            return "00-00-00-00-00-00";
        }
    }


}
