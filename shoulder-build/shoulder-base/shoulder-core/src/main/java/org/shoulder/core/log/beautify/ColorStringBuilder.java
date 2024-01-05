package org.shoulder.core.log.beautify;

import jakarta.annotation.Nonnull;

import java.util.stream.IntStream;

import static org.shoulder.core.log.beautify.ColorString.*;

/**
 * 带颜色的 StringBuilder
 *
 * @author lym
 */
public class ColorStringBuilder implements CharSequence {


    /**
     * 换行符
     */
    private static final String NEW_LINE_SEPARATOR = System.getProperty("line.separator");

    /**
     * builder
     */
    private StringBuilder delegateBuilder;

    public ColorStringBuilder() {
        this.delegateBuilder = new StringBuilder();
    }


    public ColorStringBuilder(CharSequence seq) {
        this.delegateBuilder = new StringBuilder(seq);
    }

    public int compareTo(StringBuilder another) {
        return this.delegateBuilder.compareTo(another);
    }

    /* ============================== 普通颜色（按住 Shift 鼠标进入可查看颜色） ================================ */

    public ColorStringBuilder color(CharSequence c, int color) {
        this.delegateBuilder.append(new ColorString(c).color(color));
        return this;
    }

    public ColorStringBuilder black(CharSequence c) {
        this.delegateBuilder.append(new ColorString(c).color(BLACK));
        return this;
    }

    public ColorStringBuilder red(CharSequence c) {
        this.delegateBuilder.append(new ColorString(c).color(RED));
        return this;
    }

    public ColorStringBuilder green(CharSequence c) {
        this.delegateBuilder.append(new ColorString(c).color(GREEN));
        return this;
    }

    public ColorStringBuilder yellow(CharSequence c) {
        this.delegateBuilder.append(new ColorString(c).color(YELLOW));
        return this;
    }

    public ColorStringBuilder blue(CharSequence c) {
        this.delegateBuilder.append(new ColorString(c).color(BLUE));
        return this;
    }

    public ColorStringBuilder magenta(CharSequence c) {
        this.delegateBuilder.append(new ColorString(c).color(MAGENTA));
        return this;
    }

    public ColorStringBuilder cyan(CharSequence c) {
        this.delegateBuilder.append(new ColorString(c).color(CYAN));
        return this;
    }

    public ColorStringBuilder white(CharSequence c) {
        this.delegateBuilder.append(new ColorString(c).color(WHITE));
        return this;
    }
    /* ============================== 普通颜色（按住 Shift 鼠标进入可查看颜色） ================================ */

    public ColorStringBuilder black(CharSequence c, ColorString.Style style, boolean... light) {
        ColorString colorString = new ColorString(c, style, BLACK);
        if (light != null) {
            colorString.lColor(BLACK);
        }
        this.delegateBuilder.append(colorString);
        return this;
    }

    public ColorStringBuilder red(CharSequence c, ColorString.Style style, boolean... light) {
        ColorString colorString = new ColorString(c, style, RED);
        if (light != null) {
            colorString.lColor(RED);
        }
        this.delegateBuilder.append(colorString);
        return this;
    }

    public ColorStringBuilder green(CharSequence c, ColorString.Style style, boolean... light) {
        ColorString colorString = new ColorString(c, style, GREEN);
        if (light != null) {
            colorString.lColor(GREEN);
        }
        this.delegateBuilder.append(colorString);
        return this;
    }

    public ColorStringBuilder yellow(CharSequence c, ColorString.Style style, boolean... light) {
        ColorString colorString = new ColorString(c, style, YELLOW);
        if (light != null) {
            colorString.lColor(YELLOW);
        }
        this.delegateBuilder.append(colorString);
        return this;
    }

    public ColorStringBuilder blue(CharSequence c, ColorString.Style style, boolean... light) {
        ColorString colorString = new ColorString(c, style, BLUE);
        if (light != null) {
            colorString.lColor(BLUE);
        }
        this.delegateBuilder.append(colorString);
        return this;
    }

    public ColorStringBuilder magenta(CharSequence c, ColorString.Style style, boolean... light) {
        ColorString colorString = new ColorString(c, style, MAGENTA);
        if (light != null) {
            colorString.lColor(MAGENTA);
        }
        this.delegateBuilder.append(colorString);
        return this;
    }

    public ColorStringBuilder cyan(CharSequence c, ColorString.Style style, boolean... light) {
        ColorString colorString = new ColorString(c, style, CYAN);
        if (light != null) {
            colorString.lColor(CYAN);
        }
        this.delegateBuilder.append(colorString);
        return this;
    }

    public ColorStringBuilder white(CharSequence c, ColorString.Style style, boolean... light) {
        ColorString colorString = new ColorString(c, style, WHITE);
        if (light != null) {
            colorString.lColor(WHITE);
        }
        this.delegateBuilder.append(colorString);
        return this;
    }

    /* ============================== 亮色 ================================ */

    public ColorStringBuilder lBlack(CharSequence c) {
        this.delegateBuilder.append(new ColorString(c).lColor(BLACK));
        return this;
    }

    public ColorStringBuilder lRed(CharSequence c) {
        this.delegateBuilder.append(new ColorString(c).lColor(RED));
        return this;
    }

    public ColorStringBuilder lGreen(CharSequence c) {
        this.delegateBuilder.append(new ColorString(c).lColor(GREEN));
        return this;
    }

    public ColorStringBuilder lYellow(CharSequence c) {
        this.delegateBuilder.append(new ColorString(c).lColor(YELLOW));
        return this;
    }

    public ColorStringBuilder lBlue(CharSequence c) {
        this.delegateBuilder.append(new ColorString(c).lColor(BLUE));
        return this;
    }

    public ColorStringBuilder lMagenta(CharSequence c) {
        this.delegateBuilder.append(new ColorString(c).lColor(MAGENTA));
        return this;
    }

    public ColorStringBuilder lCyan(CharSequence c) {
        this.delegateBuilder.append(new ColorString(c).lColor(CYAN));
        return this;
    }

    public ColorStringBuilder lWhite(CharSequence c) {
        this.delegateBuilder.append(new ColorString(c).lColor(WHITE));
        return this;
    }

    /* ============================== append ================================ */

    public ColorStringBuilder newLine() {
        this.delegateBuilder.append(NEW_LINE_SEPARATOR);
        return this;
    }

    public ColorStringBuilder newLine(String s) {
        return newLine().append(s);
    }

    public ColorStringBuilder newLine(CharSequence s) {
        return newLine().append(s);
    }

    public ColorStringBuilder tab() {
        this.delegateBuilder.append('\t');
        return this;
    }

    public ColorStringBuilder append(Object obj) {
        return this.append(String.valueOf(obj));
    }

    public ColorStringBuilder append(String str) {
        this.delegateBuilder.append(str);
        return this;
    }

    public ColorStringBuilder append(StringBuffer sb) {
        this.delegateBuilder.append(sb);
        return this;
    }

    public ColorStringBuilder append(CharSequence s) {
        this.delegateBuilder.append(s);
        return this;
    }

    public ColorStringBuilder append(CharSequence s, int start, int end) {
        this.delegateBuilder.append(s, start, end);
        return this;
    }

    public ColorStringBuilder append(char[] str) {
        this.delegateBuilder.append(str);
        return this;
    }

    public ColorStringBuilder append(char[] str, int offset, int len) {
        this.delegateBuilder.append(str, offset, len);
        return this;
    }

    public ColorStringBuilder append(boolean b) {
        this.delegateBuilder.append(b);
        return this;
    }

    public ColorStringBuilder append(char c) {
        this.delegateBuilder.append(c);
        return this;
    }

    public ColorStringBuilder append(int i) {
        this.delegateBuilder.append(i);
        return this;
    }

    public ColorStringBuilder append(long lng) {
        this.delegateBuilder.append(lng);
        return this;
    }

    public ColorStringBuilder append(float f) {
        this.delegateBuilder.append(f);
        return this;
    }

    public ColorStringBuilder append(double d) {
        this.delegateBuilder.append(d);
        return this;
    }

    public ColorStringBuilder appendCodePoint(int codePoint) {
        this.delegateBuilder.appendCodePoint(codePoint);
        return this;
    }

    public ColorStringBuilder delete(int start, int end) {
        this.delegateBuilder.delete(start, end);
        return this;
    }

    public ColorStringBuilder deleteCharAt(int index) {
        this.delegateBuilder.deleteCharAt(index);
        return this;
    }

    public ColorStringBuilder replace(int start, int end, String str) {
        this.delegateBuilder.replace(start, end, str);
        return this;
    }

    public ColorStringBuilder insert(int index, char[] str, int offset, int len) {
        this.delegateBuilder.insert(index, str, offset, len);
        return this;
    }

    public ColorStringBuilder insert(int offset, Object obj) {
        this.delegateBuilder.insert(offset, obj);
        return this;
    }

    public ColorStringBuilder insert(int offset, String str) {
        this.delegateBuilder.insert(offset, str);
        return this;
    }

    public ColorStringBuilder insert(int offset, char[] str) {
        this.delegateBuilder.insert(offset, str);
        return this;
    }

    public ColorStringBuilder insert(int dstOffset, CharSequence s) {
        this.delegateBuilder.insert(dstOffset, s);
        return this;
    }

    public ColorStringBuilder insert(int dstOffset, CharSequence s, int start, int end) {
        this.delegateBuilder.insert(dstOffset, s, start, end);
        return this;
    }

    public ColorStringBuilder insert(int offset, boolean b) {
        this.delegateBuilder.insert(offset, b);
        return this;
    }

    public ColorStringBuilder insert(int offset, char c) {
        this.delegateBuilder.insert(offset, c);
        return this;
    }

    public ColorStringBuilder insert(int offset, int i) {
        this.delegateBuilder.insert(offset, i);
        return this;
    }

    public ColorStringBuilder insert(int offset, long l) {
        this.delegateBuilder.insert(offset, l);
        return this;
    }

    public ColorStringBuilder insert(int offset, float f) {
        this.delegateBuilder.insert(offset, f);
        return this;
    }

    public ColorStringBuilder insert(int offset, double d) {
        this.delegateBuilder.insert(offset, d);
        return this;
    }

    public int indexOf(String str) {
        return this.delegateBuilder.indexOf(str);
    }

    public int indexOf(String str, int fromIndex) {
        return this.delegateBuilder.indexOf(str, fromIndex);
    }

    public int lastIndexOf(String str) {
        return this.delegateBuilder.lastIndexOf(str);
    }

    public int lastIndexOf(String str, int fromIndex) {
        return this.delegateBuilder.lastIndexOf(str, fromIndex);
    }

    public ColorStringBuilder reverse() {
        this.delegateBuilder.reverse();
        return this;
    }

    @Nonnull
    @Override
    public String toString() {
        return this.delegateBuilder.toString();
    }

    @Override
    public boolean equals(Object o) {
        return this.delegateBuilder.equals(o);
    }

    @Override
    public int hashCode() {
        return this.delegateBuilder.hashCode();
    }

    @Override
    public int length() {
        return this.delegateBuilder.length();
    }

    @Override
    public char charAt(int index) {
        return this.delegateBuilder.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return this.delegateBuilder.subSequence(start, end);
    }

    @Override
    public IntStream chars() {
        return this.delegateBuilder.chars();
    }

    @Override
    public IntStream codePoints() {
        return this.delegateBuilder.codePoints();
    }

}
