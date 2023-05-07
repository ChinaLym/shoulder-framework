package org.shoulder.web.template.tag.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.shoulder.data.mybatis.template.entity.LogicDeleteEntity;

import java.util.Set;

/**
 * 标签查询，由于标签是用于搜索，因此采取倒排索引的方式
 * 注意：是倒排索引表，而不是关系表！本表仅为根据标签搜索提效！需要业务表中存放 text 字段存储加的标签，或其他方式存储!
 * <p>
 * tagId -> [xx1,xx3,xx8,xx12] 在 SQL 中表示为 biz_type - id - n bizId
 * 建立索引时 tag_biz_id - ref_id - deleteVersion
 *
 * @author lym
 */
@Getter
@Setter
@Accessors(chain = true)
@ToString(callSuper = true)
//@TableName("tb_tag_search")
public class TagSearchEntity extends LogicDeleteEntity<Long> {

    /**
     * tag表id
     */
    @TableField(value = "tag_id")
    private Long tagId;

    /**
     * ref 是什么类型，比如是某个表的id
     */
    @TableField(value = "ref_type")//typeHandler = JacksonTypeHandler.class
    private String refType;

    /**
     * 外部业务表 标识 JSONArray
     */
    @TableField("ref_ids")
    private Set<Long> refIds;

}