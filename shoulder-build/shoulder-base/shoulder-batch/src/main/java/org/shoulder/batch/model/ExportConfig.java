package org.shoulder.batch.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * 导出配置
 *
 * @author lym
 */
@Data
@ToString
@NoArgsConstructor
public class ExportConfig {

    /**
     * 配置标识
     */
    private String id;

    /**
     * 导出转换扩展点
     */
    private String exportMapping;
    /**
     * 分隔符
     */
    private char separator;
    /**
     * 编码
     */
    private String encode;

    /**
     * 头部信息 - 多语言key
     */
    private List<String> headersI18n;

    /**
     * 国际化处理后的头部信息
     */
    private List<String> headers;

    /**
     * 列信息
     */
    private List<Column> columns;


    public ExportConfig(String exportMapping) {
        this.encode = "utf-8";
        this.headers = new ArrayList<>();
        this.columns = new ArrayList<>();
        this.exportMapping = exportMapping;
        this.separator = ',';
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

    public static class Column {

        public Column(String columnName, String desc) {
            this.columnName = columnName;
            this.desc = desc;
        }

        public Column() {
        }

        /**
         * 模型名称
         */
        private String modelName;

        /**
         * 国际化处理后的的列名
         */
        private transient String columnName;

        /**
         * 列名 - 多语言key
         */
        private String columnNameI18n;

        /**
         * 列信息描述
         */
        private String desc;

        public String getModelName() {
            return modelName;
        }

        public void setModelName(String modelName) {
            this.modelName = modelName;
        }

        public String getColumnName() {
            return columnName;
        }

        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }

        public String getColumnNameI18n() {
            return columnNameI18n;
        }

        public void setColumnNameI18n(String columnNameI18n) {
            this.columnNameI18n = columnNameI18n;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        @Override
        public String toString() {
            return "Column{" +
                "modelName='" + modelName + '\'' +
                ", columnName='" + columnName + '\'' +
                ", columnNameI18n='" + columnNameI18n + '\'' +
                ", desc='" + desc + '\'' +
                '}';
        }
    }
}
