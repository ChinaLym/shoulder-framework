package org.shoulder.ext.config.domain;

import java.util.List;

/**
 * @author lym
 */
public class PageInfo<T> {

    /**
     * 当前页码
     */
    private Integer pageNo;
    /**
     * 页大小
     */
    private Integer pageSize;
    /**
     * 总数量
     */
    private Long totalCount;
    /**
     * 总页数
     */
    private Long totalPage;

    /**
     * 数据
     */
    private List<T> data;

    public PageInfo() {
    }

    /**
     * Success result.
     *
     * @param <T>  the type parameter
     * @param data the data
     * @return the result
     */
    public static <T> PageInfo<T> success(List<T> data, int pageNum, int pageSize, long totalCount) {
        PageInfo<T> result = new PageInfo<>();
        result.setData(data);
        result.setPageSize(pageSize);
        result.setPageNo(pageNum);
        result.setTotalCount(totalCount);
        long totalFullPage = totalCount / pageSize;
        if (totalCount % pageSize != 0) {
            totalFullPage++;
        }
        result.setTotalPage(totalFullPage);
        return result;
    }

    /**
     * Getter method for property <tt>pageNo</tt>.
     *
     * @return property value of pageNo
     */
    public Integer getPageNo() {
        return pageNo;
    }

    /**
     * Setter method for property <tt>pageNo</tt>.
     *
     * @param pageNo value to be assigned to property pageNo
     */
    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    /**
     * Getter method for property <tt>pageSize</tt>.
     *
     * @return property value of pageSize
     */
    public Integer getPageSize() {
        return pageSize;
    }

    /**
     * Setter method for property <tt>pageSize</tt>.
     *
     * @param pageSize value to be assigned to property pageSize
     */
    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * Getter method for property <tt>totalCount</tt>.
     *
     * @return property value of totalCount
     */
    public Long getTotalCount() {
        return totalCount;
    }

    /**
     * Setter method for property <tt>totalCount</tt>.
     *
     * @param totalCount value to be assigned to property totalCount
     */
    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    /**
     * Getter method for property <tt>totalPage</tt>.
     *
     * @return property value of totalPage
     */
    public Long getTotalPage() {
        return totalPage;
    }

    /**
     * Setter method for property <tt>totalPage</tt>.
     *
     * @param totalPage value to be assigned to property totalPage
     */
    public void setTotalPage(Long totalPage) {
        this.totalPage = totalPage;
    }

    /**
     * Getter method for property <tt>data</tt>.
     *
     * @return property value of data
     */
    public List<T> getData() {
        return data;
    }

    /**
     * Setter method for property <tt>data</tt>.
     *
     * @param data value to be assigned to property data
     */
    public void setData(List<T> data) {
        this.data = data;
    }
}