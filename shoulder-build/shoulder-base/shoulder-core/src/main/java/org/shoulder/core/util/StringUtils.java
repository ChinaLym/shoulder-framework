package org.shoulder.core.util;

import cn.hutool.core.util.StrUtil;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.shoulder.core.context.ApplicationInfo;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串工具类
 *
 * @author lym
 */
public class StringUtils extends org.apache.commons.lang3.StringUtils {

    private static final char SEPARATOR = '_';
    private static final Charset CHARSET = ApplicationInfo.charset();

    /**
     * 转换为字节数组
     */
    public static byte[] getBytes(String str) {
        if (str == null) {
            return null;
        }
        return str.getBytes(CHARSET);
    }

    /**
     * 转换为字节数组
     */
    public static String toString(byte[] bytes) {
        return new String(bytes, CHARSET);
    }

    /**
     * 转换为Boolean类型
     * 'true', 'on', 'y', 't', 'yes' or '1' (case insensitive) will return true. Otherwise, false is returned.
     */
    public static Boolean toBoolean(final Object val) {
        if (val == null) {
            return false;
        }
        return BooleanUtils.toBoolean(val.toString()) || "1".equals(val.toString());
    }


    /**
     * 如果对象为空，则使用defaultVal值
     * see: ObjectUtils.toString(obj, defaultVal)
     */
    public static String toString(final Object obj, final String defaultVal) {
        return obj == null ? defaultVal : obj.toString();
    }

    /**
     * 是否包含字符串
     *
     * @param aimString 验证字符串
     * @param toMatch   字符串组
     * @return 包含返回true
     */
    public static boolean contains(String aimString, String... toMatch) {
        if (aimString != null) {
            for (String s : toMatch) {
                if (aimString.equals(trim(s))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 取中间部分
     */
    public static String subBetween(String str, String begin, String end) {
        return StrUtil.subBetween(str, begin, end);
    }

    /**
     * 判断是否是Base64编码后的值
     *
     * @param str 目标字符串
     * @return boolean
     */
    public static boolean isBase64(String str) {
        return RegexpUtils.BASE_64.matcher(str).matches();
    }

    public static boolean isNotBase64(String str) {
        return !isBase64(str);
    }

    /**
     * 将字符串转换为语言和时区
     *
     * @param locale 语言地区字符串
     * @return 对应的语言和地区
     */
    public static Locale parseLocale(String locale) {
        return parseLocale(locale, Locale.getDefault());
    }
    /**
     * 将字符串转换为语言和时区
     *
     * @param locale 语言地区字符串
     * @param defaultLocale 如果转换失败使用该值
     * @return 对应的语言和地区
     */
    public static Locale parseLocale(String locale, Locale defaultLocale) {
        if (StringUtils.isEmpty(locale)) {
            return defaultLocale;
        }
        String[] localeParts = locale.split("_");
        String language = localeParts[0];
        String country = localeParts.length > 1 ? localeParts[1] : "";
        return new Locale(language, country);

    }

    /**
     * 转换为Double类型
     */
    public static Double toDouble(Object val) {
        if (val == null) {
            return 0D;
        }
        try {
            return Double.valueOf(trim(val.toString()));
        } catch (Exception e) {
            return 0D;
        }
    }

    /**
     * 32位的 uuid
     */
    public static String uuid32() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 转换为Float类型
     */
    public static Float toFloat(Object val) {
        return toDouble(val).floatValue();
    }

    /**
     * 转换为Long类型
     */
    public static Long toLong(Object val) {
        return toDouble(val).longValue();
    }

    /**
     * 转换为Integer类型
     */
    public static Integer toInteger(Object val) {
        return toLong(val).intValue();
    }

    /**
     * 缩略字符串（不区分中英文字符）
     *
     * @param str    目标字符串
     * @param length 截取长度
     */
    public static String ellipsis(String str, int length) {
        if (str == null) {
            return "";
        }
        try {
            StringBuilder sb = new StringBuilder();
            int currentLength = 0;
            for (char c : replaceHtml(StringEscapeUtils.unescapeHtml4(str)).toCharArray()) {
                currentLength += String.valueOf(c).getBytes("GBK").length;
                if (currentLength <= length - 3) {
                    sb.append(c);
                } else {
                    sb.append("...");
                    break;
                }
            }
            return sb.toString();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 替换掉HTML标签方法
     */
    public static String replaceHtml(String html) {
        if (isBlank(html)) {
            return "";
        }
        String regEx = "<.+?>";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(html);
        return m.replaceAll("");
    }

    /**
     * Html 转码.
     */
    public static String escapeHtml(String html) {
        return StringEscapeUtils.escapeHtml4(html);
    }

    /**
     * Html 解码.
     */
    public static String unescapeHtml(String htmlEscaped) {
        return StringEscapeUtils.unescapeHtml4(htmlEscaped);
    }

    /**
     * Xml 转码.
     */
    public static String escapeXml(String xml) {
        return StringEscapeUtils.escapeXml11(xml);
    }

    /**
     * Xml 解码.
     */
    public static String unescapeXml(String xmlEscaped) {
        return StringEscapeUtils.unescapeXml(xmlEscaped);
    }

    /**
     * url追加参数
     *
     * @param url   url
     * @param name  参数名
     * @param value 参数值
     */
    public static String appendURIParam(String url, String name, String value) {
        url += (url.indexOf('?') == -1 ? '?' : '&');
        url += EncodeUtils.encodeUrl(name) + '=' + EncodeUtils.encodeUrl(value);
        return url;
    }

    /**
     * 组装新的URL
     *
     * @param url url
     * @param map map
     * @return newUrl
     */
    public static String appendURIParam(String url, Map<String, String> map) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            url = appendURIParam(url, entry.getKey(), entry.getValue());
        }
        return url;
    }


    /**
     * 驼峰转下划线
     * createTime 转为 create_time
     */
    public static String camelToUnderline(String param) {
        if (param == null || "".equals(param.trim())) {
            return "";
        }
        int len = param.length();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = param.charAt(i);
            if (Character.isUpperCase(c)) {
                sb.append(SEPARATOR);
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 下划线转驼峰
     * create_time 变为 createTime
     */
    public static String underlineToCamel(String param) {
        if (param == null || "".equals(param.trim())) {
            return "";
        }
        StringBuilder sb = new StringBuilder(param);
        Matcher mc = Pattern.compile(String.valueOf(SEPARATOR)).matcher(param);
        int i = 0;
        while (mc.find()) {
            int position = mc.end() - (i++);
            sb.replace(position - 1, position + 1, sb.substring(position, position + 1).toUpperCase());
        }
        return sb.toString();
    }

    /**
     * 格式化存储单位，如 2048 转为 2K
     *
     * @param size byte 字节
     * @return 人性化的大小表示方式
     */
    public static String formatBytes(long size) {
        // 如果字节数少于1024，则直接以B为单位，否则先除于1024，后3位因太少无意义
        int bytes = 1024;
        if (size < bytes) {
            return String.valueOf(size) + "Byte";
        } else {
            size = size / bytes;
        }
        // 如果原字节数除于1024之后，少于1024，则可以直接以KB作为单位 //因为还没有到达要使用另一个单位的时候 //接下去以此类推
        if (size < bytes) {
            return String.valueOf(size) + "K";
        } else {
            size = size / bytes;
        }
        if (size < bytes) {
            // 因为如果以MB为单位的话，要保留最后1位小数， //因此，把此数乘以100之后再取余
            size = size * 100;
            return String.valueOf((size / 100)) + "." + String.valueOf((size % 100)) + "M";
        } else { // 否则如果要以GB为单位的，先除于1024再作同样的处理
            size = size * 100 / bytes;
            return String.valueOf((size / 100)) + "." + String.valueOf((size % 100)) + "G";
        }
    }

    // ---------- 常用正则处理 ------------

    /**
     * 匿名手机号
     *
     * @param mobile 11100001234
     * @return 111****1234
     */
    public static String anonymizeMobile(String mobile) {

        if (isEmpty(mobile)) {
            return null;
        }
        return mobile.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
    }

    /**
     * 匿名银行卡号
     *
     * @param bankCard 12345 12345 12 1234
     * @return 12345****1234
     */
    public static String anonymizeBankCard(String bankCard) {
        if (isEmpty(bankCard)) {
            return null;
        }
        return bankCard.replaceAll("(\\d{5})\\d{5}\\d{2}(\\d{4})", "$1****$2");
    }

    /**
     * 匿名身份证
     *
     * @param idCard 1234 1234567890 1234
     * @return 1234****1234
     */
    public static String anonymizeIdCard(String idCard) {

        if (isEmpty(idCard)) {
            return null;
        }
        return idCard.replaceAll("(\\d{4})\\d{10}(\\w{4})", "$1*****$2");
    }

    // ---------- 常用正则匹配 ------------

    /**
     * 检测是否未手机号
     * 中国电信号段
     * 133、149、153、173、177、180、181、189、199
     * 中国联通号段
     * 130、131、132、145、155、156、166、175、176、185、186
     * 中国移动号段
     * 134(0-8)、135、136、137、138、139、147、150、151、152、157、158、159、178、182、183、184、187、188、198
     * 其他号段
     * 14号段以前为上网卡专属号段，如中国联通的是145，中国移动的是147等等。
     * 虚拟运营商
     * 电信：1700、1701、1702
     * 移动：1703、1705、1706
     * 联通：1704、1707、1708、1709、171
     *
     * @param mobile 123456
     * @return ture
     */
    public static boolean matchMobile(String mobile) {
        if (mobile == null) {
            return false;
        }
        String regex = "^((13[0-9])|(14[5,7])|(15[0-3,5-9])|(17[0,3,5-8])|(18[0-9])|166|198|199|(147))\\d{8}$";
        return Pattern.matches(regex, mobile);
    }

    /**
     * 检测Email
     *
     * @param email email
     * @return bool
     */
    public static boolean matchEmail(String email) {
        if (email == null) {
            return false;
        }
        String regex = "\\w+@\\w+\\.[a-z]+(\\.[a-z]+)?";
        return Pattern.matches(regex, email);
    }


    /**
     * 检测域名
     *
     * @param domain domain
     * @return bool
     */
    public static boolean matchDomain(String domain) {
        if (domain == null) {
            return false;
        }
        String regex = "^(?=^.{3,255}$)[a-zA-Z0-9][-a-zA-Z0-9]{0,62}(\\.[a-zA-Z0-9][-a-zA-Z0-9]{0,62})+$";
        return Pattern.matches(regex, domain);
    }

    /**
     * 检测IP
     *
     * @param ip ip
     * @return bool
     */
    public static boolean matchIp(String ip) {
        if (ip == null) {
            return false;
        }
        String regex = "^(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])$";
        return Pattern.matches(regex, ip);
    }

    /**
     * 检测HttpUrl
     *
     * @param url http
     * @return bool
     */
    public static boolean matchHttpUrl(String url) {
        if (url == null) {
            return false;
        }
        String regex = "^(?=^.{3,255}$)(http(s)?:\\/\\/)?(www\\.)?[a-zA-Z0-9][-a-zA-Z0-9]{0,62}(\\.[a-zA-Z0-9][-a-zA-Z0-9]{0,62})+(:\\d+)*(\\/\\w+\\.\\w+)*([\\?&]\\w+=\\w*)*$";
        return Pattern.matches(regex, url);
    }

    /**
     * 校验银行卡卡号
     * 校验过程：
     * 1、从卡号最后一位数字开始，逆向将奇数位(1、3、5等等)相加。
     * 2、从卡号最后一位数字开始，逆向将偶数位数字，先乘以2（如果乘积为两位数，将个位十位数字相加，即将其减去9），再求和。
     * 3、将奇数位总和加上偶数位总和，结果应该可以被10整除。
     */
    public static boolean matchBankCard(String bankCard) {
        if (bankCard == null) {
            return false;
        }
        if (bankCard.length() < 15 || bankCard.length() > 19) {
            return false;
        }
        char bit = getBankCardCheckCode(bankCard.substring(0, bankCard.length() - 1));
        if (bit == 'N') {
            return false;
        }
        return bankCard.charAt(bankCard.length() - 1) == bit;
    }

    /**
     * 用 Luhm 校验算法，获取不含校验位的银行卡卡号的校验位
     *
     * @param nonCheckCodeBankCard 不含校验位的银行卡卡号
     * @return 校验位
     */
    public static char getBankCardCheckCode(String nonCheckCodeBankCard) {
        if (nonCheckCodeBankCard == null || nonCheckCodeBankCard.trim().length() == 0
            || !nonCheckCodeBankCard.matches("\\d+")) {
            //如果传的不是数据返回N
            return 'N';
        }
        char[] chs = nonCheckCodeBankCard.trim().toCharArray();
        int luhmSum = 0;
        for (int i = chs.length - 1, j = 0; i >= 0; i--, j++) {
            int k = chs[i] - '0';
            if (j % 2 == 0) {
                k *= 2;
                k = k / 10 + k % 10;
            }
            luhmSum += k;
        }
        return (luhmSum % 10 == 0) ? '0' : (char) ((10 - luhmSum % 10) + '0');
    }


    /**
     * xss脚本正则
     */
    private static final List<Pattern> XSS_SCRIPT_PATTERNS = Arrays.asList(
        // Avoid anything between script tags
        Pattern.compile("<(no)?script>(.*?)</(no)?script>", Pattern.CASE_INSENSITIVE),
        // Avoid anything in a src='...' type of expression
        Pattern.compile("src[\\s]*=[\\s]*\\\'(.*?)\\\'", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        Pattern.compile("src[\\s]*=[\\s]*\\\"(.*?)\\\"", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        // Remove any lonesome </script> tag
        Pattern.compile("</script>", Pattern.CASE_INSENSITIVE),
        // Remove any lonesome <script ...> tag
        Pattern.compile("<script(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        // Avoid eval(...) expressions
        Pattern.compile("eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        // Avoid expression(...) expressions
        Pattern.compile("expression\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        // Avoid javascript:vbscript:view-source:xxx expressions
        Pattern.compile("(javascript:|vbscript:|view-source:)*", Pattern.CASE_INSENSITIVE),
        // Avoid onload= expressions
        Pattern.compile("onload(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        Pattern.compile("<(\"[^\"]*\"|\'[^\']*\'|[^\'\">])*>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        Pattern.compile("(window\\.location|window\\.|\\.location|document\\.cookie|document\\.|alert\\(.*?\\)|window\\.open\\()*", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        Pattern.compile("<+\\s*\\w*\\s*(oncontrolselect|oncopy|oncut|ondataavailable|ondatasetchanged|ondatasetcomplete|ondblclick|ondeactivate|ondrag|ondragend|ondragenter|ondragleave|ondragover|ondragstart|ondrop|onerror=|onerroupdate|onfilterchange|onfinish|onfocus|onfocusin|onfocusout|onhelp|onkeydown|onkeypress|onkeyup|onlayoutcomplete|onload|onlosecapture|onmousedown|onmouseenter|onmouseleave|onmousemove|onmousout|onmouseover|onmouseup|onmousewheel|onmove|onmoveend|onmovestart|onabort|onactivate|onafterprint|onafterupdate|onbefore|onbeforeactivate|onbeforecopy|onbeforecut|onbeforedeactivate|onbeforeeditocus|onbeforepaste|onbeforeprint|onbeforeunload|onbeforeupdate|onblur|onbounce|oncellchange|onchange|onclick|oncontextmenu|onpaste|onpropertychange|onreadystatuschange|onreset|onresize|onresizend|onresizestart|onrowenter|onrowexit|onrowsdelete|onrowsinserted|onscroll|onselect|onselectionchange|onselectstart|onstart|onstop|onsubmit|onunload)+\\s*=+", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL)
    );

    /**
     * 处理非法字符
     */

    private static List<Object[]> getXssPatternList() {
        List<Object[]> ret = new ArrayList<>();
        ret.add(new Object[]{"<(no)?script[^>]*>.*?</(no)?script>", Pattern.CASE_INSENSITIVE});
        ret.add(new Object[]{"eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL});
        ret.add(new Object[]{"expression\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL});
        ret.add(new Object[]{"(javascript:|vbscript:|view-source:)*", Pattern.CASE_INSENSITIVE});
        ret.add(new Object[]{"<(\"[^\"]*\"|\'[^\']*\'|[^\'\">])*>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL});
        ret.add(new Object[]{"(window\\.location|window\\.|\\.location|document\\.cookie|document\\.|alert\\(.*?\\)|window\\.open\\()*", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL});
        ret.add(new Object[]{"<+\\s*\\w*\\s*(oncontrolselect|oncopy|oncut|ondataavailable|ondatasetchanged|ondatasetcomplete|ondblclick|ondeactivate|ondrag|ondragend|ondragenter|ondragleave|ondragover|ondragstart|ondrop|onerror=|onerroupdate|onfilterchange|onfinish|onfocus|onfocusin|onfocusout|onhelp|onkeydown|onkeypress|onkeyup|onlayoutcomplete|onload|onlosecapture|onmousedown|onmouseenter|onmouseleave|onmousemove|onmousout|onmouseover|onmouseup|onmousewheel|onmove|onmoveend|onmovestart|onabort|onactivate|onafterprint|onafterupdate|onbefore|onbeforeactivate|onbeforecopy|onbeforecut|onbeforedeactivate|onbeforeeditocus|onbeforepaste|onbeforeprint|onbeforeunload|onbeforeupdate|onblur|onbounce|oncellchange|onchange|onclick|oncontextmenu|onpaste|onpropertychange|onreadystatuschange|onreset|onresize|onresizend|onresizestart|onrowenter|onrowexit|onrowsdelete|onrowsinserted|onscroll|onselect|onselectionchange|onselectstart|onstart|onstop|onsubmit|onunload)+\\s*=+", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL});
        return ret;
    }

    /**
     * html 转义符
     */
    private static final Map<String, String> HTML_ESCAPE_CHARACTER_MAP = new HashMap<>();

    static {
        HTML_ESCAPE_CHARACTER_MAP.put("<", "&lt;");
        HTML_ESCAPE_CHARACTER_MAP.put(">", "&gt;");
        HTML_ESCAPE_CHARACTER_MAP.put("\\(", "&#x28;");
        HTML_ESCAPE_CHARACTER_MAP.put("\\)", "&#x29;");
        HTML_ESCAPE_CHARACTER_MAP.put("'", "&#x27;");
        HTML_ESCAPE_CHARACTER_MAP.put("\"", "&quot;");
        HTML_ESCAPE_CHARACTER_MAP.put("/", "&#x2f;");
    }

    public static List<Pattern> getXssPatterns() {
        return XSS_SCRIPT_PATTERNS;
    }

    private static Map<String, String> getHtmlEscapeCharacterMap() {
        return HTML_ESCAPE_CHARACTER_MAP;
    }


    public static String stripXss(String value) {
        if (StringUtils.isNotBlank(value)) {
            Matcher matcher = null;
            for (Pattern pattern : getXssPatterns()) {
                matcher = pattern.matcher(value);
                if (matcher.find()) {
                    value = matcher.replaceAll("");
                }
            }
            value = value.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
        }
        return value;
    }

    /**
     * 密码强度
     *
     * @return Z = 字母 S = 数字 T = 特殊字符
     */
    public static String checkPassword(String passwordStr) {
        String regexZ = "\\d*";
        String regexS = "[a-zA-Z]+";
        String regexT = "\\W+$";
        String regexZT = "\\D*";
        String regexST = "[\\d\\W]*";
        String regexZS = "\\w*";
        String regexZST = "[\\w\\W]*";

        if (passwordStr.matches(regexZ)) {
            return "弱";
        }
        if (passwordStr.matches(regexS)) {
            return "弱";
        }
        if (passwordStr.matches(regexT)) {
            return "弱";
        }
        if (passwordStr.matches(regexZT)) {
            return "中";
        }
        if (passwordStr.matches(regexST)) {
            return "中";
        }
        if (passwordStr.matches(regexZS)) {
            return "中";
        }
        if (passwordStr.matches(regexZST)) {
            return "强";
        }
        return passwordStr;
    }


    /**
     * 将 Exception 转化为 String
     */
    public static String convertExceptionToString(Throwable e) {
        if (e == null) {
            return "";
        }
        StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }
}

