package org.shoulder.data.mybatis.template.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 树形 biz 实体： id createTime updateTime creator modifier bizId version deleteVersion name parentId sortNo
 * 举例：组织类、位置类（）
 * 第一个泛型为 id 类型，第二个泛型通常为自身类型
 *
 * @author lym
 */
@Getter
@Setter
@Accessors(chain = true)
public class BizTreeEntity<ID extends Serializable> extends TreeEntity<ID> implements ILogicDeleteEntity {

    /**
     * 业务唯一索引键
     */
    @TableField("biz_id")
    private String bizId;

    /**
     * 版本号
     */
    @Version
    private int version;

    /**
     * 删除标记
     * 0 未删除
     * n 删除时设置为当前时间戳
     */
    @TableField("delete_version")
    @TableLogic(value = "0")
    private Long deleteVersion;

    /**
     * 描述
     */
    @TableField("description")
    private String description;

}
