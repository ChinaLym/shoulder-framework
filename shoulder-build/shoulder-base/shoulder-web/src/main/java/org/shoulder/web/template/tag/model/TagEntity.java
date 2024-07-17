package org.shoulder.web.template.tag.model;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.shoulder.data.mybatis.template.entity.BizEntity;

import java.util.Map;

/**
 * 标签
 * <p>
 * 唯一索引 bizId - deleteVersion
 * 唯一索引 bizType - name
 *
 * name可能相同，但bizId是url友好的，方便 SEO，比如 name='富裕'  bizId可以是 ‘user_wealth_richer’
 *
 * @author lym
 */
@Getter
@Setter
@Accessors(chain = true)
@ToString(callSuper = true)
@TableName("tb_tag")
public class TagEntity extends BizEntity<Long> {

    /**
     * 业务类型
     */
    @TableField("biz_type")
    // @BizIdSource
    private String type;

    /**
     * 域
     */
    @TableField("tenant")
    // @BizIdSource
    private String tenant;

    /**
     * 名称
     */
    @TableField("name")
    // @BizIdSource
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
    private String displayName;

    /**
     * 描述
     */
    @TableField("display_order")
    private Integer displayOrder;

    /**
     * 来源
     */
    @TableField(value = "source", updateStrategy = FieldStrategy.NEVER)
    private String source;

    /**
     * 状态
     */
    @TableField("status")
    private String status;

    /**
     * 扩展字段
     */
    @TableField("ext")
    private Map<String, String> ext;

    // viewCount likeCount commentCount refCount
    @Override
    public String getObjectName() {
        return name;
    }

    @Override
    public String getObjectType() {
        // 这里如果过长，数据库保存时可能报错，强烈建议实现该接口
        return "objectType.shoulder.tag";
    }
}
