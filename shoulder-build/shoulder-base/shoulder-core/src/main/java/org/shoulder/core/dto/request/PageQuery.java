package org.shoulder.core.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.Map;

import static org.shoulder.core.constant.PageConst.*;

/**
 * 分页参数
 *
 * @author lym
 */
@ApiModel("分页查询 DTO param")
public class PageQuery implements Serializable {


    /**
     * 页码
     */
    @ApiModelProperty(value = "", dataType = "int", example = "1")
    private int pageNo = DEFAULT_PAGE_NO;

    /**
     * 每页大小
     */
    @ApiModelProperty(value = "", dataType = "int", example = "20")
    private int pageSize = DEFAULT_PAGE_SIZE;

    /**
     * 待排序的字段名称
     */
    @ApiModelProperty(value = "", example = "xxx")
    private String sortBy;

    /**
     * 排序顺序 asc | desc
     */
    @ApiModelProperty(value = "", example = "asc")
    private String order = DEFAULT_ORDER;

    public PageQuery() {
    }

    @SuppressWarnings("unchecked")
    public PageQuery(Map map) {
        if (map == null) {
            return;
        }
        this.pageNo = Integer.parseInt(map.getOrDefault(PARAM_PAGE_NO, DEFAULT_PAGE_NO).toString());
        this.pageSize = Integer.parseInt(map.getOrDefault(PARAM_PAGE_SIZE, DEFAULT_PAGE_SIZE).toString());
        this.sortBy = (String) map.getOrDefault(PARAM_SORT_BY, "");
        this.order = (String) map.getOrDefault(PARAM_ORDER, "");
        map.remove(PARAM_PAGE_NO);
        map.remove(PARAM_PAGE_SIZE);
        map.remove(PARAM_SORT_BY);
        map.remove(PARAM_ORDER);
    }

    public PageQuery(int pageNo, int pageSize) {
        this(pageNo, pageSize, "", "");
    }

    public PageQuery(int pageNo, int pageSize, String sortBy, String order) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.sortBy = sortBy;
        this.order = order;
    }

    /**
     * 从当前请求中获取与分页相关信息
     *
     * @return 分页相关信息
     */
    public static PageQuery fromRequest() {
        HttpServletRequest servletRequest = null;
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            servletRequest = ((ServletRequestAttributes) requestAttributes).getRequest();
            return fromRequest(servletRequest);
        }
        return null;
    }

    /**
     * 从请求中获取与分页相关信息
     *
     * @param request 请求
     * @return 分页相关信息
     */
    public static PageQuery fromRequest(HttpServletRequest request) {
        PageQuery result = new PageQuery();
        String pageNo = request.getParameter(PARAM_PAGE_NO);
        String pageSize = request.getParameter(PARAM_PAGE_SIZE);

        result.pageNo = StringUtils.isEmpty(pageNo) ? DEFAULT_PAGE_NO : Integer.parseInt(pageNo);
        result.pageSize = StringUtils.isEmpty(pageSize) ? DEFAULT_PAGE_SIZE : Integer.parseInt(pageSize);
        result.sortBy = request.getParameter(PARAM_SORT_BY);
        result.order = request.getParameter(PARAM_ORDER);
        return result;
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

}

