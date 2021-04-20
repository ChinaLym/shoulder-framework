package org.shoulder.core.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

import static org.shoulder.core.constant.PageConst.*;

/**
 * 分页参数 BO
 *
 * @author lym
 */
@ApiModel("分页查询 DTO param")
public class BasePageQuery<T> implements Serializable {

    private static final long serialVersionUID = 6532091359995631065L;
    /**
     * 页码
     */
    @ApiModelProperty(value = "", dataType = "int", example = "1", required = false)
    private int pageNo = DEFAULT_PAGE_NO;

    /**
     * 每页大小
     */
    @ApiModelProperty(value = "", dataType = "int", example = "20", required = false)
    private int pageSize = DEFAULT_PAGE_SIZE;

    /**
     * 待排序的字段名称
     */
    @ApiModelProperty(value = "", example = "xxx", required = false)
    private String sortBy;

    /**
     * 排序顺序 asc | desc
     */
    @ApiModelProperty(value = "", example = "asc", required = false)
    private String order = DEFAULT_ORDER;

    private T condition;

    public BasePageQuery() {
    }

    public BasePageQuery(int pageNo, int pageSize) {
        this(pageNo, pageSize, "", "");
    }

    public BasePageQuery(int pageNo, int pageSize, String sortBy, String order) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.sortBy = sortBy;
        this.order = order;
    }

    public int getPageNo() {
        if (pageNo <= MIN_PAGE_NO) {
            pageNo = DEFAULT_PAGE_NO;
        }
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public int getPageSize() {
        if (pageSize > MAX_PAGE_SIZE) {
            pageSize = MAX_PAGE_SIZE;
        }
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getOrder() {
        return order;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}

