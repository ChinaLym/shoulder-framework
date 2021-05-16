package org.shoulder.core.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.shoulder.core.util.JsonUtils;
import org.shoulder.core.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import static org.shoulder.core.constant.PageConst.*;

/**
 * 分页参数 DTO
 *
 * @author lym
 */
@ApiModel("分页查询 DTO param")
public class PageQuery<DTO> implements Serializable {

    private static final long serialVersionUID = -3462907130101674607L;

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
     * 排序规则
     */
    @ApiModelProperty(value = "", example = "xxx")
    private List<OrderRule> orderRules;

    /**
     * 查询条件
     */
    @ApiModelProperty("查询条件")
    private DTO condition;

    /**
     * 扩展
     */
    private Map<String, Object> ext;

    public PageQuery() {
    }

    @SuppressWarnings("unchecked")
    @Deprecated
    public PageQuery(Map map) {
        if (map == null) {
            return;
        }
        this.pageNo = Integer.parseInt(map.getOrDefault(PARAM_PAGE_NO, DEFAULT_PAGE_NO).toString());
        this.pageSize = Integer.parseInt(map.getOrDefault(PARAM_PAGE_SIZE, DEFAULT_PAGE_SIZE).toString());
        this.orderRules = (List<OrderRule>) map.getOrDefault(PARAM_SORT_BY, "");
        this.condition = JsonUtils.parseObject(JsonUtils.toJson(map));
        map.remove(PARAM_PAGE_NO);
        map.remove(PARAM_PAGE_SIZE);
        map.remove(PARAM_SORT_BY);
        map.remove(PARAM_RULES);
    }

    public PageQuery(int pageNo, int pageSize) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
    }

    /**
     * 从当前请求中获取与分页相关信息
     *
     * @return 分页相关信息
     */
    public static <T> PageQuery<T> fromRequest() {
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
    public static <T> PageQuery<T> fromRequest(HttpServletRequest request) {
        PageQuery<T> result = new PageQuery<T>();
        String pageNo = request.getParameter(PARAM_PAGE_NO);
        String pageSize = request.getParameter(PARAM_PAGE_SIZE);
        String orderRules = request.getParameter(PARAM_RULES);

        result.pageNo = StringUtils.isEmpty(pageNo) ? DEFAULT_PAGE_NO : Integer.parseInt(pageNo);
        result.pageSize = StringUtils.isEmpty(pageSize) ? DEFAULT_PAGE_SIZE : Integer.parseInt(pageSize);
        if (StringUtils.isNotEmpty(orderRules)) {
            result.orderRules = JsonUtils.parseObject(orderRules);
        }
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

    public DTO getCondition() {
        return condition;
    }

    public void setCondition(DTO condition) {
        this.condition = condition;
    }

    public List<OrderRule> getOrderRules() {
        return orderRules;
    }

    public void setOrderRules(List<OrderRule> orderRules) {
        this.orderRules = orderRules;
    }

    public Map<String, Object> getExt() {
        return ext;
    }

    public void setExt(Map<String, Object> ext) {
        this.ext = ext;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    @Data
    public static class OrderRule {

        /**
         * DTO 字段名
         */
        private String fieldName;

        /**
         * 顺序 asc/desc
         */
        private String order = "asc";

    }


}

