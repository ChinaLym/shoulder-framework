package org.shoulder.autoconfiguration.test.batch;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.shoulder.autoconfigure.batch.BatchTaskAutoConfiguration;
import org.shoulder.autoconfigure.core.I18nAutoConfiguration;
import org.shoulder.batch.config.ExportConfigManager;
import org.shoulder.batch.config.model.ExportColumnConfig;
import org.shoulder.batch.config.model.ExportFileConfig;
import org.shoulder.batch.constant.BatchConstants;
import org.shoulder.batch.service.ExportService;
import org.shoulder.core.context.AppContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootTest(
    properties = "shoulder.batch.export-file-config-locations=classpath:batch/exportFileConfig.json",
    classes = { I18nAutoConfiguration.class, BatchTaskAutoConfiguration.class })
public class ExportConfigManagerTest {

    private static String templateId = "testBatchDataType";

    @Autowired
    ExportConfigManager exportConfigManager;


    //@BeforeEach
    public void initConfig() {
        if(exportConfigManager.findFileConfig(templateId) != null) {
            return;
        }
        ExportFileConfig exportFileConfig = new ExportFileConfig();
        exportFileConfig.setId(templateId);
        exportFileConfig.setHeaders(List.of("First", "Second"));
        exportFileConfig.setColumns(
            Stream.of("First", "Second")
                .map(s -> new ExportColumnConfig(s, s))
                .collect(Collectors.toList())
        );
        //exportConfigManager.addFileConfig(exportFileConfig);
    }

    @Test
    public void templateTest() {
        ExportFileConfig exportFileConfig = exportConfigManager.getFileConfigWithLocale(templateId, AppContext.getLocaleOrDefault());
        Assertions.assertNotNull(exportFileConfig);
    }

    /**
     * 导出
     */
    @Autowired
    private ExportService exportService;

    @Test
    public void templateOutputTest() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(2048000);
        exportService.export(out, BatchConstants.CSV, Collections.emptyList(), templateId);
        System.out.println(out.toString("gb2312"));
    }

}
