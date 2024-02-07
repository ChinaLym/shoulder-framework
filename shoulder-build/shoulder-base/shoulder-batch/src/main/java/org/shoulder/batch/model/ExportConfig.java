package org.shoulder.batch.model;

import lombok.*;
import org.shoulder.core.context.AppInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 导出配置（头部信息）
 *
 * @author lym
 */
@Data
@ToString
@NoArgsConstructor
public class ExportConfig {

    // ---------------- 导出模板配置 ----------------------

    /**
     * 配置标识
     */
    private String id;

    /**
     * 导出转换扩展点 【暂未使用】
     */
    private String exportMapping;

    /**
     * 头部信息 - 多语言key
     */
    private List<String> headersI18n;

    /**
     * 国际化处理后的头部信息（框架会根据当前语言环境自动填充）
     */
    private List<String> headers;

    /**
     * 列信息
     */
    private List<Column> columns;

    // ---------------- 导出配置 （框架会根据当前语言环境自动填充）----------------------

    /**
     * 编码
     */
    private String encode;
    /**
     * 同行不同字段分隔符
     * 英语单词：separator 中表示多个单次的分割，delimiter 表示每个单次后面加一个分隔符
     */
    private char separator = ',';
    /**
     * 行分隔符
     */
    private String lineSeparator = "\n";
    /**
     * 注释标识
     */
    private char comment = '#';
    /**
     * 引号
     */
    private char quote = '"';
    /**
     * 引号逃逸时
     */
    private char quoteEscape = '"';


    public ExportConfig(String exportMapping) {
        this.encode = AppInfo.charset().name();
        this.headers = new ArrayList<>();
        this.columns = new ArrayList<>();
        this.exportMapping = exportMapping;
    }

    public ExportConfig(Character separator, String encode, String exportMapping) {
        this(exportMapping);
        this.separator = separator;
        this.encode = encode;
    }

    /**
     * 获得第x列的column对象,如果不存在返回为null
     *
     * @param index : 第n列，起始为1
     * @return 对应的列，如果不存在返回null
     */
    public Column getColumnByIndex(int index) {
        try {
            return columns.get(index - 1);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    @Getter
    @Setter
    public static class Column {

        /**
         * 业务/领域模型字段名称，如 Person 类的 name 字段需要对应该列，则会有一个 Column 的 modelName=name
         */
        private String modelFieldName;

        /**
         * 列名 - 多语言key，使用者定义
         */
        private String columnNameI18n;

        /**
         * 国际化处理后的的列名，用于导出时展示
         */
        private transient String columnName;

        /**
         * 列信息描述 - 多语言key
         */
        private String descriptionI18n;

        /**
         * 列信息描述，用于导出时展示
         */
        private String description;


        public Column() {
        }

        public Column(String modelFieldName, String columnName) {
            this.modelFieldName = modelFieldName;
            this.columnName = columnName;
        }

        @Override
        public String toString() {
            return "Column{" +
                    "modelName='" + modelFieldName + '\'' +
                ", columnNameI18n='" + columnNameI18n + '\'' +
                ", descriptionI18n='" + descriptionI18n + '\'' +
                '}';
        }
    }
}
