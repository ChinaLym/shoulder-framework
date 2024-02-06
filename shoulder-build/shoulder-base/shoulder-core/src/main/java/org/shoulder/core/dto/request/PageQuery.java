package org.shoulder.core.dto.request;

import com.fasterxml.jackson.core.type.TypeReference;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.shoulder.core.dto.ToStringObj;
import org.shoulder.core.util.JsonUtils;
import org.shoulder.core.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Map;

import static org.shoulder.core.constant.PageConst.DEFAULT_PAGE_NO;
import static org.shoulder.core.constant.PageConst.DEFAULT_PAGE_SIZE;
import static org.shoulder.core.constant.PageConst.PARAM_PAGE_NO;
import static org.shoulder.core.constant.PageConst.PARAM_PAGE_SIZE;
import static org.shoulder.core.constant.PageConst.PARAM_RULES;
import static org.shoulder.core.constant.PageConst.PARAM_SORT_BY;

/**
 * 分页参数 DTO
 *
 * @author lym
 */
@Getter
@Setter
@ApiModel("分页查询 DTO param")
public class PageQuery<DTO> extends ToStringObj {

    private static final long serialVersionUID = -3462907130101674607L;

    /**
     * 页码
     * 不设置默认值，有些框架（如 JPA）页码是从0开始
     */
    @ApiModelProperty(value = "", dataType = "int", example = "1")
    private Integer pageNo;

    /**
     * 每页大小
     */
    @ApiModelProperty(value = "", dataType = "int", example = "20")
    private Integer pageSize;

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

    private Class<DTO> dtoType = resolveModelClass();

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
        this.condition = JsonUtils.parseObject(JsonUtils.toJson(map), dtoType);
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
        HttpServletRequest servletRequest;
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
        PageQuery<T> result = new PageQuery<>();
        String pageNo = request.getParameter(PARAM_PAGE_NO);
        String pageSize = request.getParameter(PARAM_PAGE_SIZE);
        String orderRules = request.getParameter(PARAM_RULES);

        result.pageNo = StringUtils.isEmpty(pageNo) ? DEFAULT_PAGE_NO : Integer.parseInt(pageNo);
        result.pageSize = StringUtils.isEmpty(pageSize) ? DEFAULT_PAGE_SIZE : Integer.parseInt(pageSize);
        if (StringUtils.isNotEmpty(orderRules)) {
            result.orderRules = JsonUtils.parseObject(orderRules, new TypeReference<List<OrderRule>>() {
            });
        }
        return result;
    }


    @SuppressWarnings("unchecked")
    protected Class<DTO> resolveModelClass() {
        return dtoType = condition == null ? null : (Class<DTO>) condition.getClass();
        //return (Class<DTO>) GenericTypeResolver.resolveTypeArguments(this.getClass(), PageQuery.class)[2];
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

