package org.shoulder.core.dto.request;

import jakarta.annotation.Nonnull;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.shoulder.core.dto.ToStringObj;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.util.AssertUtils;

import java.io.Serial;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.shoulder.core.constant.PageConst.DEFAULT_PAGE_NO;
import static org.shoulder.core.constant.PageConst.DEFAULT_PAGE_SIZE;
import static org.shoulder.core.constant.PageConst.MAX_PAGE_SIZE;
import static org.shoulder.core.constant.PageConst.MIN_PAGE_NO;

/**
 * 分页参数 BO
 *
 * @author lym
 */
public class BasePageQuery<T> extends ToStringObj {

    @Serial private static final long serialVersionUID = 6532091359995631065L;
    /**
     * 页码
     */
    private                      int  pageNo           = DEFAULT_PAGE_NO;

    /**
     * 每页大小
     */
    private int pageSize = DEFAULT_PAGE_SIZE;

    /**
     * 排序规则
     */
    private List<OrderRule> orderRules;

    /**
     * 查询条件
     */
    private T condition;

    /**
     * 扩展
     */
    private Map<String, Object> ext;

    public BasePageQuery() {
    }

    @SuppressWarnings("unchecked")
    public static <ENTITY, DTO> BasePageQuery<ENTITY> create(@Nonnull PageQuery<DTO> pageQuery) {
        // convertUtil 支持泛型?
        AssertUtils.isTrue(false, CommonErrorCodeEnum.CODING, "function not support!");
        return create(pageQuery, dto -> (ENTITY) dto);
    }

    public static <ENTITY, DTO> BasePageQuery<ENTITY> create(@Nonnull PageQuery<DTO> pageQuery, Function<DTO, ENTITY> converter) {
        BasePageQuery<ENTITY> page = new BasePageQuery<>();
        page.pageNo = pageQuery.getPageNo();
        page.pageSize = pageQuery.getPageSize();
        page.condition = converter.apply(pageQuery.getCondition());
        if (CollectionUtils.isNotEmpty(pageQuery.getOrderRules())) {
            page.orderRules = pageQuery.getOrderRules().stream()
                    .map(r -> new OrderRule(r.getFieldName(), Order.getByName(r.getOrder())))
                    .collect(Collectors.toList());
        }
        return page;
    }

    public BasePageQuery(int pageNo, int pageSize) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
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

    public List<OrderRule> getOrderRules() {
        return orderRules;
    }

    public void setOrderRules(List<OrderRule> orderRules) {
        this.orderRules = orderRules;
    }

    public T getCondition() {
        return condition;
    }

    public void setCondition(T condition) {
        this.condition = condition;
    }

    public Map<String, Object> getExt() {
        return ext;
    }

    public void setExt(Map<String, Object> ext) {
        this.ext = ext;
    }

    @Data
    public static class OrderRule {

        /**
         * domain model 字段名
         */
        private String fieldName;

        /**
         * 顺序 asc/desc
         */
        private Order order = Order.ASC;

        public OrderRule() {
        }

        public OrderRule(String fieldName, Order order) {
            this.fieldName = fieldName;
            this.order = order;
        }
    }

    /**
     * 排序
     */
    public enum Order {
        /**
         * 正序
         */
        ASC,
        /**
         * 逆序
         */
        DESC,
        ;

        public static Order getByName(String name) {
            // 默认正序
            return "desc".equalsIgnoreCase(name) ? DESC : ASC;
        }

    }


}

