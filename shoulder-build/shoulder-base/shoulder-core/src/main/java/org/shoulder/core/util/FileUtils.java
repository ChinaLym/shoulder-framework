package org.shoulder.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

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

}
