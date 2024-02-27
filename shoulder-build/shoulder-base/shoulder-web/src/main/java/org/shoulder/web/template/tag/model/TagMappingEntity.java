package org.shoulder.web.template.tag.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.shoulder.data.mybatis.template.entity.LogicDeleteEntity;

/**
 * 标签映射
 * 建立索引时使用 biz_type - ref_id - tag_biz_id - deleteVersion
 *
 * tagId -> [xx1,xx3,xx8,xx12] 在 SQL 中表示为 biz_type - id - n bizId
 * 建立索引时 tag_biz_id - ref_id - deleteVersion
 *
 * @author lym
 */
@Getter
@Setter
@Accessors(chain = true)
@ToString(callSuper = true)
@TableName("tb_tag_mapping")
public class TagMappingEntity extends LogicDeleteEntity<Long> {

    /**
     * tag表id
     */
    @TableField(value = "tag_id")
    private Long tagId;

    /**
     * ref 是什么类型，比如 User、Post、Photo
     */
    @TableField(value = "ref_type")//typeHandler = JacksonTypeHandler.class
    private String refType;

    /**
     * 业务类型
     */
//    @TableField("biz_type")
//    private String bizType;

    /**
     * 外部业务表 标识 JSONArray
     */
    //@TableField("ref_ids")
    //private Set<Long> refIds;

    /**
     * 关联 Object id
     */
    @TableField("oid")
    private String oid;

}
