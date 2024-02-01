package org.shoulder.data.mybatis.config.handler;

import cn.hutool.core.util.ReflectUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import jakarta.annotation.Nonnull;
import org.apache.ibatis.reflection.MetaObject;
import org.shoulder.core.context.AppContext;
import org.shoulder.core.util.StringUtils;
import org.shoulder.data.constant.DataBaseConsts;
import org.shoulder.data.mybatis.template.entity.BaseEntity;
import org.shoulder.data.uid.EntityIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.GenericTypeResolver;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 当基础字段为空时（创建时间、更新时间等）自动填充
 * <p>
 * 1. insert 时填充 id, createTime, updateTime, createdBy, updatedBy
 * 2. update 时填充 updateTime, updatedBy
 * todo 【优化-兼容性】createTime update 时区问题：数据库采取的是 0 时区，则 NOW() 返回值和 java 的 LocalDateTime.now() 是不一样的，换成Instant
 * <p>
 * 值来源：
 * id： {@link EntityIdGenerator#next(String, String)}
 * createTime updateTime：{@link LocalDateTime#now}
 * createdBy updatedBy：{@link AppContext#getUserId}
 *
 * @author lym
 */
public class ModelMetaObjectHandler implements MetaObjectHandler {

    @Autowired(required = false)
    private EntityIdGenerator entityIdGenerator;

    /**
     * 插入时添加创建时间
     *
     * @param metaObject 页面传递过来的参数的包装对象
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        fillDateAndModifier(metaObject, DataBaseConsts.FIELD_CREATE_TIME, DataBaseConsts.FIELD_CREATOR);
        fillDateAndModifier(metaObject, DataBaseConsts.FIELD_UPDATE_TIME, DataBaseConsts.FIELD_MODIFIER);
        fillVersion(metaObject, DataBaseConsts.FIELD_VERSION, DataBaseConsts.FIELD_DELETE_VERSION);
        fillId(metaObject);
    }

    private void fillVersion(MetaObject metaObject, String fieldVersion, String fieldDeleteVersion) {
        Object dataVersion = this.getFieldValByName(fieldVersion, metaObject);
        if (dataVersion == null) {
            this.setFieldValByName(fieldVersion, 0, metaObject);
        }
        Object dataDeleteVersion = this.getFieldValByName(fieldDeleteVersion, metaObject);
        if (dataDeleteVersion == null) {
            this.setFieldValByName(fieldDeleteVersion, 0L, metaObject);
        }
    }

    /**
     * 更新时添加更新时间，修改人
     *
     * @param metaObject 页面传递过来的参数的包装对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        fillDateAndModifier(metaObject, DataBaseConsts.FIELD_UPDATE_TIME, DataBaseConsts.FIELD_MODIFIER);
    }

    /**
     * 创建者，创建时间
     * 修改者，修改时间
     */
    private void fillDateAndModifier(MetaObject metaObject, String timeField, String userField) {
        Object timeFieldValue = this.getFieldValByName(timeField, metaObject);
        if (timeFieldValue == null) {
            this.setFieldValByName(timeField, LocalDateTime.now(), metaObject);
        }
        String currentUserIdStr = AppContext.getUserId();
        if (StringUtils.isNotEmpty(currentUserIdStr)) {
            Long currentUserId = Long.valueOf(currentUserIdStr);
            Object modifier = this.getFieldValByName(userField, metaObject);
            if (modifier == null) {
                this.setFieldValByName(userField, currentUserId, metaObject);
            }
        }
    }

    /**
     * 填充 id 值
     *
     * @param metaObject obj
     */
    private void fillId(MetaObject metaObject) {
        String idFieldName = getIdFieldName(metaObject);
        Object oldId = getFieldValByName(idFieldName, metaObject);
        if (oldId != null) {
            // id 有值，无需框架赋值
            return;
        }
        // 否则生成并 set
        Class<?> actuallyClass = metaObject.getOriginalObject().getClass();
        Class<?> idType = null;
        if(BaseEntity.class.isAssignableFrom(actuallyClass)) {
            idType = Optional.ofNullable(GenericTypeResolver.resolveTypeArguments(actuallyClass, BaseEntity.class)).orElseThrow()[0];
        } else {
            idType = ReflectUtil.getField(actuallyClass, idFieldName).getType();
        }

        Object newId = entityIdGenerator.genId(metaObject, idType);
        setFieldValByName(idFieldName, newId, metaObject);
    }

    @Nonnull
    protected String getIdFieldName(MetaObject metaObject) {
        // 有 fieldName 为 id 的字段
        if (metaObject.getOriginalObject() instanceof BaseEntity || metaObject.hasGetter(DataBaseConsts.FIELD_ID)) {
            return "id";
        }

        // 3. 实体没有继承 Entity 和 BaseEntity，且 主键名为其他字段
        TableInfo tableInfo = TableInfoHelper.getTableInfo(metaObject.getOriginalObject().getClass());
        if (tableInfo == null || tableInfo.getKeyProperty() == null) {
            throw new IllegalArgumentException("tableInfo == null. obj=" + metaObject);
        }
        return tableInfo.getKeyProperty();
    }

}
