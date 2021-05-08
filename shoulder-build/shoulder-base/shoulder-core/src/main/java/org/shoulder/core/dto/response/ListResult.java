package org.shoulder.core.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.List;

/**
 * 列表数据对象
 * <p>
 * 统一列表返回值，作为 {@link BaseResult} 的 data 字段，total，list
 *
 * @author lym
 */
@ApiModel(value = "列表数据返回格式")
//@Schema(description = "列表数据返回内容")
public class ListResult<T> implements Serializable {

    private static final long serialVersionUID = -3134782461635924904L;
    //@Schema(name = "数据总数")
    @ApiModelProperty(value = "数据总数", dataType = "long", required = true, example = "4", position = 0)
    private Long total = 0L;

    //@Schema(name = "列表数据")
    @ApiModelProperty(value = "列表数据", dataType = "List", required = true, example = "[1,2,3,4]", position = 1)
    private List<T> list;

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public ListResult() {

    }

    public ListResult(List<T> list) {
        this.list = list;
        this.total = (long) this.list.size();
    }

    public static <T> ListResult<T> of(List<T> list) {
        return new ListResult<>(list);
    }
}
