package org.shoulder.crypto.local.repository.impl;

import org.shoulder.core.context.AppInfo;
import org.shoulder.core.util.JsonUtils;
import org.shoulder.crypto.local.entity.LocalCryptoInfoEntity;
import org.shoulder.crypto.local.repository.LocalCryptoInfoRepository;
import org.springframework.lang.NonNull;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 使用文件实现持久化 AES加密所需信息
 *
 * @author lym
 */
public class FileLocalCryptoInfoRepository implements LocalCryptoInfoRepository {

    public static final String DEFAULT_FILE_NAME = "_shoulder_aesInfo.json";
    /**
     * 存储文件名称
     */
    private final String fileName;
    /**
     * 存储文件字符集
     */
    private final Charset charset;
    /**
     * 文件存储路径
     */
    private String aesInfoPath;

    public FileLocalCryptoInfoRepository() {
        this(
            ClassUtils.getDefaultClassLoader().getResource("").getPath(),
            DEFAULT_FILE_NAME,
            AppInfo.charset()
        );
    }

    public FileLocalCryptoInfoRepository(String aesInfoPath, String fileName, Charset charset) {
        this.aesInfoPath = aesInfoPath;
        this.fileName = fileName;
        this.charset = charset;
    }

    @Override
    public void save(@NonNull LocalCryptoInfoEntity aesInfo) throws Exception {
        String jsonStr = JsonUtils.toJson(aesInfo);
        Files.write(getFilePath(), jsonStr.getBytes(charset));
    }

    @Override
    public LocalCryptoInfoEntity get(String appId, String markHeader) throws Exception {
        return getAll().stream()
            .filter(info -> appId.equals(info.getAppId()) && markHeader.equals(info.getHeader()))
            .findFirst().orElse(null);
    }

    @Override
    @NonNull
    public List<LocalCryptoInfoEntity> get(String appId) throws Exception {
        return getAll().stream().filter(info -> appId.equals(info.getAppId())).collect(Collectors.toList());
    }

    private Path getFilePath() throws IOException {
        Path fileLocation = Paths.get(aesInfoPath);
        if (Files.notExists(fileLocation)) {
            Files.createDirectories(fileLocation);
        }
        return fileLocation.resolve(fileName);
    }

    @SuppressWarnings("unchecked")
    public List<LocalCryptoInfoEntity> getAll() throws Exception {
        String jsonStr = Files.readString(getFilePath(), charset);
        return (List<LocalCryptoInfoEntity>) JsonUtils.toObject(jsonStr, List.class, LocalCryptoInfoEntity.class);
    }


}
