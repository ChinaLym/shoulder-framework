package org.shoulder.batch.config.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * csv 导入/导出 文件格式定义
 *
 * @author lym
 */
@Getter
@Setter
@NoArgsConstructor
public class ExportColumnConfig {

    /**
     * 业务/领域模型字段名称，如 Person 类的 name 字段需要对应该列，则会有一个 Column 的 modelName=name
     */
    private String modelFieldName;

    /**
     * 列名 - 多语言key，使用者定义
     */
    private String columnNameI18nKey;

    /**
     * 国际化处理后的的列名，用于导出时展示
     */
    private transient String columnName;

    /**
     * 列信息描述 - 多语言key
     */
    private String descriptionI18nKey;

    /**
     * 列信息描述，用于导出时展示
     */
    private String description;

    public ExportColumnConfig(String modelFieldName, String columnName) {
        this.modelFieldName = modelFieldName;
        this.columnName = columnName;
    }

    @Override
    public String toString() {
        return "Column{" +
               "modelName='" + modelFieldName + '\'' +
               ", columnNameI18nKey='" + columnNameI18nKey + '\'' +
               ", descriptionI18nKey='" + descriptionI18nKey + '\'' +
               '}';
    }
}
