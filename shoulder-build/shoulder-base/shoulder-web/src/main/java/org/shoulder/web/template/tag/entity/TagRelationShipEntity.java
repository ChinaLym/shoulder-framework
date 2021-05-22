package org.shoulder.web.template.tag.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 标签关系
 * <p>
 * 建立索引时使用 biz_type - ref_id - tag_biz_id - deleteVersion
 *
 * @author lym
 * @see TagSearchEntity 与 tagRef 不同的是，这里是关系表
 */
@Getter
@Setter
@Accessors(chain = true)
@ToString(callSuper = true)
//@TableName("tb_tag_relation_ship")
public class TagRelationShipEntity extends TagSearchEntity {

    /**
     * 业务类型
     */
    @TableField("biz_type")
    private String bizType;

}