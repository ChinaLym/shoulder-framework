package org.shoulder.batch.xml;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.naming.NoNameCoder;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.security.AnyTypePermission;
import org.junit.Test;
import org.shoulder.batch.spec.ShopBO;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.UUID;

/**
 * 测试 XStream api
 *
 * @author lym
 */
public class XstreamTest {

    private static XStream DEFAULT_X_STREAM;

    static {
        DEFAULT_X_STREAM = new XStream(new DomDriver("UTF-8", new NoNameCoder()));
        XStream.setupDefaultSecurity(DEFAULT_X_STREAM);
        DEFAULT_X_STREAM.ignoreUnknownElements();
        DEFAULT_X_STREAM.autodetectAnnotations(true);
        DEFAULT_X_STREAM.addPermission(AnyTypePermission.ANY);
    }

    @Test
    public void writeToXml() throws FileNotFoundException {
        ShopBO shopBO = new ShopBO();
        ShopBO.Owner boss = new ShopBO.Owner();
        boss.setName("bossName");
        boss.setAge(20);
        shopBO.setId(UUID.randomUUID().toString());
        shopBO.setName("name");
        shopBO.setAddr("addr");
        shopBO.setBoss(boss);

        File outPutFile = new File("E:\\shop.xml");
        DEFAULT_X_STREAM.toXML(shopBO, new FileOutputStream(outPutFile));
    }

    @Test
    public void readFromXml() throws FileNotFoundException {
        File outPutFile = new File("E:\\shop.xml");
        System.out.println(DEFAULT_X_STREAM.fromXML(outPutFile));
        ShopBO shopBO = (ShopBO) DEFAULT_X_STREAM.fromXML(outPutFile);
        System.out.println(shopBO);
    }

}
