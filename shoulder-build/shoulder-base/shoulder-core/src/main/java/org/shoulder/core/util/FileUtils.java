package org.shoulder.core.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.HexUtil;
import com.fasterxml.jackson.core.type.TypeReference;
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
 *
 * @author lym
 */
public class FileUtils extends FileUtil {

    private final static Logger log = LoggerFactory.getLogger(FileUtils.class);
    public static final String UPLOAD_FILE_ROOT_PATH = "upload";
    public static final String COMMA_SEPARATOR = ",";
    private static final String TEMP_DIR_NAME = "temp";
    private static final String FILE_SEPARATOR = File.separator;

    /**
     * 文件扩展名 - 数组
     */
    private static final Map<String, byte[]> FILE_HEADER_MAP = new ConcurrentHashMap<>();

    static {
        final String fileHeaderPath = "META-INF/file_header_allow_list.properties";
        try (InputStream inputStream = FileUtils.class.getClassLoader().getResourceAsStream(fileHeaderPath)) {
            readProperties(inputStream).forEach((k, v) -> addFileHeader((String) k, (String) v));
        } catch (IOException e) {
            log.warn("init file_header_allow_list from " + fileHeaderPath + "fail", e);
        }
    }


    /**
     * 获取文件的扩展名小写
     *
     * @param fileName 文件名 xxx.xxx
     * @return 若没有则返回空字符串
     */
    public static String getExtName(@Nonnull String fileName) {
        int pos = fileName.lastIndexOf(".");
        if (pos > -1) {
            return fileName.substring(pos + 1).toLowerCase();
        } else {
            return "";
        }
    }


    public static Properties readProperties(InputStream inputStream) throws IOException {
        Properties config = new Properties();
        config.load(inputStream);
        return config;
    }

    public static <T> T readJson(InputStream inputStream, TypeReference<T> type) {
        return JsonUtils.parseObject(inputStream, type);
    }

    // Xstream
    public static <T> T readXml(InputStream inputStream) {
        return null;
    }

    public static <T> T readYaml(InputStream inputStream) {
        throw new UnsupportedOperationException();
    }

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
     * @param file 上传文件
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
    public static boolean writeLines(OutputStream os, String charSet, List<String> dataList) {
        try {
            OutputStreamWriter osw = new OutputStreamWriter(os, charSet);
            BufferedWriter bfw = new BufferedWriter(osw);
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
    public static List<String> readLines(File file) {
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
    public static boolean checkHeader(@Nonnull InputStream in, @Nonnull String extName, boolean allowUnKnown) throws IOException {
        String lowExtName = extName.toLowerCase();
        byte[] standHeaderBytes = FILE_HEADER_MAP.get(lowExtName);
        if (standHeaderBytes != null) {
            int byteCount = standHeaderBytes.length;
            if (byteCount == 0) {
                // byte[0] 意味着无要求
                return true;
            }
            byte[] headBytes = in.readNBytes(byteCount);
            return Arrays.compare(headBytes, standHeaderBytes) == 0;
        } else {
            // 警告，不在里，未校验
            if (allowUnKnown) {
                log.warn("filExtName({}) unknown and skipped.", extName);
            }
            return allowUnKnown;
        }
    }

    public static Map<String, byte[]> getFileHeaders() {
        return FILE_HEADER_MAP;
    }

    public static void addFileHeader(@Nonnull String extName, @Nonnull String standHeaderHex) {
        log.debug("use stand file({}) with header({})", extName, standHeaderHex);
        if (standHeaderHex.startsWith("0x")) {
            standHeaderHex = standHeaderHex.replaceAll("0x", "");
        }
        FILE_HEADER_MAP.put(extName, HexUtil.decodeHex(standHeaderHex));
    }

    /**
     * 将文件大小的数字转换成可读的字符串(如1GB)
     *
     * @param size byte 大小
     * @return 可读字符串
     */
    public static String byteCountToDisplay(long size) {
        return org.apache.commons.io.FileUtils.byteCountToDisplaySize(size);
    }
}
