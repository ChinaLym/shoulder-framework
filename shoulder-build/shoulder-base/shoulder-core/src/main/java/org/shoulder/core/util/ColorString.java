package org.shoulder.core.util;

import org.shoulder.core.context.AppInfo;
import org.springframework.lang.NonNull;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

/**
 * 控制台颜色
 * <p>
 * 2/3、>=60 的控制符未被納入规范
 * <p>
 * https://blog.csdn.net/ShewMi/article/details/78992458
 *
 * @author lym
 */
public class ColorString implements CharSequence {

    // 是颜色转义符，在 UTF-8 中
    private static final String COLOR_PREFIX;

    static {
        //init color ansi prefix 是颜色转义符('\u001b') 在不同编码中不同
        Map<Charset, Character> charsetMap = new HashMap<>();
        //'0x1B'、'\033'、'00011011'
        charsetMap.put(StandardCharsets.UTF_8, '\033');
        charsetMap.put(StandardCharsets.UTF_16, '\27');
        COLOR_PREFIX = String.valueOf(charsetMap.getOrDefault(AppInfo.charset(), '\033')) + "[";
    }

    private static final String RESET = COLOR_PREFIX + "[0m";

    /**
     * 关闭格式
     */
    public static final int NORMAL = 0;
    /**
     * 粗体
     */
    public static final int BLOD = 1;
    /**
     * 斜体
     */
    //public static final int BLOD = 3;
    /**
     * 下划线
     */
    public static final int UNDERLINE = 1;

    /**
     * 字体色起始
     */
    public static final int FONT_COLOR = 30;
    /**
     * 背景色起始
     */
    public static final int BACK_GROUD_COLOR = 40;

    /**
     * 字体色起始（非标准）
     */
    public static final int FONT_COLOR_LIGHT = 90;
    /**
     * 背景色起始（非标准）
     */
    public static final int BACK_GROUD_COLOR_LIGHT = 100;

    public static final int BLACK = 0;
    public static final int RED = 1;
    public static final int GREEN = 2;
    public static final int YELLOW = 3;
    public static final int BLUE = 4;
    public static final int MAGENTA = 5;
    public static final int CYAN = 6;
    public static final int WHITE = 7;


    private int style = 0;

    private int color = FONT_COLOR;

    private CharSequence content;


    @NonNull
    @Override
    public String toString() {
        return COLOR_PREFIX + style + ";" + color + "m" + content + RESET;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ColorString)) {
            return false;
        }
        ColorString that = (ColorString) o;
        return style == that.style &&
            color == that.color &&
            content.equals(that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(style, color, content);
    }

    @Override
    public int length() {
        return this.content.length();
    }

    @Override
    public char charAt(int index) {
        return this.content.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return this.content.subSequence(start, end);
    }

    @Override
    public IntStream chars() {
        return this.content.chars();
    }

    @Override
    public IntStream codePoints() {
        return this.content.codePoints();
    }
}
