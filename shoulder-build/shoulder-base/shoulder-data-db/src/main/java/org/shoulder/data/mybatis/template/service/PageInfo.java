package org.shoulder.data.mybatis.template.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

/**
 * 分页模型
 *
 * @author lym
 */
@Getter
@Setter
public class PageInfo<T> extends Page<T> {

    @Serial private static final long serialVersionUID = 1L;

    /**
     * 条件
     */
    private T condition;

    /**
     * 扩展条件
     * 如日期范围
     */
    private ExtendMap extend = new ExtendMap();

}
