package org.shoulder.batch.config;

import com.fasterxml.jackson.core.type.TypeReference;
import org.shoulder.batch.config.model.ExportColumnConfig;
import org.shoulder.batch.config.model.ExportFileConfig;
import org.shoulder.batch.config.model.ExportLocalizeConfig;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.i18.Translator;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;
import org.shoulder.core.util.AssertUtils;
import org.shoulder.core.util.ContextUtils;
import org.shoulder.core.util.JsonUtils;
import org.shoulder.core.util.StringUtils;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 导出辅助
 *
 * @author lym
 */
public class DefaultExportConfigManager implements ExportConfigManager {

    private static final Logger log = LoggerFactory.getLogger(DefaultExportConfigManager.class);

    private final String LOCALIZE_FILE_PATH = "META-INF/export-localize.json";

    /**
     * 数据类型 - 导出配置 - 语言标识, 编码/分隔符映射
     */
    private final Map<String, ExportLocalizeConfig> LOCALIZE_CACHE = new ConcurrentHashMap<>(readResourceToLocalizeMap(LOCALIZE_FILE_PATH));

    /**
     * 数据类型 - 导出配置
     * 使用者来调用 {@link ExportConfigManager#addFileConfig} 方法填充
     */
    private final Map<String, ExportFileConfig> CONFIG_CACHE = new ConcurrentHashMap<>();

    private final Map<String, Map<String, ExportFileConfig>> LOCALIZE_CONFIG_CACHE = new ConcurrentHashMap<>();

    private static Map<String, ExportLocalizeConfig> readResourceToLocalizeMap(String jsonListFile) {
        try (InputStream inputStream = DefaultExportConfigManager.class.getClassLoader().getResourceAsStream(jsonListFile)) {
            List<ExportLocalizeConfig> exportLocalizeList = JsonUtils.parseObject(inputStream, new TypeReference<>() {
            });
            return exportLocalizeList.stream()
                .collect(Collectors.toMap(ExportLocalizeConfig::getLanguageId, c -> c, (c1, c2) -> c2));
        } catch (IOException e) {
            log.warn("csv_localize.json read failed! csv localize disabled.", e);
            return Collections.emptyMap();
        }
    }

    @Override
    public void addLocalizeConfig(ExportLocalizeConfig exportLocalizeConfig) {
        LOCALIZE_CACHE.put(exportLocalizeConfig.getLanguageId(), exportLocalizeConfig);
    }

    @Override
    public ExportLocalizeConfig findLocalizeConfig(Locale locale) {
        return LOCALIZE_CACHE.get(locale.getLanguage());
    }

    @Override
    public void addFileConfig(ExportFileConfig exportFileConfig) {
        AssertUtils.notNull(exportFileConfig, CommonErrorCodeEnum.ILLEGAL_PARAM);
        AssertUtils.notNull(exportFileConfig.getId(), CommonErrorCodeEnum.ILLEGAL_PARAM);
        AssertUtils.notEmpty(exportFileConfig.getHeaders(), CommonErrorCodeEnum.ILLEGAL_PARAM);
        AssertUtils.notEmpty(exportFileConfig.getColumns(), CommonErrorCodeEnum.ILLEGAL_PARAM);
        AssertUtils.isTrue(exportFileConfig.getColumns().size() >= exportFileConfig.getHeaders().size(), CommonErrorCodeEnum.ILLEGAL_PARAM);
        CONFIG_CACHE.put(exportFileConfig.getId(), exportFileConfig);
    }

    @Override
    public ExportFileConfig findFileConfig(String csvId) {
        return CONFIG_CACHE.get(csvId);
    }

    @Override
    public ExportFileConfig getFileConfigWithLocale(String templateId, Locale locale) {
        return LOCALIZE_CONFIG_CACHE.computeIfAbsent(templateId, id -> new ConcurrentHashMap<>())
            .computeIfAbsent(locale.getLanguage(),
                languageId -> renderFileConfigWithLocale(templateId, locale)
            );
    }

    public ExportFileConfig renderFileConfigWithLocale(String id, Locale locale) {
        ExportFileConfig exportFileConfig = findFileConfig(id);
        if (exportFileConfig == null) {
            return null;
        }
        localizeExportConfig(exportFileConfig, locale);
        return exportFileConfig;
    }

    /**
     * 对exportConfig进行本地化
     *
     * @param exportFileConfig 导出配置
     * @param locale           地区、语言
     */
    private void localizeExportConfig(ExportFileConfig exportFileConfig, Locale locale) {
        // 编码 / 分隔符 本地化
        ExportLocalizeConfig exportLocalize = findLocalizeConfig(locale);
        if (exportLocalize != null) {
            exportFileConfig.setEncode(exportLocalize.getEncoding());
            exportFileConfig.setSeparator(exportLocalize.getDelimiter().charAt(0));
        } else {
            log.info("locale {} is not found, fall back to default", locale);
        }
        // header 注释信息-介绍
        if (CollectionUtils.isEmpty(exportFileConfig.getHeaders())) {
            AssertUtils.notEmpty(exportFileConfig.getHeadersI18n(), CommonErrorCodeEnum.ILLEGAL_PARAM);
            List<String> headers = new ArrayList<>();
            headers.add("# #### note######################");
            for (String headerI18n : exportFileConfig.getHeadersI18n()) {
                headers.add(ContextUtils.getBean(Translator.class).getMessage(headerI18n, new Object[] {}, locale));
            }
            headers.add("# ##################################");
            exportFileConfig.setHeaders(headers);
        } else {
            List<String> headers = exportFileConfig.getHeaders()
                .stream()
                .map(h -> h.startsWith("#") ? h : "# " + h)
                .collect(Collectors.toList());
            exportFileConfig.setHeaders(headers);
        }

        // 模型字段
        for (ExportColumnConfig column : exportFileConfig.getColumns()) {
            AssertUtils.notEmpty(column.getModelFieldName(), CommonErrorCodeEnum.ILLEGAL_PARAM);
            if (StringUtils.isEmpty(column.getColumnName())) {
                AssertUtils.notEmpty(column.getColumnNameI18nKey(), CommonErrorCodeEnum.ILLEGAL_PARAM);
                column.setColumnName(
                    ContextUtils.getBean(Translator.class).getMessage(column.getColumnNameI18nKey(), new Object[] {}, locale));
            }
            if (StringUtils.isEmpty(column.getDescription()) && StringUtils.isNotEmpty(column.getDescriptionI18nKey())) {
                column.setDescription(
                    ContextUtils.getBean(Translator.class).getMessage(column.getDescriptionI18nKey(), new Object[] {}, locale));
            }
        }

    }

}
