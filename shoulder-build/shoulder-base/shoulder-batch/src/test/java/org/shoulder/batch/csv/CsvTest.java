package org.shoulder.batch.csv;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.naming.NoNameCoder;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.security.AnyTypePermission;
import com.univocity.parsers.common.record.Record;
import com.univocity.parsers.csv.CsvFormat;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.shoulder.batch.spec.ShopBO;
import org.shoulder.core.context.AppInfo;

import java.io.*;
import java.util.List;
import java.util.UUID;

/**
 * 测试 csv 读取，写出
 *
 * @author lym
 */
public class CsvTest {

    private static final XStream DEFAULT_X_STREAM;

    private static final String FILE_PATH = System.getProperty("user.home") + "/" + "shoulder_batch_test_shop.xml";

    static {
        DEFAULT_X_STREAM = new XStream(new DomDriver("UTF-8", new NoNameCoder()));
        XStream.setupDefaultSecurity(DEFAULT_X_STREAM);
        DEFAULT_X_STREAM.ignoreUnknownElements();
        DEFAULT_X_STREAM.autodetectAnnotations(true);
        DEFAULT_X_STREAM.addPermission(AnyTypePermission.ANY);
        // register
        DEFAULT_X_STREAM.processAnnotations(new Class[]{ShopBO.class});
        DEFAULT_X_STREAM.allowTypes(new Class[]{ShopBO.class});
    }

    @Test
    public void readCsvHeaderTest() throws IOException {
        File file = new File("D:\\code\\java\\self\\shoulder-framework\\shoulder-build\\shoulder-base\\shoulder-batch\\src\\test\\resources\\batchImportTest.csv");
        CsvParserSettings settings = new CsvParserSettings();
        settings.setFormat(new CsvFormat());
        settings.setNumberOfRecordsToRead(10000);
        // 忽略的注释行
        settings.setCommentCollectionEnabled(false);
        CsvParser csvParser = new CsvParser(settings);

        List<Record> recordList;
        try (InputStreamReader in = new InputStreamReader(new FileInputStream(file), AppInfo.charset())) {
            recordList = csvParser.parseAllRecords(in);
        }
        Record firstRecord = recordList.get(0);

        System.out.println(firstRecord);
    }


    @Test
    public void writeAndReadTest() throws IOException {
        ShopBO shopBO = new ShopBO();
        ShopBO.Owner boss = new ShopBO.Owner();
        boss.setName("bossName");
        boss.setAge(20);
        shopBO.setId(UUID.randomUUID().toString());
        shopBO.setName("name");
        shopBO.setAddr("addr");
        shopBO.setBoss(boss);

        File outPutFile = new File(FILE_PATH);
        DEFAULT_X_STREAM.toXML(shopBO, new FileOutputStream(outPutFile));

        // Read
        ShopBO shopBOR = (ShopBO) DEFAULT_X_STREAM.fromXML(outPutFile);
        Assertions.assertEquals(shopBOR, shopBO);
        outPutFile.deleteOnExit();

    }

}
