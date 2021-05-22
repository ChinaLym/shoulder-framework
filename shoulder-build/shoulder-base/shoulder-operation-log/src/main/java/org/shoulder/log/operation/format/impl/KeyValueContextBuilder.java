package org.shoulder.log.operation.format.impl;

import org.shoulder.core.util.StringUtils;

/**
 * 键值对文本构建器
 *
 * @author lym
 */
public class KeyValueContextBuilder {

    private final StringBuilder context = new StringBuilder();

    /**
     * key value 之间分割符
     */
    private final String keyValueSplit;

    /**
     * 多个键值对之间分割符
     */
    private final String keyPairSplit;

    public KeyValueContextBuilder() {
        // 默认 key value 之间用冒号分割，多个键值对之间用逗号分割
        this(":", ",");
    }

    public KeyValueContextBuilder(String keyValueSplit, String keyPairSplit) {
        this.keyValueSplit = keyValueSplit;
        this.keyPairSplit = keyPairSplit;
    }

    /**
     * 当且仅当 value 不为空才拼接
     */
    public KeyValueContextBuilder addIfValueNotEmpty(String key, String value) {
        if (StringUtils.isNotEmpty(value)) {
            add(key, value);
        }
        return this;
    }

    public KeyValueContextBuilder add(String key, Object value) {
        this.context.append(key)
                .append(keyValueSplit).append("\"")
                .append(value)
                .append("\"").append(keyPairSplit);
        return this;
    }

    public String formatResult() {
        // 去掉最后一个分隔符
        context.setLength(context.length() - 1);
        return context.toString();
    }

}
