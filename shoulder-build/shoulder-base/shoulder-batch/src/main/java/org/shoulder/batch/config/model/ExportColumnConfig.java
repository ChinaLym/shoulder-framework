package org.shoulder.batch.config.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.shoulder.core.dto.ToStringObj;

import java.io.Serial;

/**
 * csv 导入/导出 文件格式定义
 *
 * @author lym
 */
@Getter
@Setter
@NoArgsConstructor
public class ExportColumnConfig extends ToStringObj {

    @Serial
    private static final long serialVersionUID = -9003361621730630092L;

    /**
     * 业务/领域模型字段名称，如 Person 类的 name 字段需要对应该列，则会有一个 Column 的 modelName=name
     */
    private String modelField;

    /**
     * 列名 - 多语言key，使用者定义
     */
    private String displayNameI18n;

    /**
     * 国际化处理后的的列名，用于导出时展示
     */
    private String displayName;

    /**
     * 列信息描述 - 多语言key
     */
    private String descriptionI18n;

    /**
     * 列信息描述，用于导出时展示
     */
    private String description;

    public ExportColumnConfig(String modelField, String displayName) {
        this.modelField = modelField;
        this.displayName = displayName;
    }

    @Override
    public ExportColumnConfig clone() {
        ExportColumnConfig clone = new ExportColumnConfig();
        clone.setModelField(this.getModelField());
        clone.setDisplayNameI18n(this.getDisplayNameI18n());
        clone.setDisplayName(this.getDisplayName());
        clone.setDescriptionI18n(this.getDescriptionI18n());
        clone.setDescription(this.getDescription());
        return clone;
    }

}
