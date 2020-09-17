package org.shoulder.core.util;

import org.shoulder.core.context.AppInfo;
import org.springframework.lang.NonNull;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 控制台颜色
 * <p>
 * 2/3、>=60 的控制符未被納入规范
 * <p>
 * https://blog.csdn.net/ShewMi/article/details/78992458
 *
 * @author lym
 */
public class ColorString {

    /**
     * 颜色转义符前缀
     */
    private static final String COLOR_PREFIX;

    static {
        //init color ansi prefix 是颜色转义符('\u001b') 在不同编码中不同
        Map<Charset, Character> charsetMap = new HashMap<>();
        //'0x1B'、'\033'、'00011011'
        charsetMap.put(StandardCharsets.UTF_8, '\033');
        charsetMap.put(StandardCharsets.UTF_16, '\27');
        COLOR_PREFIX = charsetMap.getOrDefault(AppInfo.charset(), '\033') + "[";
    }

    /**
     * 颜色重置字符串，一般作为后缀
     */
    private static final String RESET = COLOR_PREFIX + "0m";

    /**
     * 字体色起始
     */
    public static final int FONT_COLOR = 30;
    /**
     * 背景色起始
     */
    public static final int BG_COLOR = 40;

    /**
     * 字体色起始（非标准）
     */
    public static final int FONT_COLOR_LIGHT = 90;
    /**
     * 背景色起始（非标准）
     */
    public static final int BG_COLOR_LIGHT = 100;

    /* ============= 颜色偏移 =========== */
    public static final int BLACK = 0;
    public static final int RED = 1;
    public static final int GREEN = 2;
    public static final int YELLOW = 3;
    public static final int BLUE = 4;
    public static final int MAGENTA = 5;
    public static final int CYAN = 6;
    public static final int WHITE = 7;


    /* ============================= 变量 =========================== */

    /**
     * 字符内容
     */
    private CharSequence content;

    /**
     * 样式
     */
    private int style = 0;

    /**
     * 字符颜色
     */
    private int color = FONT_COLOR;

    public ColorString() {
    }

    public ColorString(CharSequence content) {
        this.content = content;
    }

    public ColorString(CharSequence content, Style style, int color) {
        this.content = content;
        style(style);
        color(color);
    }

    public int getStyle() {
        return style;
    }

    public ColorString style(Style style) {
        this.style = style.getCode();
        return this;
    }

    public int getColor() {
        return color;
    }

    public ColorString color(int color) {
        this.color = color + FONT_COLOR;
        return this;
    }

    public ColorString lColor(int color) {
        this.color = color + FONT_COLOR_LIGHT;
        return this;
    }

    public ColorString bgColor(int color) {
        this.color = color + BG_COLOR;
        return this;
    }

    public ColorString lbgColor(int color) {
        this.color = color + BG_COLOR_LIGHT;
        return this;
    }

    public CharSequence getContent() {
        return content;
    }

    public ColorString setContent(CharSequence content) {
        this.content = content;
        return this;
    }

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
            return o instanceof CharSequence && this.content.equals(o);
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

    /**
     * 0 – Reset / Normal
     * 1 – Bold: treated as intensity under Windows console, user option in this plugin)
     * 2 – Intensity faint: “kind of” supported :-) It resets the intensity to normal.
     * 3 – Italic: on (treated as inverse under Windows console, user option in this plugin)
     * 4 – Underline
     * 7 – Negative
     * 8 – Conceal
     * 9 – Crossed-out
     * 21 – Double underline
     * 22 – Bold off (normal intensity)
     * 23 – Italic off
     * 24 – Underline off
     * 27 – Negative off
     * 28 – Conceal off
     * 29 – Crossed-out off
     * 30-37 – Set text color
     * 38 – Set xterm-256 text color
     * 39 – Default text color
     * 40 – 47 – Set background color
     * 48 – Set xterm-256 background color
     * 49 – Default background color
     * 51 – Framed
     * 54 – Framed off
     * 90-97 – Set foreground color, high intensity
     * 100-107 – Set background color, high intensity
     *
     * @author lym
     */
    public enum Style {

        /**
         * 设置为正常格式
         */
        NORMAL(0),
        /**
         * 粗体
         */
        BOLD(1),
        /**
         * 斜体
         */
        ITALIC(3),
        /**
         * 闪烁
         */
        TWINKLE(6),
        /**
         * 划掉。删除线
         */
        DELETE(9),
        /**
         * 下划线
         */
        UNDERLINE(24),
        ;

        private final int code;

        Style(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

    }
}
