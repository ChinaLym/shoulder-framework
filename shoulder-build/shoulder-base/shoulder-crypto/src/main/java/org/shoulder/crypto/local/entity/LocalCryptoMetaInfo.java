package org.shoulder.crypto.local.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 本地存储加解密-元信息，包含根密钥等
 *
 * @author lym
 */
@Data
public class LocalCryptoMetaInfo implements Serializable {

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

}

