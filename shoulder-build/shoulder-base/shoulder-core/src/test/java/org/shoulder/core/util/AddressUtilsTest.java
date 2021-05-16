package org.shoulder.core.util;

import org.junit.jupiter.api.Test;

/**
 * ip 转换，区间比较
 *
 * @author lym
 */
public class AddressUtilsTest {

    @Test
    public void testConvert() {
        String ip = "243.215.64.23";
        assert ip.equals(AddressUtils.parseIPv4(AddressUtils.toInt(ip)));
        assert ip.equals(AddressUtils.parseIPv4(AddressUtils.toLong(ip)));
        assert ip.equals(AddressUtils.parseIPv4FromHex(AddressUtils.toHexStr(ip)));
    }

    @Test
    public void testIntervalCheck() {
        String ip = "243.215.64.23";
        String start = "200.26.45.65";
        String end = "244.26.45.65";
        assert AddressUtils.isBetweenInterval(ip, start, end);
        assert AddressUtils.isBetweenInterval(AddressUtils.toLong(ip), start, end);
        assert AddressUtils.isBetweenInterval(AddressUtils.toInt(ip), start, end);
        assert AddressUtils.isBetweenIntervalHex(AddressUtils.toHexStr(ip), start, end);
        assert AddressUtils.isBetweenHexInterval(AddressUtils.toHexStr(ip), AddressUtils.toHexStr(start), AddressUtils.toHexStr(end));
    }

}
