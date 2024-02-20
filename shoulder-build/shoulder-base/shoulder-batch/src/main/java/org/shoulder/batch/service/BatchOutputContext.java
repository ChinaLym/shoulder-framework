package org.shoulder.batch.service;

import lombok.Data;
import org.shoulder.batch.config.model.ExportFileConfig;
import org.shoulder.batch.spi.DataExporter;

import java.util.HashMap;
import java.util.Map;

/**
 * 批量处理 - 输出 - 上下文
 *
 * @author lym
 */
@Data
public class BatchOutputContext {

    private static ThreadLocal<BatchOutputContext> currentContext = ThreadLocal.withInitial(BatchOutputContext::new);

    /**
     * 导出器
     */
    private DataExporter currentDataExporter;
    /**
     * 导出配置
     */
    private ExportFileConfig exportConfig;

    /**
     * 是否额外生成详情列（当且仅当导出批量处理结果时使用）
     * 额外输出：数据行号、处理结果、处理结果详情
     */
    private boolean extraDetail = false;

    private Map<String, Object> ext = new HashMap<>();

    public <T> T getExtValue(String key) {
        return (T) ext.get(key);
    }

    public <T> T putExtValue(String key, T value) {
        return (T) ext.put(key, value);
    }

    public static BatchOutputContext get() {
        return currentContext.get();
    }

    public static void clean() {
        currentContext.remove();
    }
}
