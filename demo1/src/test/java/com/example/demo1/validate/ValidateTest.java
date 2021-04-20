package com.example.demo1.validate;

import com.example.demo1.BaseControllerTest;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.FileInputStream;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ValidateTest extends BaseControllerTest {

    private static final ResourceLoader resourceLoader = new DefaultResourceLoader();

    @Test
    public void fileTest() throws Exception {
        String fileName = "application.properties";
        File file = resourceLoader.getResource(fileName).getFile();
        MockMultipartFile mockFile = new MockMultipartFile("uploadFile", fileName,
                "text/plain", new FileInputStream(file));
        uploadFile("/validate/file/2", mockFile)
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("{\"code\":\"0\",\"msg\":\"success\",\"data\":\"application.properties\"}")));
    }

    @Test
    public void fileTestFail() throws Exception {
        String fileName = "application.properties";
        File file = resourceLoader.getResource(fileName).getFile();
        MockMultipartFile mockFile = new MockMultipartFile("uploadFile", fileName,
                "text/plain", new FileInputStream(file));
        uploadFile("/validate/file/1", mockFile)
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("{\"code\":\"0x0000013a\",\"msg\":\"shoulder.validate.input.mimeType.illegal\",\"data\":[\"uploadFile\"]}")));
    }
}
