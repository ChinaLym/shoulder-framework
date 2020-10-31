package org.shoulder.batch.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 导入记录
 *
 * @author lym
 */
@Data
@NoArgsConstructor
public class ImportRecord implements Serializable {

    /**
     * 主键
     */
    private Integer id;

    /**
     * 导入数据类型，建议可翻译。对应 导入数据库表名
     */
    private String dataType;

    /**
     * 导入总条数
     */
    private Integer totalNum;

    /**
     * 成功条数
     */
    private Integer successNum;

    /**
     * 失败条数
     */
    private Integer failNum;

    /**
     * 执行导入的用户
     */
    private Long creator;

    /**
     * 导入时间
     */
    private Date createTime;

}
