package org.shoulder.core.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.List;

/**
 * 列表数据对象
 * @author lym
 */
@Schema(description = "列表数据返回内容")
public class ListResponse<T> implements Serializable {

    @Schema(name = "列表数据")
    private List<T> list;
    @Schema(name = "列表数据总数")
    private Long total;

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }
}
