package org.shoulder.data.enums;

/**
 * 数据库类型枚举
 *
 * @author lym
 */
public enum DataSourceType {

    /** 读数据库，可以有多个 DataSource Bean */
    READ,
    /** 写数据库，只有一个 DataSource Bean */
    WRITE,
    ;

}
