package org.shoulder.batch.config.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * csv 导出样式-配置，如 csv 的分隔符、换行符等
 *
 * 国际化使用
 *
 * @author lym
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExportLocalizeConfig {

    private String languageId;

    private String encoding;

    private String delimiter;

}
