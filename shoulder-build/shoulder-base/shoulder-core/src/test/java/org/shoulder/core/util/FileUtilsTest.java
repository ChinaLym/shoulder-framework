package org.shoulder.core.util;

import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Set;

public class FileUtilsTest {


    private static final String parentFilePath = "F:\\files\\test\\";

    private static final String realFileName = "F:\\files\\test\\real.";
    private static final String fakerFileName = "F:\\files\\test\\faker.";

    private Set<String> needTestFileType = FileUtils.getFileHeaders().keySet();


    @Test
    public void testAll() throws IOException {
        for (String ext : needTestFileType) {
            checkRealFileHeader(ext);
            checkFakerFileHeader(ext);
        }
    }

    private void checkRealFileHeader(String extType) throws IOException {
        File f = new File(realFileName + extType);
        if (f.exists()) {
            String extname = FileUtils.getExtName(f.getName());
            assert FileUtils.checkHeader(new FileInputStream(f), extname, true);
        } else {
            System.err.println("ignore real extType check: " + extType);
        }
    }

    private void checkFakerFileHeader(String extType) throws IOException {
        File f = new File(fakerFileName + extType);
        if (f.exists()) {
            String extname = FileUtils.getExtName(f.getName());
            assert !FileUtils.checkHeader(new FileInputStream(f), extname, true);
        } else {
            System.err.println("ignore faker extType check: " + extType);
        }
    }

}
