package org.shoulder.crypto.local.repository.impl;

import org.shoulder.core.util.JsonUtils;
import org.shoulder.crypto.local.entity.LocalCryptoInfoEntity;
import org.shoulder.crypto.local.repository.LocalCryptoInfoRepository;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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

    private final Charset utf8 = StandardCharsets.UTF_8;

	public String getAesInfoPath() {
		return aesInfoPath;
	}

	public void setAesInfoPath(String aesInfoPath) {
		this.aesInfoPath = aesInfoPath;
	}

	/** 文件存储路径 */
	private String aesInfoPath;

	public FileLocalCryptoInfoRepository(String aesInfoPath){
		this.aesInfoPath = aesInfoPath;
	}

	@Override
	public void save(@NonNull LocalCryptoInfoEntity aesInfo) throws Exception {
        String jsonStr = JsonUtils.toJson(aesInfo);
		Files.write(getFilePath(), jsonStr.getBytes(utf8));
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
		if(Files.notExists(fileLocation)){
			Files.createDirectories(fileLocation);
		}
		String fileName = "aesInfo.json";
		return fileLocation.resolve(fileName);
	}

	public List<LocalCryptoInfoEntity> getAll() throws Exception {
		String jsonStr = Files.readString(getFilePath(), utf8);
		return (List<LocalCryptoInfoEntity>) JsonUtils.toObject(jsonStr, List.class);
	}


}
