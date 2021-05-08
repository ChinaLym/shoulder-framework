package org.shoulder.data.mybatis.config.handler;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.ReflectUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.reflection.MetaObject;
import org.shoulder.core.context.AppContext;
import org.shoulder.core.util.StringUtils;
import org.shoulder.data.constant.DataBaseConsts;
import org.shoulder.data.mybatis.template.entity.BaseEntity;
import org.shoulder.data.uid.UidGenerator;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

/**
 * 当基础字段为空时（创建时间、更新时间等）自动填充
 * <p>
 * 1. insert 时填充 id, createTime, updateTime, createdBy, updatedBy
 * 2. update 时填充 updateTime, updatedBy
 * <p>
 * 值来源：
 * id： {@link UidGenerator#next(String, String)}
 * createTime updateTime：{@link LocalDateTime#now}
 * createdBy updatedBy：{@link AppContext#getUserId}
 *
 * @author lym
 */
public class ModelMetaObjectHandler implements MetaObjectHandler {

    @Autowired(required = false)
    private UidGenerator uidGenerator;

    /**
     * 插入时添加创建时间
     *
     * @param metaObject 页面传递过来的参数的包装对象
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        fillDateAndModifier(metaObject, DataBaseConsts.FIELD_CREATE_TIME, DataBaseConsts.FIELD_CREATOR);
        fillId(metaObject);
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
     * 修改者，修改时间
     */
    private void fillDateAndModifier(MetaObject metaObject, String timeField, String userField) {
        Object updateTime = this.getFieldValByName(timeField, metaObject);
        if (updateTime == null) {
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


    private void fillId(MetaObject metaObject) {
        final String StringTypeName = "java.lang.String";
        if (uidGenerator == null) {
            // 这里使用SpringUtils的方式"延迟"获取对象，防止启动时，报循环注入的错
            //uidGenerator = SpringUtils.getBean(UidGenerator.class);
        }
        //1. 继承了BaseEntity 若 ID 中有值，就不设置
        if (metaObject.getOriginalObject() instanceof BaseEntity) {
            Object oldId = ((BaseEntity) metaObject.getOriginalObject()).getId();
            if (oldId != null) {
                return;
            }
            Long id = genUid();
            Object idVal = StringTypeName.equals(metaObject.getGetterType(DataBaseConsts.FIELD_ID).getName()) ? String.valueOf(id) : id;
            this.setFieldValByName(DataBaseConsts.FIELD_ID, idVal, metaObject);
            return;
        }

        // 2. 没有继承BaseEntity， 但主键的字段名为：  id
        if (metaObject.hasGetter(DataBaseConsts.FIELD_ID)) {
            Object oldId = metaObject.getValue(DataBaseConsts.FIELD_ID);
            if (oldId != null) {
                return;
            }
            Long id = genUid();
            Object idVal = StringTypeName.equals(metaObject.getGetterType(DataBaseConsts.FIELD_ID).getName()) ? String.valueOf(id) : id;
            this.setFieldValByName(DataBaseConsts.FIELD_ID, idVal, metaObject);
            return;
        }

        // 3. 实体没有继承 Entity 和 BaseEntity，且 主键名为其他字段
        TableInfo tableInfo = metaObject.hasGetter(Constants.MP_OPTLOCK_ET_ORIGINAL) ?
                TableInfoHelper.getTableInfo(metaObject.getValue(Constants.MP_OPTLOCK_ET_ORIGINAL).getClass())
                : TableInfoHelper.getTableInfo(metaObject.getOriginalObject().getClass());
        if (tableInfo == null) {
            // todo warn
            return;
        }
        // 主键类型
        Class<?> keyType = tableInfo.getKeyType();
        if (keyType == null) {
            return;
        }
        // id 字段名
        String keyProperty = tableInfo.getKeyProperty();
        Object oldId = metaObject.getValue(keyProperty);
        if (oldId != null) {
            return;
        }

        // 反射得到 主键的值
        Field idField = ReflectUtil.getField(metaObject.getOriginalObject().getClass(), keyProperty);
        Object fieldValue = ReflectUtil.getFieldValue(metaObject.getOriginalObject(), idField);
        // 判断ID 是否有值，有值就不
        if (ObjectUtil.isNotEmpty(fieldValue)) {
            return;
        }
        Long id = genUid();
        Object idVal = StringTypeName.equalsIgnoreCase(keyType.getName()) ? String.valueOf(id) : id;
        this.setFieldValByName(keyProperty, idVal, metaObject);
    }

    protected Long genUid() {
        // Long id = uidGenerator.getUid();
        // todo
        return RandomUtil.randomLong();
    }

}
