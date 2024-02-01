package org.shoulder.data.constant;

/**
 * 数据库列名
 *
 * @author lym
 */
public interface DataBaseConsts {

    /**
     * 表名前缀
     */
    String TABLE_PREFIX = "tb_";

    // ------------------------ 列名 ------------------------

    /**
     * 主键
     */
    String COLUMN_ID = "id";

    /**
     * 创建时间
     */
    String COLUMN_CREATE_TIME = "create_time";

    /**
     * 最后一次修改时间
     * alter 修改表结构
     * update 表示修改表内容
     * modify 表示修改字段格式
     */
    String COLUMN_UPDATE_TIME = "update_time";

    /**
     * 创建者
     */
    String COLUMN_CREATOR = "creator";

    /**
     * 最后一次修改者
     */
    String COLUMN_MODIFIER = "modifier";

    /**
     * 标签名
     */
    String COLUMN_LABEL = "name";

    /**
     * 父级标识
     */
    String COLUMN_PARENT_ID = "parent_id";

    /**
     * 父级标识
     */
    String COLUMN_DEPTH = "depth";

    /**
     * 排序序号
     */
    String COLUMN_SORT_NO = "sort_no";


    // ------------------ 常用的数据源名称 ------------------

    /**
     * 主数据库
     */
    String DB_LEADER = "leader";

    /**
     * 从数据库
     */
    String DB_FOLLOWER = "follower";

    /**
     * 只读数据库
     */
    String DB_READ = "read";

    /**
     * 读写数据库
     */
    String DB_WRITE = "write";


    // ------------------------ Mapper 中的扩展方法名 ------------------------


    /**
     * 根据 bizIds 批量查询
     */
    String METHOD_SELECT_BY_BIZ_ID = "selectByBizId";

    /**
     * 根据 bizIds 批量查询
     */
    String METHOD_SELECT_BATCH_BY_BIZ_IDS = "selectBatchByBizIds";


    /**
     * 根据 id 锁定，悲观锁
     */
    String METHOD_SELECT_FOR_UPDATE_BY_ID = "selectForUpdateById";

    /**
     * 根据 id 锁定，悲观锁
     */
    String METHOD_SELECT_BATCH_FOR_UPDATE_BY_IDS = "selectBatchForUpdateByIds";

    /**
     * 根据 bizId 锁定，悲观锁
     */
    String METHOD_SELECT_FOR_UPDATE_BY_BIZ_ID = "selectForUpdateByBizId";

    /**
     * 根据 bizId 锁定，悲观锁
     */
    String METHOD_SELECT_BATCH_FOR_UPDATE_BY_BIZ_IDS = "selectBatchForUpdateByBizIds";


    /**
     * 批量插入
     */
    String METHOD_INSERT_BATCH = "insertBatch";

    /**
     * 根据 bizId 更新
     */
    String METHOD_UPDATE_BY_BIZ_ID = "updateByBizId";


    /**
     * 根据 id 更新所有字段
     */
    String METHOD_UPDATE_ALL_FIELDS_BY_ID = "updateAllFieldsById";
    /**
     * 根据 bizId 更新所有字段
     */
    String METHOD_UPDATE_ALL_FIELDS_BY_BIZ_ID = "updateAllByBizId";


    /**
     * 逻辑删除方法名
     */
    String METHOD_DELETE_LOGIC_BY_ID = "deleteInLogicById";

    /**
     * 逻辑删除方法名
     */
    String METHOD_DELETE_LOGIC_BY_ID_LIST = "deleteInLogicByIdList";

    /**
     * 逻辑删除方法名
     */
    String METHOD_DELETE_LOGIC_BY_BIZ_ID = "deleteInLogicByBizId";

    /**
     * 逻辑删除方法名
     */
    String METHOD_DELETE_LOGIC_BY_BIZ_ID_LIST = "deleteInLogicByBizIdList";

    // ------------------------ 字段名 ------------------------

    /**
     * 主键
     */
    String FIELD_ID = "id";

    /**
     * 创建时间
     */
    String FIELD_CREATE_TIME = "createTime";

    /**
     * 最后一次修改时间
     */
    String FIELD_UPDATE_TIME = "updateTime";

    /**
     * 创建者
     */
    String FIELD_CREATOR = "creator";

    /**
     * 最后一次修改者
     */
    String FIELD_MODIFIER = "modifier";

    /**
     * 数据版本号
     */
    String FIELD_VERSION = "version";

    /**
     * 逻辑删除标
     */
    String FIELD_DELETE_VERSION = "deleteVersion";
}
