package org.shoulder.batch.model;

import lombok.Data;
import org.shoulder.batch.service.impl.BatchManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 批量任务
 * 校验完毕后，组装成该模型，传入Service，批处理管理类（{@link BatchManager}）会将本类分割转化为 {@link BatchDataSlice}
 *
 * @author lym
 */
@Data
public class BatchData {

    /**
     * 业务标识 / 数据类型
     */
    protected String dataType;

    /**
     * 需要批量处理分类，通常只有一个键值对（一项业务）
     * 操作类型（如 ADD、UPDATE...） - 要处理的数据
     */
    protected Map<String, List<? extends DataItem>> batchListMap = new HashMap<>(4);


    // ======================= 处理完毕后才能确定 =======================
    /**
     * 直接成功的数据，如校验已存在，需要跳过
     */
    protected List<? extends DataItem> successList = new ArrayList<>();

    /**
     * 直接失败的数据，如校验未通过，不要处理
     */
    protected List<? extends DataItem> failList = new ArrayList<>();

    /**
     * 校验失败原因（校验 / 处理）
     * 行号 - 失败原因
     */
    protected Map<Integer, String> failReason = new HashMap<>();


    public BatchData() {
    }

}
