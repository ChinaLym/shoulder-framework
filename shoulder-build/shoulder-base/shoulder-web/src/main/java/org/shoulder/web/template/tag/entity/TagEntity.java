package org.shoulder.web.template.tag.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.shoulder.data.mybatis.template.entity.BizEntity;

/**
 * 标签
 * <p>
 * 唯一索引 bizId - deleteVersion
 * 唯一索引 bizType - name
 *
 * @author lym
 */
@Getter
@Setter
@Accessors(chain = true)
@ToString(callSuper = true)
//@TableName("tb_tag")
public class TagEntity extends BizEntity<Long> {

    /**
     * 业务类型
     */
    @TableField("biz_type")
    private String bizType;

    /**
     * 名称
     */
    @TableField("name")
    private String name;

    /**
     * 图标
     */
    @TableField("icon")
    private String icon;

    /**
     * display 名称
     */
    @TableField("display_name")
    private Long displayName;

    /**
     * 描述
     */
    @TableField("description")
    private String description;

    // viewCount likeCount commentCount refCount

}
