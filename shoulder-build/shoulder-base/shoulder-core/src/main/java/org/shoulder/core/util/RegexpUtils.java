package org.shoulder.core.util;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正则表达式工具
 *
 * @author lym
 */
public class RegexpUtils {
    /**
     * 用于匹配任意字符串的非贪婪表达式
     */
    public static final String MATCH_ANY_STRING = ".*?";

    /**
     * 正则表达式编译缓存池(pattern是线程安全的，Matcher不是)
     */
    private static final int COMPILE_CACHE_SIZE = 64;
    private static final Map<String, Pattern> CACHE = new ConcurrentHashMap<>(COMPILE_CACHE_SIZE);

    public static final char[] STAR_QUESTION = new char[]{'*', '?', '+'};

    //常见匹配预定义
    public static final String MATCH_ANY = ".";
    public static final String MATCH_NUMBER = "\\d";
    public static final String MATCH_NON_NUMBER = "\\D";
    public static final String MATCH_BLANK = "\\s";
    public static final String MATCH_NON_BLANK = "\\S";
    public static final String MATCH_WORD_CHAR = "\\w";
    public static final String MATCH_NON_WORD_CHAR = "\\W";
    public static final String MATCH_START = "^";
    public static final String MATCH_END = "$";

    //常见数量词预定义
    public static final String COUNT_0_N = "*";
    public static final String COUNT_1_N = "+";
    public static final String COUNT_0_1 = "?";
    public static final String COUNT_0_N_RELUCTANT = "*?";
    public static final String COUNT_1_N_RELUCTANT = "+?";
    public static final String COUNT_0_1_RELUCTANT = "??";

    /**
     * 字母开头包含数字或下划线，常用于账号
     */
    public static final Pattern CHAR_NUM_UNDERLINE = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]{4,15}$");
    /**
     * 不含特殊字符，常用于昵称等
     */
    public static final Pattern NO_SPECIAL = Pattern.compile("^[^'/\\\\:*?\"<>|]{1,32}$");
    /**
     * 中文
     */
    public static final Pattern CHINESE = Pattern.compile("\\u4e00-\\u9fa5");
    /**
     * href
     */
    public static final Pattern HREF_LINK = Pattern.compile("([hH])([rR])([eE])([fF]) *= *(['\"])?(\\w|\\\\|/|\\.)+('|\"| *|>)?");
    /**
     * 数字（实数）
     */
    public static final Pattern NUM = Pattern.compile("(-?\\d*)(\\.\\d+)?");
    /**
     * 整数
     */
    public static final Pattern INTEGER = Pattern.compile("\\d+");
    /**
     * base64 格式字符串
     */
    public static final Pattern BASE_64 = Pattern.compile("^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$");

    /**
     * 中国大陆手机号
     */
    public static final Pattern PHONE_NUM = Pattern.compile("[1]([3-9])[0-9]{9}");
    /**
     * 中国大陆手机号（严格匹配）
     */
    public static final Pattern PHONE_NUM_STRICT = Pattern.compile("[1](([3][0-9])|([4][5-9])|([5][0-3,5-9])|([6][5,6])|([7][0-8])|([8][0-9])|([9][1,8,9]))[0-9]{8}");
    /**
     * 中国大陆 身份证 15/18位，最后一位可以为x
     */
    public static final Pattern ID_CARD = Pattern.compile("\\d{15}|\\d{18}|\\d{17}x|\\d{17}X");
    /**
     * 中国大陆 邮编 6位数字，非0开头
     */
    public static final Pattern POSTCODE = Pattern.compile("[1-9]\\d{5}(?!\\d)");
    /**
     * 中国大陆 车牌号 民用机动车 新能源 警车 武警车 领事馆车 军用车
     */
    public static final Pattern LICENSE_PLATE = Pattern.compile("([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼]" +
        "(([A-HJ-Z][A-HJ-NP-Z0-9]{5})|([A-HJ-Z](([DF][A-HJ-NP-Z0-9][0-9]{4})|([0-9]{5}[DF])))" +
        "|([A-HJ-Z][A-D0-9][0-9]{3}警)))|([0-9]{6}使)|((([沪粤川云桂鄂陕蒙藏黑辽渝]A)|鲁B|闽D|蒙E|蒙H)[0-9]{4}领)" +
        "|(WJ[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼·•][0-9]{4}[TDSHBXJ0-9])" +
        "|([VKHBSLJNGCE][A-DJ-PR-TVY][0-9]{5})");

    /**
     * QQ
     */
    public static final Pattern QQ = Pattern.compile("[1-9]\\d{4,10}");
    /**
     * 微信号
     */
    public static final Pattern WE_CHAT = Pattern.compile("[a-zA-Z][-_a-zA-Z0-9]{5,19}");
    /**
     * 邮箱
     */
    public static final Pattern EMAIL = Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");
    /**
     * 链接
     */
    public static final Pattern URL = Pattern.compile("[a-zA-z]+://[^\\s]*");
    /**
     * IP地址
     */
    public static final Pattern IP = Pattern.compile("(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])");
    /**
     * 端口号
     */
    public static final Pattern PORT = Pattern.compile("([0-9]|[1-9]\\d{1,3}|[1-5]\\d{4}|6[0-4]\\d{3}|65[0-4]\\d{2}|655[0-2]\\d|6553[0-5])");
    /**
     * MAC 地址
     */
    public static final Pattern MAC = Pattern.compile("([A-Fa-f0-9]{2}-){5}[A-Fa-f0-9]{2}");

    /**
     * Java 预定义的字符，凡是字符串中出现这些字符的话，都需要转义
     * 注 Java使用\b来表示单词边界而非使用尖括号
     * 中划线、与符号 当且仅当出现在方括号中时会被当作元字符
     * 正则表达式在编译中，对于不匹配的括号会当作字面值在处理，但是对于+ * ?则会严格处理，不会当作字面值
     */
    public static final char[] REGEXP_KEY_CHARS = new char[]{
        '\\',    //将下一个字符标记为或特殊字符、或原义字符、或后向引用、或八进制转义符。必须为第一个（替换顺序）
        '(', ')',//匹配分组关键字
        '{', '}',//匹配次数关键字
        '[', ']',//匹配字符集描述字
        '*',    //匹配次数快捷定义：0..n
        '+',    //匹配次数快捷定义: 1..n,也用于描述占有匹配
        '?',    //匹配次数快捷定义: 0..1，也用于辅助描述非贪婪描述（即要求匹配取尽量少的匹配次数)
        '.',    //匹配任意字符
        '$',    //匹配行尾
        '^',    //有两种含义，匹配行首。在[]中时表示对字符集合求非
        '|'        //用于分隔若干项匹配，表示其中之一
    };

    /**
     * 将使用简易匹配语法的关键字修改为标准正则表达式
     * 简易语法就是用*表示匹配任意数量字符，用?表示匹配0~1个字符，其他字符一律按照字面理解。 这是为了和Windows用户的习惯相吻合
     */
    public static String simpleMatchToRegexp(String key) {
        //将除了* ?之外的字符全部修改为正则表达式原义
        key = escapeRegChars(key, STAR_QUESTION);
        key = StringUtils.replaceEach(key, new String[]{"*", "?", "+"}, new String[]{".*", ".?", ".+"});
        return key;
    }

    /**
     * 得到简易通配的正则pattern
     */
    public static Pattern simplePattern(String key, boolean IgnoreCase, boolean matchStart, boolean matchEnd, boolean wildcardSpace) {
        if (IgnoreCase) {
            key = key.toUpperCase();
        }
        // 将除了指定字符之外的所有正则元字符全部用转义符更改为字面含义
        String regStr = simpleMatchToRegexp(key);
        regStr = ((matchStart) ? "" : RegexpUtils.MATCH_ANY_STRING) + ((wildcardSpace) ? key.replace(" ", "\\s+") : regStr) + ((matchEnd) ? "" : RegexpUtils.MATCH_ANY_STRING);
        return Pattern.compile(regStr);
    }

    /**
     * 将除了指定字符之外的所有正则元字符全部用转义符更改为字面含义
     */
    public static String escapeRegChars(String key, char[] keeps) {
        for (char c : REGEXP_KEY_CHARS) {
            if (!ArrayUtils.contains(keeps, c)) {
                key = key.replace(String.valueOf(c), "\\" + c);
            }
        }
        return key;
    }

    /**
     * 只要字符串中有部分匹配正则表达式，就返回true
     */
    public static boolean contains(String str, String regexp) {
        return matches(str, regexp, false);
    }

    /**
     * 效果等同于String.matches. 不过可以使用缓存
     */
    public static boolean matches(String str, String regexp) {
        return matches(str, regexp, true);
    }

    /**
     * 判断字符串和正则表达式是否匹配
     */
    public static boolean matches(String str, String regexp, boolean strict) {
        Matcher m = getMatcher(str, regexp, strict);
        return m.matches();
    }

    public static String[] getSimpleMatchResult(String str, String key, boolean strict) {
        key = escapeRegChars(key, STAR_QUESTION);
        key = StringUtils.replaceEach(key, new String[]{"*", "?", "+"}, new String[]{"(.*)", "(.?)", "(.+)"});
        return getMatcherResult(str, key, strict);
    }

    /**
     * 得到匹配结果分组
     *
     * @param str    字符串
     * @param regexp 正则表达式
     * @param strict 使用严格方式，即只有字符完全匹配上正则表达式时才返回结果
     * @return 如果没有匹配上，返回一个null。
     */
    public static String[] getMatcherResult(String str, String regexp, boolean strict) {
        if (!strict) {
            str = StringUtils.remove(str, "\\(");
            String tmp = StringUtils.remove(str, "\\)");
            boolean hasLeft = tmp.indexOf('(') > -1;
            boolean hasRight = tmp.indexOf(')') > -1;
            if (!hasLeft || !hasRight) {
                //补充一个默认分组
                regexp = "(" + regexp + ")";
            }
        }
        Matcher m = getMatcher(str, regexp, strict);
        if (!m.matches()) {
            return null;
        }
        int n = m.groupCount();
        if (n == 0) {
            return new String[]{m.group()};
        }
        String[] result = new String[n];
        for (int i = 1; i <= n; i++) {
            result[i - 1] = m.group(i);
        }
        return result;
    }

    private static Matcher getMatcher(String str, String regexp, boolean strict) {
        if (!strict) {
            regexp = MATCH_ANY_STRING + regexp + MATCH_ANY_STRING;
        }
        Pattern p = CACHE.get(regexp);
        if (p == null) {
            p = Pattern.compile(regexp);
            addToCache(p);
        }
        return p.matcher(str);
    }

    //将Pattern缓存
    private static void addToCache(Pattern p) {
        if (CACHE.size() == COMPILE_CACHE_SIZE) {
            CACHE.clear();
        }
        CACHE.put(p.pattern(), p);
    }
}
