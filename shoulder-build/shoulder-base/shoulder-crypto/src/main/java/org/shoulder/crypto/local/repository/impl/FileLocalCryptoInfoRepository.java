package org.shoulder.crypto.local.repository.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.collections4.CollectionUtils;
import org.shoulder.core.exception.BaseRuntimeException;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;
import org.shoulder.core.util.JsonUtils;
import org.shoulder.core.util.StringUtils;
import org.shoulder.crypto.local.entity.LocalCryptoMetaInfo;
import org.shoulder.crypto.local.repository.LocalCryptoInfoRepository;
import org.springframework.util.ClassUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 使用文件实现持久化 AES加密所需信息
 *
 * @author lym
 */
public class FileLocalCryptoInfoRepository implements LocalCryptoInfoRepository {

    private static final String DEFAULT_FILE_NAME = "_shoulder_aesInfo.json";
    private final Logger log = LoggerFactory.getLogger(getClass());
    /**
     * 存储文件字符集
     */
    private final Charset charset;
    /**
     * 文件存储路径
     */
    private String rootKeyInfoPath;


    public FileLocalCryptoInfoRepository(String rootKeyInfoPath, Charset charset) {
        this.rootKeyInfoPath = StringUtils.isEmpty(rootKeyInfoPath) ? getDefaultFilePath() : rootKeyInfoPath;
        this.charset = charset;
    }

    private String getDefaultFilePath() {
        String defaultPath = ClassUtils.getDefaultClassLoader().getResource("").getPath() + DEFAULT_FILE_NAME;
        log.warn("no available localCrypto.MetaInfoFilePath config config, use default: {}. ", defaultPath);
        // windows 系统下，去掉 '/c:/xx' 前缀，改为 'c:/xx'，避免 Path.class 不支持的情况
        return new File(defaultPath).getAbsolutePath();
    }

    @Override
    public synchronized void save(@Nonnull LocalCryptoMetaInfo localCryptoMetaInfo) throws IOException {
        // 先取出所有
        List<LocalCryptoMetaInfo> allExists = getAll();
        if (CollectionUtils.isNotEmpty(allExists)) {
            boolean add = true;
            // 如果存在则更新
            for (int i = 0; i < allExists.size(); i++) {
                LocalCryptoMetaInfo existsMetaInfo = allExists.get(i);
                if (existsMetaInfo.getAppId().equals(localCryptoMetaInfo.getAppId()) && existsMetaInfo.getHeader().equals(localCryptoMetaInfo.getHeader())) {
                    allExists.set(i, localCryptoMetaInfo);
                    add = false;
                    break;
                }
            }
            // 不存在则添加
            if (add) {
                allExists.add(localCryptoMetaInfo);
            }
        } else {
            allExists = Collections.singletonList(localCryptoMetaInfo);
        }
        // 整体保存
        String jsonStr = JsonUtils.toJson(allExists);
        Files.write(getFilePath(), jsonStr.getBytes(charset));
    }

    @Override
    public LocalCryptoMetaInfo get(@Nonnull String appId, @Nonnull String markHeader) {
        return getAll().stream()
            .filter(info -> appId.equals(info.getAppId()) && markHeader.equals(info.getHeader()))
            .findFirst().orElse(null);
    }

    @Override
    @Nonnull
    public List<LocalCryptoMetaInfo> get(@Nonnull String appId) {
        return getAll().stream()
            .filter(info -> appId.equals(info.getAppId()))
            .collect(Collectors.toList());
    }

    private Path getFilePath() throws IOException {
        Path fileLocation = Paths.get(rootKeyInfoPath);
        if (Files.notExists(fileLocation)) {
            synchronized (this) {
                if (Files.notExists(fileLocation)) {
                    Files.createFile(fileLocation);
                }
            }
        }
        return fileLocation;
    }

    private List<LocalCryptoMetaInfo> getAll() {
        try {
            Path path = getFilePath();
            String jsonStr = Files.readString(path, charset);
            if (StringUtils.isBlank(jsonStr)) {
                return Collections.emptyList();
            }
            return JsonUtils.toObject(jsonStr, new TypeReference<>() {
            });
        } catch (IOException e) {
            // 大概率文件路径有问题，不可读
            throw new BaseRuntimeException(e);
        }
    }


}
