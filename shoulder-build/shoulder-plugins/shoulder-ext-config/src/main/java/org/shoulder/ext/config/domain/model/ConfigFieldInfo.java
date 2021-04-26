package org.shoulder.ext.config.domain.model;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;

/**
 * @author lym
 */
public class ConfigFieldInfo {

    /**
     * 展示顺序
     */
    private int orderNum;

    /**
     * 名称：用于与数据关联
     */
    private String name;

    /**
     * 名称用于前端表格header/编辑label 字段名展示
     */
    private String displayName;

    /**
     * 字段类型
     * 为 枚举 列出所有枚举值
     * 为 Object 列出所有子字段信息
     * 为 List/Map 提供参数信息
     * 为 Boolean 提供勾选框
     * 为 时间 类型提供特殊的选择框
     */
    private Class<?> type;

    /**
     * 是否为索引（必定非空，无法修改）
     */
    private boolean index;

    /**
     * 不能为null / 空串
     */
    private boolean notNull;

    /**
     * 不能只有空格
     */
    private boolean notBlank;

    /**
     * 最小长度
     */
    private int minLength;

    /**
     * 最大长度
     */
    private int maxLength = Integer.MAX_VALUE;

    /**
     * 最小值
     */
    private long min = Integer.MIN_VALUE;

    /**
     * 最大值（默认是 int 范围，为了兼容 js 中 long 的范围与 java 不一致）
     */
    private long max = Integer.MAX_VALUE;

    /**
     * 正则表达式
     */
    private String regex;

    /**
     * 提示文本
     */
    private String description;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * getter
     */
    private Method readMethod;

    /**
     * setter
     */
    private Method writeMethod;

    /**
     * Getter method for property <tt>orderNum</tt>.
     *
     * @return property value of orderNum
     */
    public int getOrderNum() {
        return orderNum;
    }

    /**
     * Setter method for property <tt>orderNum</tt>.
     *
     * @param orderNum value to be assigned to property orderNum
     */
    public void setOrderNum(int orderNum) {
        this.orderNum = orderNum;
    }

    /**
     * Getter method for property <tt>name</tt>.
     *
     * @return property value of name
     */
    public String getName() {
        return name;
    }

    /**
     * Setter method for property <tt>name</tt>.
     *
     * @param name value to be assigned to property name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter method for property <tt>displayName</tt>.
     *
     * @return property value of displayName
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Setter method for property <tt>displayName</tt>.
     *
     * @param displayName value to be assigned to property displayName
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Getter method for property <tt>type</tt>.
     *
     * @return property value of type
     */
    public Class<?> getType() {
        return type;
    }

    /**
     * Setter method for property <tt>type</tt>.
     *
     * @param type value to be assigned to property type
     */
    public void setType(Class<?> type) {
        this.type = type;
    }

    /**
     * Getter method for property <tt>index</tt>.
     *
     * @return property value of index
     */
    public boolean isIndex() {
        return index;
    }

    /**
     * Setter method for property <tt>index</tt>.
     *
     * @param index value to be assigned to property index
     */
    public void setIndex(boolean index) {
        this.index = index;
    }

    /**
     * Getter method for property <tt>notEmpty</tt>.
     *
     * @return property value of notEmpty
     */
    public boolean isNotNull() {
        return notNull;
    }

    /**
     * Setter method for property <tt>notEmpty</tt>.
     *
     * @param notNull value to be assigned to property notEmpty
     */
    public void setNotNull(boolean notNull) {
        this.notNull = notNull;
    }

    /**
     * Getter method for property <tt>notBlank</tt>.
     *
     * @return property value of notBlank
     */
    public boolean isNotBlank() {
        return notBlank;
    }

    /**
     * Setter method for property <tt>notBlank</tt>.
     *
     * @param notBlank value to be assigned to property notBlank
     */
    public void setNotBlank(boolean notBlank) {
        this.notBlank = notBlank;
    }

    /**
     * Getter method for property <tt>minLength</tt>.
     *
     * @return property value of minLength
     */
    public int getMinLength() {
        return minLength;
    }

    /**
     * Setter method for property <tt>minLength</tt>.
     *
     * @param minLength value to be assigned to property minLength
     */
    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    /**
     * Getter method for property <tt>maxLength</tt>.
     *
     * @return property value of maxLength
     */
    public int getMaxLength() {
        return maxLength;
    }

    /**
     * Setter method for property <tt>maxLength</tt>.
     *
     * @param maxLength value to be assigned to property maxLength
     */
    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    /**
     * Getter method for property <tt>min</tt>.
     *
     * @return property value of min
     */
    public long getMin() {
        return min;
    }

    /**
     * Setter method for property <tt>min</tt>.
     *
     * @param min value to be assigned to property min
     */
    public void setMin(long min) {
        this.min = min;
    }

    /**
     * Getter method for property <tt>max</tt>.
     *
     * @return property value of max
     */
    public long getMax() {
        return max;
    }

    /**
     * Setter method for property <tt>max</tt>.
     *
     * @param max value to be assigned to property max
     */
    public void setMax(long max) {
        this.max = max;
    }

    /**
     * Getter method for property <tt>regex</tt>.
     *
     * @return property value of regex
     */
    public String getRegex() {
        return regex;
    }

    /**
     * Setter method for property <tt>regex</tt>.
     *
     * @param regex value to be assigned to property regex
     */
    public void setRegex(String regex) {
        this.regex = regex;
    }

    /**
     * Getter method for property <tt>description</tt>.
     *
     * @return property value of description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Setter method for property <tt>description</tt>.
     *
     * @param description value to be assigned to property description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Getter method for property <tt>defaultValue</tt>.
     *
     * @return property value of defaultValue
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * Setter method for property <tt>defaultValue</tt>.
     *
     * @param defaultValue value to be assigned to property defaultValue
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * Getter method for property <tt>readMethod</tt>.
     *
     * @return property value of readMethod
     */
    @Nonnull
    public Method getReadMethod() {
        return readMethod;
    }

    /**
     * Setter method for property <tt>readMethod</tt>.
     *
     * @param readMethod value to be assigned to property readMethod
     */
    public void setReadMethod(@Nonnull Method readMethod) {
        this.readMethod = readMethod;
    }

    /**
     * Getter method for property <tt>writeMethod</tt>.
     *
     * @return property value of writeMethod
     */
    @Nonnull
    public Method getWriteMethod() {
        return writeMethod;
    }

    /**
     * Setter method for property <tt>writeMethod</tt>.
     *
     * @param writeMethod value to be assigned to property writeMethod
     */
    public void setWriteMethod(@Nonnull Method writeMethod) {
        this.writeMethod = writeMethod;
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this,
                ToStringStyle.SHORT_PREFIX_STYLE);
    }
}