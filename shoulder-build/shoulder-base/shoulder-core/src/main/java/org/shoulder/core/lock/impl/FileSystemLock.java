package org.shoulder.core.lock.impl;

import org.shoulder.core.context.AppInfo;
import org.shoulder.core.lock.LockInfo;
import org.shoulder.core.util.JsonUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * 通过文件系统实现
 *
 * @author lym
 */
public class FileSystemLock {

    private static final String OS = System.getProperty("os.name").toLowerCase();

    private static final Path LOCK_FILE = initLockFilePath();


    public synchronized void save(LockInfo lockInfo) throws Exception {
        List<LockInfo> lockInfoList = getAll();
        lockInfoList.add(lockInfo);
        String jsonStr = JsonUtils.toJson(lockInfoList);
        Files.write(LOCK_FILE, jsonStr.getBytes(AppInfo.charset()));
    }

    @SuppressWarnings("unchecked")
    private List<LockInfo> getAll() throws IOException {
        String jsonStr = Files.readString(LOCK_FILE, AppInfo.charset());
        return JsonUtils.parseObject(jsonStr, List.class, LockInfo.class);
    }

    public synchronized LockInfo get(String resource) throws Exception {
        List<LockInfo> lockInfoList = getAll();
        for (LockInfo lockInfo : lockInfoList) {
            if (lockInfo.getResource().equals(resource)) {
                return lockInfo;
            }
        }
        return null;
    }


    public static Path initLockFilePath() {
        // 这里依赖了用户家目录，所以互斥、隔离性的范围仅限于当前用户
        String userHome = System.getProperty("user.home");
        String filePathStr = userHome + File.separator + ".shoulder";
        Path fileLocation = Paths.get(filePathStr);
        if (Files.notExists(fileLocation)) {
            try {
                Files.createDirectories(fileLocation);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return fileLocation.resolve("server.lock");
    }

}
