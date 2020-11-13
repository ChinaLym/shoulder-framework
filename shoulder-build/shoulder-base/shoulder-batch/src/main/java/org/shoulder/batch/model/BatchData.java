package org.shoulder.batch.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 批量任务
 * 校验完毕后，组装成该模型，传入Service，导入管理类会将本类分割转化为 {@link BatchDataSlice}
 *
 * @author lym
 */
@Data
public class BatchData {

    /**
     * 业务标识 / 数据类型
     */
    protected String dataType;

    // @deprecated -----------------------
    protected List<? extends DataItem> addList = new ArrayList<>();

    protected List<? extends DataItem> updateList = new ArrayList<>();

    /**
     * 需要批量执行分类，通常只有一个键值对（一项业务）
     * 操作类型（如 ADD、UPDATE...） - 要处理的数据
     */
    protected Map<String, List<? extends DataItem>> batchListMap = new HashMap<>(4);


    // ======================= 执行导入完毕后才能确定 =======================
    /**
     * 直接成功的数据，如校验已存在，需要跳过
     */
    protected List<? extends DataItem> successList = new ArrayList<>();

    /**
     * 直接失败的数据，如校验未通过，不要导入
     */
    protected List<? extends DataItem> failList = new ArrayList<>();

    /**
     * 校验失败原因（校验 / 导入）
     * 行号 - 失败原因
     */
    protected Map<Integer, String> failReason = new HashMap<>();


    public BatchData() {
    }

}
