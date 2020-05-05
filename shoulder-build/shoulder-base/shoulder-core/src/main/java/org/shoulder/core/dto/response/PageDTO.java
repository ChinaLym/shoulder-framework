package org.shoulder.core.dto.response;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页数据
 * @author lym
 */
@Data
public class PageDTO<T> implements Serializable {

    private Integer pageNo;

    private Integer pageSize;

    private Boolean hasNext;

    private Integer total;

    private List<T> list;

    private Integer totalPage;

}
