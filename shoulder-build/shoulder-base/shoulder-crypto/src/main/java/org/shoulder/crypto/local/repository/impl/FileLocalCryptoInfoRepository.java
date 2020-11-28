package org.shoulder.crypto.local.repository.impl;

import org.shoulder.core.context.AppInfo;
import org.shoulder.core.exception.BaseRuntimeException;
import org.shoulder.core.util.JsonUtils;
import org.shoulder.crypto.local.entity.LocalCryptoInfoEntity;
import org.shoulder.crypto.local.repository.LocalCryptoInfoRepository;
import org.springframework.util.ClassUtils;

import javax.annotation.Nonnull;
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
    public void save(@Nonnull LocalCryptoInfoEntity aesInfo) throws IOException {
        String jsonStr = JsonUtils.toJson(aesInfo);
        Files.write(getFilePath(), jsonStr.getBytes(charset));
    }

    @Override
    public LocalCryptoInfoEntity get(String appId, String markHeader) {
        return getAll().stream()
            .filter(info -> appId.equals(info.getAppId()) && markHeader.equals(info.getHeader()))
            .findFirst().orElse(null);
    }

    @Override
    @Nonnull
    public List<LocalCryptoInfoEntity> get(String appId) {
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
    public List<LocalCryptoInfoEntity> getAll() {
        try {
            String jsonStr = Files.readString(getFilePath(), charset);
            return (List<LocalCryptoInfoEntity>) JsonUtils.toObject(jsonStr, List.class, LocalCryptoInfoEntity.class);
        } catch (IOException e) {
            // 大概率文件路径有问题，不可读
            throw new BaseRuntimeException(e);
        }
    }


}
