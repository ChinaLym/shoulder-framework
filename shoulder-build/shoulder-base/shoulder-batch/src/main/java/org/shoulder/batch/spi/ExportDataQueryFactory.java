package org.shoulder.batch.spi;

import jakarta.validation.constraints.NotNull;
import org.shoulder.core.dto.request.PageQuery;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 数据导出
 * 用于查询数据
 *
 * @author lym
 */
public interface ExportDataQueryFactory {

    boolean support(@NotNull String businessType, @Nullable PageQuery<Map> exportCondition);

    List<Supplier<List<Map<String, String>>>> createQuerySuppliers(@NotNull String businessType, @Nullable PageQuery<Map> exportCondition);

}
