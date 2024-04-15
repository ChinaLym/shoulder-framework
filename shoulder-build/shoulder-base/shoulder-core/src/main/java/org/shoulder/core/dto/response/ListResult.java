package org.shoulder.core.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.shoulder.core.dto.ToStringObj;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 列表数据对象
 * <p>
 * 统一列表返回值，作为 {@link BaseResult} 的 data 字段，total，list
 *
 * @author lym
 */
@Schema(name = "列表数据返回格式")
public class ListResult<T> extends ToStringObj {

    private static final long serialVersionUID = -3134782461635924904L;
    @Schema(name = "数据总数", type = "long", requiredMode = Schema.RequiredMode.REQUIRED, example = "4")
    private Long total = 0L;

    @Schema(name = "列表数据", type = "List", requiredMode = Schema.RequiredMode.REQUIRED, example = "[1,2,3,4]")
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

    public ListResult(Collection<? extends T> list) {
        this.list = new ArrayList<>(list);
        this.total = (long) this.list.size();
    }

    public static <T> ListResult<T> of(Collection<? extends T> list) {
        return new ListResult<>(list);
    }

    public static <T> ListResult<T> empty() {
        return new ListResult<>(Collections.emptyList());
    }
}
