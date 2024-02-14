package org.shoulder.autoconfiguration.test.batch;

import org.junit.jupiter.api.Test;
import org.shoulder.autoconfiguration.test.BaseWebTest;
import org.shoulder.autoconfigure.batch.BatchTaskAutoConfiguration;
import org.shoulder.autoconfigure.core.I18nAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.io.FileInputStream;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author lym
 */
@SpringBootTest(
    properties = "shoulder.batch.export-file-config-locations=classpath:batch/exportFileConfig.json",
    classes = { I18nAutoConfiguration.class, BatchTaskAutoConfiguration.class })
public class ImportControllerTest extends BaseWebTest {

    @Autowired
    protected MockMvc mockMvc;

    @Test
    public void testImportAndValidate() throws Exception {
        String fileName = "batch/batchImportTest.csv";
        File file = resourceLoader.getResource(fileName).getFile();
        MockMultipartFile mockFile = new MockMultipartFile("uploadFile", fileName,
            "text/plain", new FileInputStream(file));

        uploadFile("/api/v1/batch/testId/validate", mockFile)
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("{\"code\":\"0\",\"msg\":\"success\",\"data\":\"")));
    }
}
