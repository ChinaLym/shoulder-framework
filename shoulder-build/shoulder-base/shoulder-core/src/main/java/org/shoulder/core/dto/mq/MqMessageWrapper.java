package org.shoulder.core.dto.mq;

import lombok.Data;

import java.util.Map;

/**
 * mq 消息统一包装类
 *
 * @author lym
 */
@Data
public class MqMessageWrapper {

    /**
     * 消息类型
     */
    private String type;

    /**
     * 消息唯一标识
     */
    private String id;

    /**
     * 哪个服务发出的
     */
    private String from;

    /**
     * 真正的 msg json 化的部分，建议对应数据添加 version 以应对多版本服务共存
     */
    private String data;

    /**
     * 扩展字段，供特殊需求
     * 如分布式系统中希望获取各个消息产生顺序以及因果关系，要通过时钟向量分析，需要额外保存一些字段
     * 多版本服务共存的系统，可能还要包含发出者版本号，以方便下游业务感知，进行处理
     */
    private Map<String, String> extend;

}
