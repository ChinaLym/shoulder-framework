package org.shoulder.data.uid;

import org.apache.ibatis.reflection.MetaObject;

/**
 * guid
 *
 * @author lym
 */
public interface EntityIdGenerator {

    /**
     * 生产下一个 id
     *
     * @param metaObject entity 引用
     * @param idType     id 类型
     * @return id
     */
    Object genId(MetaObject metaObject, Class<?> idType);

}
