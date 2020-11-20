package org.shoulder.core.util;

import org.shoulder.core.exception.BaseRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nonnull;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 文件相关工具类
 * todo 【功能】从 xml、json、yml、properties 文件读取成对象
 *
 * @author lym
 */
public class FileUtils {

    private final static Logger log = LoggerFactory.getLogger(FileUtils.class);
    public static final String UPLOAD_FILE_ROOT_PATH = "upload";
    public static final String COMMA_SEPARATOR = ",";
    private static final String TEMP_DIR_NAME = "temp";
    private static final String FILE_SEPARATOR = File.separator;

    /**
     * 文件扩展名 - 数组
     */
    private static final Map<String, byte[]> FILE_HEADER_MAP = new ConcurrentHashMap<>();

    /**
     * 检查/创建文件在所的文件夹
     *
     * @param file
     */
    private static void ensureParentFolder(File file) {
        File f = file.getParentFile();
        if (!f.exists() && !f.mkdirs()) {
            throw new RuntimeException("can not ensure exist for " + f.getAbsolutePath());
        }
    }

    /**
     * 获取文件上传的根路径
     */
    public static String getUploadFileRootPath() {
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        ServletContext servletContext = request.getSession().getServletContext();
        if (servletContext != null) {
            return servletContext.getRealPath(FILE_SEPARATOR + UPLOAD_FILE_ROOT_PATH);
        }
        throw new RuntimeException("not a servlet environment!");
    }

    /**
     * 获取上传文件的临时文件夹路径
     */
    public static String getUploadFileTempPath() {
        return getUploadFileRootPath() + FILE_SEPARATOR + TEMP_DIR_NAME;
    }

    /**
     * 将multipartFile文件转换为file
     *
     * @param file
     */
    public static File getFileFromMultiPartFile(MultipartFile file) throws Exception {
        File goalFile = new File(new File(getUploadFileTempPath()), UUID.randomUUID().toString());
        ensureParentFolder(goalFile);
        file.transferTo(goalFile);
        return goalFile;
    }

    /**
     * 把数据导出到指定文件
     */
    public static boolean exportDataToCsv(OutputStream os, String charSet, String csvHeader, List<String> dataList) {
        try {
            OutputStreamWriter osw = new OutputStreamWriter(os, charSet);
            BufferedWriter bfw = new BufferedWriter(osw);
            bfw.append(csvHeader).append("\r");
            if (dataList != null && !dataList.isEmpty()) {
                for (String data : dataList) {
                    bfw.append(data).append("\r");
                }
            }
            bfw.flush();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 导入指定文件的数据
     */
    public static List<String> importDataFromCsv(File file) {
        List<String> dataList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                dataList.add(line);
            }
        } catch (Exception e) {
            throw new RuntimeException("load by csv fail from " + file.getAbsolutePath());
        }
        return dataList;
    }


    /**
     * 校验文件头
     *
     * @param in      文件的输入流
     * @param extName 文件扩展名，不带点
     * @return 是否合法
     * @throws IOException 流读取异常
     */
    public static boolean checkHeader(@Nonnull InputStream in, @Nonnull String extName) throws IOException {
        String lowExtName = extName.toLowerCase();
        if (FILE_HEADER_MAP.containsKey(lowExtName)) {
            byte[] standHeaderBytes = FILE_HEADER_MAP.get(lowExtName);
            int byteCount = standHeaderBytes.length;
            byte[] headBytes = new byte[byteCount];
            assert in.read(headBytes, 0, byteCount) == byteCount;
            return Arrays.compare(headBytes, standHeaderBytes) == 0;
        } else {
            // 警告，不在里，未校验
            throw new BaseRuntimeException("the extname '" + extName + "' is out of check bounds.");
        }
    }


    private static void addFileHeader(@Nonnull Properties properties) {
        properties.forEach((k, v) -> {
            String standHeaderHex = (String) v;
            log.info("stand file ");
            if (standHeaderHex.startsWith("0x")) {
                standHeaderHex = standHeaderHex.replaceAll("0x", "");
            }
            byte[] standHeaderBytes = new byte[standHeaderHex.length() / 2];
            int j = 0;
            for (int i = 0; i < standHeaderBytes.length; i++) {
                byte high = (byte) (Character.digit(standHeaderHex.charAt(j), 16) & 0xff);
                byte low = (byte) (Character.digit(standHeaderHex.charAt(j + 1), 16) & 0xff);
                standHeaderBytes[i] = (byte) (high << 4 | low);
                j += 2;
            }
            FILE_HEADER_MAP.put((String) k, standHeaderBytes);
        });
    }

}
