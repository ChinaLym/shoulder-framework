package org.shoulder.core.constant;

/**
 * 分页相关常量
 *
 * @author lym
 */
public interface PageConst {

    // -------------------- 默认值 --------------------

    /**
     * 默认页码
     */
    int DEFAULT_PAGE_NO = 1;
    /**
     * 默认显示条数
     */
    int DEFAULT_PAGE_SIZE = 20;
    /**
     * 默认显示条数
     */
    String DEFAULT_ORDER = "asc";
    /**
     * 默认最小页码
     */
    int MIN_PAGE_NO = 0;
    /**
     * 最大显示条数
     */
    int MAX_PAGE_SIZE = 1000;

    // -------------------- 参数名称 --------------------
    /**
     * 页码 KEY
     */
    String PARAM_PAGE_NO = "pageNo";
    /**
     * 每页显示条数 KEY
     */
    String PARAM_PAGE_SIZE = "pageSize";
    /**
     * 排序字段 KEY
     */
    String PARAM_SORT_BY = "sortBy";
    /**
     * 排序方向 KEY (ASC\DESC)
     */
    String PARAM_ORDER = "order";

}
