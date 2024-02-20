package org.shoulder.batch.config.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.shoulder.core.context.AppInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 导出模板: 头部信息、字段信息
 * 该配置信息为全局默认配置，不包含多语言，国际化/本地化配置
 *
 * @author lym
 */
@Data
@ToString
@NoArgsConstructor
public class ExportFileConfig {

    // ---------------- 导出模板配置 ----------------------

    /**
     * 配置标识
     */
    private String id;

    /**
     * 导出转换扩展点
     * @deprecated【暂未使用】
     */
    private String exportMapping;

    /**
     * 注释行
     */
//    private List<String> commentLinesI18n;

    /**
     * 注释行
     */
    private List<String> commentLines;

    /**
     * 国际化处理后的头部信息（框架会根据当前语言环境自动填充）
     */
    private List<String> headers;

    /**
     * 列信息
     */
    private List<ExportColumnConfig> columns;

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


    public ExportFileConfig(String exportMapping) {
        this.encode = AppInfo.charset().name();
        this.headers = new ArrayList<>();
        this.columns = new ArrayList<>();
        this.exportMapping = exportMapping;
    }

    public ExportFileConfig(Character separator, String encode, String exportMapping) {
        this(exportMapping);
        this.separator = separator;
        this.encode = encode;
    }

    /**
     * 获得第x列的column对象,如果不存在返回为null
     *
     * @param index : 第n列，起始为1
     * @return 对应的列，如果不存在返回null
     * @deprecated 容易写bug，弃用
     */
    public ExportColumnConfig getColumnByIndex(int index) {
        try {
            return columns.get(index - 1);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }


    @Override
    public ExportFileConfig clone() {
        ExportFileConfig clone = new ExportFileConfig();
        clone.setId(this.getId());
        clone.setExportMapping(this.getExportMapping());

        // Clone collection fields
        clone.setCommentLines(this.getCommentLines() == null ? null : new ArrayList<>(this.getCommentLines()));
        clone.setHeaders(this.getHeaders() == null ? null : new ArrayList<>(this.getHeaders()));

        // Deep clone the columns list
        clone.setColumns(this.columns.stream()
                .map(ExportColumnConfig::clone)
                .toList());

        return clone;
    }

}
