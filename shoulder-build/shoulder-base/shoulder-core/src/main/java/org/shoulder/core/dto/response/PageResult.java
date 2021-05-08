package org.shoulder.core.dto.response;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.ApiModel;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 分页数据
 * <p>
 * 统一分页列表返回值，作为 {@link BaseResult} 的 data 字段，total，list
 *
 * @author lym
 */
@ApiModel(value = "分页数据返回格式")
public class PageResult<T> extends ListResult<T> implements Serializable {

    private static final long serialVersionUID = -1451879834966540928L;
    /**
     * 当前页
     */
    private Integer pageNum;

    /**
     * 每页的数量
     */
    private Integer pageSize;

    /**
     * 当前页的数量
     */
    private Integer size;

    /**
     * 总页数
     */
    private Integer totalPageNum;

    /**
     * 是否为第一页
     */
    private Boolean firstPage = false;

    /**
     * 是否为最后一页
     */
    private Boolean lastPage = false;

    /**
     * 有前一页
     */
    private Boolean hasPreviousPage = false;

    /**
     * 有后一页
     */
    private Boolean hasNextPage = false;

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

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Integer getTotalPageNum() {
        return totalPageNum;
    }

    public void setTotalPageNum(Integer totalPageNum) {
        this.totalPageNum = totalPageNum;
    }

    public Boolean getFirstPage() {
        return firstPage;
    }

    public void setFirstPage(Boolean firstPage) {
        this.firstPage = firstPage;
    }

    public Boolean getLastPage() {
        return lastPage;
    }

    public void setLastPage(Boolean lastPage) {
        this.lastPage = lastPage;
    }

    public Boolean getHasPreviousPage() {
        return hasPreviousPage;
    }

    public void setHasPreviousPage(Boolean hasPreviousPage) {
        this.hasPreviousPage = hasPreviousPage;
    }

    public Boolean getHasNextPage() {
        return hasNextPage;
    }

    public void setHasNextPage(Boolean hasNextPage) {
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


    public static class PageInfoConverter {
        public static <T> PageResult<T> toResult(@Nonnull PageInfo<T> pageInfo) {
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

    public static class IPageConverter {
        public static <T> PageResult<T> toResult(@Nonnull IPage<T> page) {
            if (page == null) {
                return null;
            }

            PageResult<T> pageResult = new PageResult<>();

            pageResult.setFirstPage(page.getCurrent() == 1L);
            pageResult.setLastPage(page.getCurrent() == page.getTotal());
            pageResult.setTotalPageNum((int) page.getPages());
            pageResult.setTotal(page.getTotal());
            List<T> list = page.getRecords();
            if (list != null) {
                pageResult.setList(new ArrayList<>(list));
            }
            pageResult.setPageNum((int) page.getCurrent());
            pageResult.setPageSize((int) page.getPages());
            pageResult.setSize((int) page.getSize());
            pageResult.setHasPreviousPage(!pageResult.getFirstPage());
            pageResult.setHasNextPage(!pageResult.getLastPage());

            return pageResult;
        }
    }

    public static final class PageResultBuilder {
        //@Schema(name = "数据总数")
        private Long total = 0L;
        //@Schema(name = "列表数据")
        private List list;
        private Integer pageNum;
        private Integer pageSize;
        private Integer size;
        private Integer totalPageNum;
        private Boolean firstPage = false;
        private Boolean lastPage = false;
        private Boolean hasPreviousPage = false;
        private Boolean hasNextPage = false;

        private PageResultBuilder() {
        }

        public static PageResultBuilder aPageResult() {
            return new PageResultBuilder();
        }

        public PageResultBuilder total(Long total) {
            this.total = total;
            return this;
        }

        public PageResultBuilder list(List list) {
            this.list = list;
            return this;
        }

        public PageResultBuilder pageNum(Integer pageNum) {
            this.pageNum = pageNum;
            return this;
        }

        public PageResultBuilder pageSize(Integer pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public PageResultBuilder size(Integer size) {
            this.size = size;
            return this;
        }

        public PageResultBuilder totalPageNum(Integer totalPageNum) {
            this.totalPageNum = totalPageNum;
            return this;
        }

        public PageResultBuilder firstPage(Boolean firstPage) {
            this.firstPage = firstPage;
            return this;
        }

        public PageResultBuilder lastPage(Boolean lastPage) {
            this.lastPage = lastPage;
            return this;
        }

        public PageResultBuilder hasPreviousPage(Boolean hasPreviousPage) {
            this.hasPreviousPage = hasPreviousPage;
            return this;
        }

        public PageResultBuilder hasNextPage(Boolean hasNextPage) {
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
}
