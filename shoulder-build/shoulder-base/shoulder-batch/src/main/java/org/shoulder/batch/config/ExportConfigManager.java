package org.shoulder.batch.config;

import org.shoulder.batch.config.model.ExportFileConfig;
import org.shoulder.batch.config.model.ExportLocalizeConfig;

import java.util.Locale;

/**
 * 导出辅助
 *
 * @author lym
 */
public interface ExportConfigManager {

    // ----------------------- ExportLocalizeConfig --------------

    void addLocalizeConfig(ExportLocalizeConfig exportLocalizeConfig);

    ExportLocalizeConfig findLocalizeConfig(Locale locale);

    // ----------------------- ExportFileConfig --------------

    void addFileConfig(ExportFileConfig exportFileConfig);

    ExportFileConfig findFileConfig(String csvId);

    ExportFileConfig getFileConfigWithLocale(String templateId, Locale locale);
}
