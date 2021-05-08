package org.shoulder.core.guid.impl;

/**
 * jdk uuid 去掉 -
 *
 * @author lym
 */
abstract class JdkUuidEnhancer {

    /**
     * 采用URL Base64字符，即把“+/”换成“-_”
     */
    protected static final char[] digits = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-_=".toCharArray();


    protected static String digits(long val, int digits) {
        long hi = 1L << (digits << 2);
        return Long.toHexString(hi | (val & (hi - 1)));
    }


}
