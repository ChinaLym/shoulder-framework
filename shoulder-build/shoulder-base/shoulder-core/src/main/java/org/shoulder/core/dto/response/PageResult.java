package org.shoulder.core.dto.response;

import com.github.pagehelper.PageInfo;
import io.swagger.annotations.ApiModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 分页数据
 * <p>
 * 统一分页列表返回值，作为 {@link RestResult} 的 data 字段，total，list
 *
 * @author lym
 */
@ApiModel(value = "分页数据返回格式")
public class PageResult<T> extends ListResult<T> implements Serializable {

    private static final long serialVersionUID = -1451879834966540928L;
    /**
     * 当前页
     */
    private int pageNum;

    /**
     * 每页的数量
     */
    private int pageSize;

    /**
     * 当前页的数量
     */
    private int size;

    /**
     * 总页数
     */
    private int totalPageNum;

    /**
     * 是否为第一页
     */
    private boolean firstPage = false;

    /**
     * 是否为最后一页
     */
    private boolean lastPage = false;

    /**
     * 有前一页
     */
    private boolean hasPreviousPage = false;

    /**
     * 有后一页
     */
    private boolean hasNextPage = false;


    public PageResult() {
    }

    public static <T> PageResult<T> empty(int pageSize) {
        PageResult<T> pageResult = new PageResult<>();
        pageResult.firstPage = true;
        pageResult.lastPage = true;
        pageResult.totalPageNum = 0;
        pageResult.size = 0;
        pageResult.pageNum = 1;
        pageResult.pageSize = pageSize;
        return pageResult;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getTotalPageNum() {
        return totalPageNum;
    }

    public void setTotalPageNum(int totalPageNum) {
        this.totalPageNum = totalPageNum;
    }

    public boolean isFirstPage() {
        return firstPage;
    }

    public void setFirstPage(boolean firstPage) {
        this.firstPage = firstPage;
    }

    public boolean isLastPage() {
        return lastPage;
    }

    public void setLastPage(boolean lastPage) {
        this.lastPage = lastPage;
    }

    public boolean isHasPreviousPage() {
        return hasPreviousPage;
    }

    public void setHasPreviousPage(boolean hasPreviousPage) {
        this.hasPreviousPage = hasPreviousPage;
    }

    public boolean isHasNextPage() {
        return hasNextPage;
    }

    public void setHasNextPage(boolean hasNextPage) {
        this.hasNextPage = hasNextPage;
    }


    public static PageResultBuilder builder() {
        return new PageResultBuilder();
    }

    public static <T> PageResult<T> build(List<T> list, int pageNum, int pageSize, long totalCount) {
        PageResult<T> result = new PageResult<>();
        result.setList(list);
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        result.setTotal(totalCount);
        int totalFullPage = (int) (totalCount / pageSize);
        if (totalCount % pageSize != 0) {
            totalFullPage++;
        }
        result.setTotalPageNum(totalFullPage);
        return result;
    }

    public static final class PageResultBuilder {
        boolean firstPage = false;
        private int pageNum;
        private int pageSize;
        private int size;
        private int totalPageNum;
        //@Schema(name = "数据总数")
        private int total = 0;
        private boolean lastPage = false;
        private boolean hasPreviousPage = false;
        //@Schema(name = "列表数据")
        private List list;
        private boolean hasNextPage = false;

        private PageResultBuilder() {
        }

        public static PageResultBuilder create() {
            return new PageResultBuilder();
        }

        public PageResultBuilder pageNum(int pageNum) {
            this.pageNum = pageNum;
            return this;
        }

        public PageResultBuilder pageSize(int pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public PageResultBuilder size(int size) {
            this.size = size;
            return this;
        }

        public PageResultBuilder totalPageNum(int totalPageNum) {
            this.totalPageNum = totalPageNum;
            return this;
        }

        public PageResultBuilder total(int total) {
            this.total = total;
            return this;
        }

        public PageResultBuilder firstPage(boolean firstPage) {
            this.firstPage = firstPage;
            return this;
        }

        public PageResultBuilder lastPage(boolean lastPage) {
            this.lastPage = lastPage;
            return this;
        }

        public PageResultBuilder hasPreviousPage(boolean hasPreviousPage) {
            this.hasPreviousPage = hasPreviousPage;
            return this;
        }

        public PageResultBuilder list(List list) {
            this.list = list;
            return this;
        }

        public PageResultBuilder hasNextPage(boolean hasNextPage) {
            this.hasNextPage = hasNextPage;
            return this;
        }

        @SuppressWarnings("unchecked")
        public <T> PageResult<T> build() {
            PageResult<T> pageResult = new PageResult<>();
            pageResult.setPageNum(pageNum);
            pageResult.setPageSize(pageSize);
            pageResult.setSize(size);
            pageResult.setTotalPageNum(totalPageNum);
            pageResult.setTotal(total);
            pageResult.setFirstPage(firstPage);
            pageResult.setLastPage(lastPage);
            pageResult.setHasPreviousPage(hasPreviousPage);
            pageResult.setList(list);
            pageResult.setHasNextPage(hasNextPage);
            return pageResult;
        }
    }

    public static class PageInfoConverter {
        public static <T> PageResult<T> toResult(PageInfo<T> pageInfo) {
            if (pageInfo == null) {
                return null;
            }

            PageResult<T> pageResult = new PageResult<>();

            pageResult.setFirstPage(pageInfo.isIsFirstPage());
            pageResult.setLastPage(pageInfo.isIsLastPage());
            pageResult.setTotalPageNum(pageInfo.getPages());
            pageResult.setTotal(pageInfo.getTotal());
            List<T> list = pageInfo.getList();
            if (list != null) {
                pageResult.setList(new ArrayList<>(list));
            }
            pageResult.setPageNum(pageInfo.getPageNum());
            pageResult.setPageSize(pageInfo.getPageSize());
            pageResult.setSize(pageInfo.getSize());
            pageResult.setHasPreviousPage(pageInfo.isHasPreviousPage());
            pageResult.setHasNextPage(pageInfo.isHasNextPage());

            return pageResult;
        }
    }

}
