package org.shoulder.batch.config;

import com.fasterxml.jackson.core.type.TypeReference;
import org.shoulder.batch.config.model.ExportColumnConfig;
import org.shoulder.batch.config.model.ExportFileConfig;
import org.shoulder.batch.config.model.ExportLocalizeConfig;
import org.shoulder.batch.log.ShoulderBatchLoggers;
import org.shoulder.core.context.AppInfo;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.i18.Translator;
import org.shoulder.core.log.Logger;
import org.shoulder.core.util.AssertUtils;
import org.shoulder.core.util.ContextUtils;
import org.shoulder.core.util.JsonUtils;
import org.shoulder.core.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 导出辅助
 *
 * @author lym
 */
public class DefaultExportConfigManager implements ExportConfigManager {

    private static final Logger log = ShoulderBatchLoggers.DEFAULT;

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
//        AssertUtils.notEmpty(exportFileConfig.getHeaders(), CommonErrorCodeEnum.ILLEGAL_PARAM);
        AssertUtils.notEmpty(exportFileConfig.getColumns(), CommonErrorCodeEnum.ILLEGAL_PARAM);
//        AssertUtils.isTrue(exportFileConfig.getColumns().size() >= exportFileConfig.getHeaders().size(), CommonErrorCodeEnum.ILLEGAL_PARAM);
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
        ExportFileConfig clone = exportFileConfig.clone();
        localizeExportConfig(clone, locale);
        return clone;
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
            String encode = AppInfo.charset().name();
            exportFileConfig.setEncode(encode);
            log.info("locale {} is not found, fall back to default, and encode with app.default={}.", locale, encode);
        }
        // 模型字段
        for (ExportColumnConfig column : exportFileConfig.getColumns()) {
            AssertUtils.notEmpty(column.getModelField(), CommonErrorCodeEnum.ILLEGAL_PARAM);
            if (StringUtils.isEmpty(column.getDisplayName())) {
                AssertUtils.notEmpty(column.getDisplayNameI18n(), CommonErrorCodeEnum.ILLEGAL_PARAM);
                column.setDisplayName(ContextUtils.getBeanOptional(Translator.class)
                        .map(t -> t.getMessage(column.getDisplayNameI18n(), new Object[]{}, locale))
                        // 没有 Translator 则使用 modelField 作为 默认值
                        .orElse(column.getModelField())
                );
            }
            if (StringUtils.isEmpty(column.getDescription()) && StringUtils.isNotEmpty(column.getDescriptionI18n())) {
                column.setDescription(
                        ContextUtils.getBeanOptional(Translator.class)
                                .map(t -> t.getMessage(column.getDescriptionI18n(), new Object[]{}, locale))
                                // 没有 Translator 则默认值为空
                                .orElse("")

                );
            }
        }

        // 注释/介绍（多行）
//        if (!CollectionUtils.isEmpty(exportFileConfig.getCommentLines())) {
//            List<String> commentLines = exportFileConfig.getCommentLines()
//                    .stream()
//                    .map(h -> h.startsWith("#") ? h : "# " + h)
//                    .collect(Collectors.toList());
//            exportFileConfig.setCommentLines(commentLines);
//        } else {
//            AssertUtils.notEmpty(exportFileConfig.getCommentLinesI18n(), CommonErrorCodeEnum.ILLEGAL_PARAM);
//            List<String> commentLines = new ArrayList<>();
//            for (String commentI18n : exportFileConfig.getCommentLinesI18n()) {
//                String afterTrans = ContextUtils.getBean(Translator.class)
//                        .getMessage(commentI18n, new Object[]{}, locale);
//                commentLines.add(afterTrans.startsWith("#") ? afterTrans : "# " + afterTrans);
//            }
//            exportFileConfig.setCommentLines(commentLines);
//        }

        // commentLines-auto
        exportFileConfig.setCommentLines(new ArrayList<>(3 + exportFileConfig.getColumns().size()));
        String commentChar = "" + exportFileConfig.getComment();
        exportFileConfig.getCommentLines().add(0, commentChar + exportFileConfig.getId() + commentChar + locale + commentChar + (exportFileConfig.getColumns().size() + 3) + commentChar.repeat(10));
        exportFileConfig.getColumns().forEach(c -> exportFileConfig.getCommentLines().add(
                commentChar + " " + c.getDisplayName() + ": " + c.getDescription())
        );
        exportFileConfig.getCommentLines().add(commentChar + " " + commentChar.repeat(34) + exportFileConfig.getLineSeparator());

        // headers-auto
        List<String> nameList = exportFileConfig.getColumns().stream()
                .map(ExportColumnConfig::getDisplayName)
                .toList();
        exportFileConfig.setHeaders(nameList);

        // header 信息（一行）
//        if (!CollectionUtils.isEmpty(exportFileConfig.getHeaders())) {
//            exportFileConfig.setHeaders(exportFileConfig.getHeaders());
//        } else {
//            AssertUtils.notEmpty(exportFileConfig.getHeadersI18n(), CommonErrorCodeEnum.ILLEGAL_PARAM);
//            List<String> headers = new ArrayList<>();
//            for (String headerI18n : exportFileConfig.getHeadersI18n()) {
//                headers.add(ContextUtils.getBean(Translator.class)
//                        .getMessage(headerI18n, new Object[]{}, locale));
//            }
//            exportFileConfig.setHeaders(headers);
//        }


    }

}
