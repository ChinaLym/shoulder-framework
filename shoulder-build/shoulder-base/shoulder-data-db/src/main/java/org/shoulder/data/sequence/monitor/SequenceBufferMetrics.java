package org.shoulder.data.sequence.monitor;

import lombok.Data;

@Data
public class SequenceBufferMetrics {
    private String sequenceName;
    private long   value0;
    private int    step0;
    private long   max0;

    private long value1;
    private int  step1;
    private long max1;

    // 当前buffer下标 0/1
    private int     positionId;
    // 下一个是否可用
    private boolean nextReady;
    // 是否已从 DB 获取了一批序列
    private boolean initialized;

}
