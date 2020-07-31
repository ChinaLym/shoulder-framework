package org.shoulder.core.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.List;

/**
 * 列表数据对象
 * <p>
 * 统一列表返回值，作为 {@link BaseResponse} 的 data 字段，total，list
 *
 * @author lym
 */
@ApiModel(value = "列表数据返回格式")
//@Schema(description = "列表数据返回内容")
public class ListResponse<T> implements Serializable {

    //@Schema(name = "列表数据总数")
    @ApiModelProperty(value = "列表数据总数", dataType = "long", required = true, example = "4", position = 0)
    private Long total;

    //@Schema(name = "列表数据")
    @ApiModelProperty(value = "列表数据", dataType = "List", required = true, example = "[1,2,3,4]", position = 1)
    private List<T> list;

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }
}
