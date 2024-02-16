package org.shoulder.batch.spi;

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

    default String serialize() {
        return JsonUtils.toJson(this);
    }

}
