package org.shoulder.batch;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.shoulder.batch.config.DefaultExportConfigManager;
import org.shoulder.batch.config.ExportConfigManager;
import org.shoulder.batch.config.model.ExportColumnConfig;
import org.shoulder.batch.config.model.ExportFileConfig;
import org.shoulder.batch.test.TestStarter;
import org.shoulder.core.context.AppContext;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootTest(classes = TestStarter.class)
public class ExportConfigManagerTest {

    private static String templateId = "testId";

    static ExportConfigManager exportConfigManager = new DefaultExportConfigManager();

    static {
        ExportFileConfig exportFileConfig = new ExportFileConfig();
        exportFileConfig.setHeaders(List.of("First", "Second"));
        exportFileConfig.setColumns(
                Stream.of("First", "Second")
                        .map(s -> new ExportColumnConfig(s, s))
                        .collect(Collectors.toList())
        );
        exportConfigManager.addFileConfig(templateId, exportFileConfig);
    }

    @Test
    public void templateTest() {
        ExportFileConfig exportFileConfig = exportConfigManager.getFileConfigWithLocale(templateId, AppContext.getLocaleOrDefault()); exportConfigManager.getFileConfigWithLocale(templateId, AppContext.getLocaleOrDefault());
        Assertions.assertNotNull(exportFileConfig);
    }

}
