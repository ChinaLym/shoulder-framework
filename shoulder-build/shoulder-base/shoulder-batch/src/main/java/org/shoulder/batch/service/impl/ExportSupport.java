package org.shoulder.batch.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import org.shoulder.batch.model.ExportConfig;
import org.shoulder.core.context.AppContext;
import org.shoulder.core.i18.Translator;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;
import org.shoulder.core.util.ContextUtils;
import org.shoulder.core.util.JsonUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 导出辅助
 *
 * @author lym
 */
public class ExportSupport {

    private static final Logger log = LoggerFactory.getLogger(ExportSupport.class);

    private static final String LOCALIZE_FILE_PATH = "META-INF/export-localize.json";

    private static final Map<String, ExportLocalize> LOCALIZE_CACHE = new HashMap<>();

    // todo put 数据类型 - 导出配置
    private static final Map<String, ExportConfig> CONFIG_CACHE = new HashMap<>();

    static {
        Resource resource = new ClassPathResource(LOCALIZE_FILE_PATH);
        try (InputStream inputStream = resource.getInputStream()) {
            List<ExportLocalize> exportLocalizeList = JsonUtils.toObject(inputStream, new TypeReference<>() {
            });
            for (ExportLocalize exportLocalize : exportLocalizeList) {
                LOCALIZE_CACHE.put(exportLocalize.getLanguageId(), exportLocalize);
            }
        } catch (IOException e) {
            log.warn("csv_localize.json read failed! csv localize disabled.", e);
        }
    }

    public static ExportLocalize getLocalizeByLanguageId(String languageId) {
        return LOCALIZE_CACHE.get(languageId);
    }


    public static ExportConfig getConfig(String csvId) {
        return CONFIG_CACHE.get(csvId);
    }

    public static void putConfig(String csvId, ExportConfig exportConfig) {
        CONFIG_CACHE.put(csvId, exportConfig);
    }


    public static ExportConfig getConfigWithLocale(String id) {
        ExportConfig exportConfig = getConfig(id);
        if (exportConfig == null) {
            return null;
        }
        localizeExportConfig(exportConfig);
        return exportConfig;
    }

    /**
     * 对exportConfig进行本地化
     *
     * @param exportConfig 导出配置
     */
    private static void localizeExportConfig(ExportConfig exportConfig) {
        // 编码 / 分隔符 本地化
        String languageId = AppContext.getLocale().getLanguage();
        ExportLocalize exportLocalize = getLocalizeByLanguageId(languageId);
        if (exportLocalize != null) {
            exportConfig.setEncode(exportLocalize.getEncoding());
            exportConfig.setSeparator(exportLocalize.getDelimiter().charAt(0));
        } else {
            log.info("can't find languageId {} in export-localize.json, fall back to default", languageId);
        }
        // 详情
        List<String> headers = new ArrayList<>();
        for (String header : exportConfig.getHeadersI18n()) {
            headers.add(ContextUtils.getBean(Translator.class).getMessage(header));
        }
        for (ExportConfig.Column column : exportConfig.getColumns()) {
            column.setColumnName(ContextUtils.getBean(Translator.class).getMessage(column.getColumnNameI18n()));
        }
        exportConfig.setHeaders(headers);
    }

    public static class ExportLocalize {
        public ExportLocalize() {
        }

        public ExportLocalize(String languageId, String encoding, String delimiter) {
            this.languageId = languageId;
            this.encoding = encoding;
            this.delimiter = delimiter;
        }

        private String languageId;

        private String encoding;

        private String delimiter;

        public String getLanguageId() {
            return languageId;
        }

        public String getEncoding() {
            return encoding;
        }

        public String getDelimiter() {
            return delimiter;
        }

        public void setLanguageId(String languageId) {
            this.languageId = languageId;
        }

        public void setEncoding(String encoding) {
            this.encoding = encoding;
        }

        public void setDelimiter(String delimiter) {
            this.delimiter = delimiter;
        }
    }

}
