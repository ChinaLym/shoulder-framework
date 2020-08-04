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
    String ID = "id";

    /**
     * 创建时间
     */
    String CREATE_TIME = "createTime";

    /**
     * 最后一次修改时间
     */
    String UPDATE_TIME = "updateTime";

    /**
     * 创建者
     */
    String CREATOR = "creator";

    /**
     * 最后一次修改者
     */
    String MODIFIER = "modifier";


    // ------------------ 常用的数据源名称 ------------------

    /**
     * 主数据库
     */
    String LEADER = "leader";

    /**
     * 从数据库
     */
    String FOLLOWER = "follower";

    /**
     * 只读数据库
     */
    String READ = "read";

    /**
     * 读写数据库
     */
    String WRITE = "write";
}
