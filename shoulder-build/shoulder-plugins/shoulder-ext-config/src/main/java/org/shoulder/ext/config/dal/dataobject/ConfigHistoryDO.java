package org.shoulder.ext.config.dal.dataobject;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.shoulder.data.constant.DataBaseConsts;

import java.time.Instant;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "iexpmng_config_history")
public class ConfigHistoryDO {

    /**
     * 主键
     */
    @TableId(value = DataBaseConsts.COLUMN_ID, type = IdType.ASSIGN_ID)
    protected Long id;

    /**
     * 创建时间
     */
    @TableField(value = DataBaseConsts.COLUMN_CREATE_TIME, fill = FieldFill.INSERT, updateStrategy = FieldStrategy.NEVER)
    protected Instant gmtCreate;

    /**
     * 修改时间
     */
    @TableField(value = DataBaseConsts.COLUMN_UPDATE_TIME, fill = FieldFill.INSERT_UPDATE)
    protected Instant gmtModified;

    /**
     * 创建者
     */
    @TableField(value = DataBaseConsts.COLUMN_CREATOR, updateStrategy = FieldStrategy.NEVER)
    private String creator;

    /**
     * 创建者
     */
    //@TableField(value = DataBaseConsts.COLUMN_CREATOR_NAME, updateStrategy = FieldStrategy.NEVER)
    //private String creatorName;

    /**
     * 修改者
     */
    @TableField(value = DataBaseConsts.COLUMN_MODIFIER,fill = FieldFill.INSERT_UPDATE)
    private String modifier;

    /**
     * 修改者
     */
    //@TableField(value = DataBaseConsts.COLUMN_MODIFIER_NAME,fill = FieldFill.INSERT_UPDATE)
    //private String modifierName;

    @TableLogic
    private int deleteFlag;

    /**
     * 配置表主键
     */
    @TableField(value = "config_biz_id")
    private String configBizId;

    /**
     * 操作前这条记录的版本号
     */
    @TableField(value = "version")
    private int version;

    /**
     * 操作类型，这条记录被如何改动进入的下个版本：UPDATE DELETE
     */
    @TableField(value = "operation")
    private String operation;

    /**
     * 原业务数据内容，json格式
     */
    @TableField(value = "business_value")
    private String businessValue;

}
