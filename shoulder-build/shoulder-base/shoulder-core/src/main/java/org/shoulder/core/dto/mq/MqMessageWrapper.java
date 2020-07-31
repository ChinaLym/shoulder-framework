package org.shoulder.core.dto.mq;

import lombok.Data;

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


}
