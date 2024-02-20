package org.shoulder.batch.model;

import lombok.Data;
import org.shoulder.batch.spi.DataItem;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * 批量任务分片
 * 已经被拆分过的，可直接执行的任务分片，通常比 {@link BatchData} 粒度更细
 * 数据类型 dataType, 操作类型 operationType 全部确定，且所有数据相同
 *
 * @author lym
 */
@Data
public class BatchDataSlice {

    /**
     * 批处理批处理任务id
     * 其实可通过 BatchDataWorker 来填充。未来可能调整，预留该字段
     */
    private String batchId;

    /**
     * 本次批量任务中的第几个子任务
     */
    private int sequence;

    /**
     * 数据类型
     */
    private String dataType;

    /**
     * 操作类型：ADD / UPDATE...
     */
    protected String operationType;

    /**
     * 当前分片内处理的数据数目
     */
    protected Function<BatchDataSlice, Integer> dataSizeCalculator = s -> s.getBatchList().size();

    /**
     * 当前分片内处理的数据 index List
     */
    protected Function<BatchDataSlice, Stream<Integer>> dataIndexStreamCalculator = s -> s.getBatchList().stream().map(DataItem::getIndex);

    /**
     * 要批量处理的数据
     */
    private List<? extends DataItem> batchList;

    public BatchDataSlice() {
    }

    public BatchDataSlice(String batchId, int sequence, String dataType, String operationType, List<? extends DataItem> batchList) {
        this.batchId = batchId;
        this.sequence = sequence;
        this.dataType = dataType;
        this.operationType = operationType;
        this.batchList = batchList;
    }

    @Override
    public String toString() {
        return "BatchDataSlice{" +
                "batchId='" + batchId + '\'' +
                ", sequence=" + sequence +
                ", dataType='" + dataType + '\'' +
                ", operationType='" + operationType + '\'' +
                ", batchList.size=" + batchList.size() +
                '}';
    }

    public Integer calculateDataSize() {
        return this.dataSizeCalculator.apply(this);
    }

    public Stream<Integer> dataIndexStream() {
        return this.dataIndexStreamCalculator.apply(this);
    }

}
