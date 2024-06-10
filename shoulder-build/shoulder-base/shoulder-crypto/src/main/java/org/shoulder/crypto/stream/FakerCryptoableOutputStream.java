package org.shoulder.crypto.stream;

import javax.crypto.Cipher;
import java.io.*;

/**
 * TODO
 *
 * @author lym
 */
public class FakerCryptoableOutputStream extends BufferedOutputStream {

    protected byte[] buf;
    protected int    count;
    private   int    mode;

    /**
     * Cipher.ENCRYPT_MODE
     */
    public FakerCryptoableOutputStream(OutputStream out, int mode) {
        this(out, 8192, mode);
    }

    public FakerCryptoableOutputStream(OutputStream out, int size, int mode) {
        super(out);
        if (size <= 0) {
            throw new IllegalArgumentException("Buffer size <= 0");
        }
        buf = new byte[size];
        this.mode = mode;
    }

    public static void encryptFile(String from, String to) throws IOException {
        doCipher(from, to, Cipher.ENCRYPT_MODE);
    }

    public static void decryptFile(String from, String to) throws IOException {
        doCipher(from, to, Cipher.DECRYPT_MODE);
    }

    public static void doCipher(String from, String to, int mode) throws IOException {
        File readFile = new File(from);
        File writeFile = new File(to);
        FileInputStream in = new FileInputStream(readFile);
        FakerCryptoableOutputStream out = new FakerCryptoableOutputStream(new FileOutputStream(writeFile), mode);
        byte[] buffer = new byte[4096];
        int n;
        while ((n = in.read(buffer)) != -1) {
            out.write(buffer, 0, n);
        }
        in.close();
        out.close();
    }

    @Override
    public synchronized void write(int b) throws IOException {
        if (count >= buf.length) {
            flushBuffer();
        }
        buf[count++] = (byte) b;
    }

    @Override
    public synchronized void write(byte[] b, int off, int len) throws IOException {
        if (len >= buf.length) {
            flushBuffer();
            out.write(b, off, len);
            return;
        }
        if (len > buf.length - count) {
            flushBuffer();
        }
        System.arraycopy(b, off, buf, count, len);
        count += len;
    }

    @Override
    public synchronized void flush() throws IOException {
        flushBuffer();
        out.flush();
    }

    private void flushBuffer() throws IOException {
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                buf[i] ^= 26;
            }
            out.write(buf, 0, count);
            count = 0;
        }
    }
}
