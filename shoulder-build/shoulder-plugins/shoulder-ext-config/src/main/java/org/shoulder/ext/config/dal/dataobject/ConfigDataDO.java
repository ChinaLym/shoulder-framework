package org.shoulder.ext.config.dal.dataobject;

import lombok.Data;

import java.util.Date;

/**
 * configData
 *
 * @author lym
 */
@Data
public class ConfigDataDO {

    private Long id;

    private String bizId;

    private Long deleteVersion;

    private Integer version;

    private String tenant;

    private String type;

    private String note;

    private String creator;

    private Date createTime;

    private String modifier;

    private Date modifyTime;

    private String businessValue;

}