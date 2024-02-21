package org.shoulder.batch.spi;

import org.shoulder.batch.model.BatchRecordDetail;
import org.shoulder.core.util.JsonUtils;

/**
 * 批量记录某一项（多行中某一行）
 *
 * @author lym
 */
public interface DataItem {

    /**
     * 获取本项在整体批量操作中的行号
     *
     * @return 行号
     */
    int getIndex();

    /**
     * 保存至 recordDetail 表的 source 字段 {@link BatchRecordDetail#setSource(String)}
     * - 为了后续反序列化方便，建议使用 json 格式
     *
     * @return str
     */
    default String serialize() {
        return JsonUtils.toJson(this);
    }

}
