package org.shoulder.crypto.local.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * AES加解密相关密钥对存储
 *
 * @author lym
 */
public class LocalCryptoInfoEntity implements Serializable {

    /**
     * 应用标识
     */
    private String appId;

    /**
     * 密文头部标识信息，方便识别是否为该算法加密的密文，更新加密算法等
     */
    private String header;

    /**
     * 数据密钥
     */
    private String dataKey;

    /**
     * 跟密钥部件，用于生成根密钥
     */
    private String rootKeyPart;

    /**
     * 加密向量
     */
    private String vector;

    /**
     * 创建时间
     */
    private Date createTime;

    public LocalCryptoInfoEntity() {
    }

    public LocalCryptoInfoEntity(String appId, String dataKey, String rootKeyPart, String vector,
                                 String header, Date createTime) {
        this.appId = appId;
        this.dataKey = dataKey;
        this.rootKeyPart = rootKeyPart;
        this.vector = vector;
        this.header = header;
        this.createTime = createTime;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getDataKey() {
        return dataKey;
    }

    public void setDataKey(String dataKey) {
        this.dataKey = dataKey;
    }

    public String getRootKeyPart() {
        return rootKeyPart;
    }

    public void setRootKeyPart(String rootKeyPart) {
        this.rootKeyPart = rootKeyPart;
    }

    public String getVector() {
        return vector;
    }

    public void setVector(String vector) {
        this.vector = vector;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }
}

