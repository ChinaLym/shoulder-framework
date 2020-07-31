package org.shoulder.core.dto.response;

import io.swagger.annotations.ApiModel;

import java.io.Serializable;

/**
 * 分页数据
 * <p>
 * 统一分页列表返回值，作为 {@link BaseResponse} 的 data 字段，total，list
 *
 * @author lym
 */
@ApiModel(value = "分页数据返回格式")
public class PageDTO<T> extends ListResponse implements Serializable {

    private Integer pageNo;

    private Integer pageSize;

    private Boolean hasNext;

    private Integer totalPage;

    public Integer getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Boolean getHasNext() {
        return hasNext;
    }

    public void setHasNext(Boolean hasNext) {
        this.hasNext = hasNext;
    }

    public Integer getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(Integer totalPage) {
        this.totalPage = totalPage;
    }
}
