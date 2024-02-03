package org.shoulder.data.mybatis.template.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.shoulder.data.annotation.BizIdSource;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 可变记录型实体: 带有 id、创建时间、最后修改时间、创建者、最后修改者
 * <p>
 * 索引：biz_id - delete_version - version （加 version 主要是为了 lock for update 时候避免锁表）
 * <p>
 * 实现类必须有字段标注{@link BizIdSource}供框架自行生成 {@link BizEntity#bizId}，或创建时自行填写，否则创建时失败
 *
 * @author lym
 */
@Getter
@Setter
@Accessors(chain = true)
public class BizEntity<ID extends Serializable>
        extends LogicDeleteEntity<ID> {

    /**
     * 业务唯一索引键
     * 是什么：bizId 往往具有特定的业务逻辑含义，如订单号、用户账号、项目编号等。这对用户看到、业务人员理解和操作数据非常直观和便捷。
     * 跨系统集成：在分布式或微服务架构中，与具体数据库无关且能跨越多个系统
     * 外部系统关联：在与其他第三方系统进行数据交换使用：业务单据、发票、合同等相关联，具有法律效力或对外可见性。
     * 预定义规则：可按照特定格式或规则生成，比如按时间序列、地区信息、特定算法等组合而成
     * 无序增长和不可预测性：id 的往往连续递增的，易暴露业务量大小或记录创建顺序等敏感信息。而 bizId 可设计为不连续或不可预测的，有助于保护一定的业务隐私。
     */
    @TableField("biz_id")
    private String bizId;

    /**
     * 版本号
     */
    @Version
    @TableField(value = "version", fill = FieldFill.INSERT)
    private Integer version;

    /**
     * 备注：这条记录的说明，通常用于辅助解释这条数据的用处或注意点
     * note: 注意某个物
     * remark: 评价某物
     * description: 更通用，详细的说明（detail），解释某物
     * info: 泛指信息
     */
    @TableField("description")
    private String description;

    public BizEntity() {
    }

    public BizEntity(ID id, LocalDateTime createTime, LocalDateTime updateTime, Long createUser, Long modifier) {
        super(id, createTime, updateTime, createUser, modifier);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    // 用于扩展
    public String generateBizId() {
        return null;
    }

    @Override
    public String getObjectId() {
        return bizId;
    }
}
