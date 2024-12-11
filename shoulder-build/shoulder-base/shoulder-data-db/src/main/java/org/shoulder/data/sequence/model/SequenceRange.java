package org.shoulder.data.sequence.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.shoulder.core.util.AssertUtils;
import org.shoulder.validate.exception.ParamErrorCodeEnum;

import java.io.Serial;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 * 管理和生成组合序列号的范围
 *
 * @author lym
 */
@EqualsAndHashCode//(callSuper = true)
@Data
@NoArgsConstructor
public class SequenceRange {//extends SequenceRouteInfo {

    @Serial private static final long serialVersionUID = -8033123630197877163L;

    public static final String NAME = "name";
    public static final String VALUE = "current_value";
    public static final String MIN_VALUE = "min_value";
    public static final String MAX_VALUE = "max_value";
    public static final String STEP = "step";
    public static final String GMT_CREATE = "create_time";
    public static final String GMT_MODIFIED = "update_time";
    public static final String PARTION_ID = "partition_id";

    private Date systemDate;

    private Date fetchDate;

    private String name;

    private long step;

    private long min;

    private long max;

    private volatile long value;

    private long latestValue;

    private final AtomicLong localValue = new AtomicLong();

    private String sequenceSourceId;

    /**
     * For AntCloud multiple tenant
     */
    private String tntId;

    /**
     * refresh on true
     */
    private volatile boolean isOver = false;

    public SequenceRange(String sequenceName, long min, long max, long step, long value) {
        setName(sequenceName);
        setMax(max);
        setMin(min);
        setStep(step);
        setValue(value);
        localValue.set(value);
    }

    public void setValue(long value) {
        synchronized (localValue) {
            this.value = value;
            localValue.set(value);
        }
    }

    public boolean needRefresh() {
        //It is over
        if (isOver) {
            return isOver;
        }

        return !((localValue.get() < step + value) && localValue.get() <= max);
    }

    public void over() {
        this.isOver = true;
    }

    public long genNextValue() {
        return localValue.getAndIncrement();
    }

    /**
     * 批量获取：成功返回一批，失败返回null
     * @param size 获取大小
     */
    public List<Long> genNextValues(int size) {
        long current = localValue.get();
        if(current + size < max) {
            boolean success = localValue.compareAndSet(current, current + size);
            if(success) {
                return LongStream.range(current, current + size).boxed().collect(Collectors.toList());
            }
        }
        return null;
    }

    public long currentValue() {
        return localValue.get();
    }

    public boolean isQualifiedValue(long value) {
        return value >= 0 && value <= max && value >= min;
    }

    public void validate() {
        AssertUtils.isTrue(min < max && min + step < max, ParamErrorCodeEnum.PARAM_ILLEGAL, "SequenceRange_rule invalid.");
    }

    public void cloneContextValue(SequenceRange cloneSource) {
        // if (null != cloneSource) {
        //     setDatabaseIndex(cloneSource.getDatabaseIndex());
        //     setDatabaseShardValue(cloneSource.getDatabaseShardValue());
        //     setTableShardValue(cloneSource.getTableShardValue());
        //     setTargetTableName(cloneSource.getTargetTableName());
        //     setLogicalTableName(cloneSource.getLogicalTableName());
        //     setRouteCondition(cloneSource.getRouteCondition());
        //     setSequenceSourceId(cloneSource.getSequenceSourceId());
        //     setElasticDataSourceIndex(cloneSource.getElasticDataSourceIndex());
        //     setAllContext(cloneSource.getAllContext());
        //
        //     setPartitionId(cloneSource.getPartitionId());
        // }
    }

}
