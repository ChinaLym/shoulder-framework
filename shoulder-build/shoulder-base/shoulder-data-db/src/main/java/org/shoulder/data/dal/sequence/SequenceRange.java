package org.shoulder.data.dal.sequence;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 序列区间
 *
 * @author lym
 *
 */
public class SequenceRange {
    private final long       min;
    private final long       max;

    private final AtomicLong value;

    private volatile boolean over = false;

    public SequenceRange(long min, long max) {
        this.min = min;
        this.max = max;
        this.value = new AtomicLong(min);
    }

    public long getAndIncrement() {
        long currentValue = value.getAndIncrement();
        if (currentValue > max) {
            over = true;
            return -1;
        }

        return currentValue;
    }

    public long getMin() {
        return min;
    }

    public long getMax() {
        return max;
    }

    public boolean isOver() {
        return over;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (max ^ (max >>> 32));
        result = prime * result + (int) (min ^ (min >>> 32));
        result = prime * result + (over ? 1231 : 1237);
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SequenceRange other = (SequenceRange) obj;
        if (max != other.max)
            return false;
        if (min != other.min)
            return false;
        if (over != other.over)
            return false;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }

    public AtomicLong getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "SequenceRange [max=" + max + ", min=" + min + ", over=" + over + ", value="
               + value.get() + "]";
    }
}
