package org.shoulder.batch;

import org.junit.jupiter.api.Test;
import org.shoulder.batch.model.ExportConfig;
import org.shoulder.batch.service.impl.DefaultExportConfigManager;
import org.shoulder.batch.test.TestStarter;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootTest(classes = TestStarter.class)
public class ExportConfigManagerTest {

    private static String templateId = "testId";

    static {
        ExportConfig exportConfig = new ExportConfig();
        exportConfig.setHeaders(List.of("First", "Second"));
        exportConfig.setColumns(
                Stream.of("First", "Second")
                        .map(s -> new ExportConfig.Column(s, s))
                        .collect(Collectors.toList())
        );
        DefaultExportConfigManager.putConfig(templateId, exportConfig);
    }

    @Test
    public void templateTest() {
        DefaultExportConfigManager.getConfigWithLocale(templateId);
    }

}
