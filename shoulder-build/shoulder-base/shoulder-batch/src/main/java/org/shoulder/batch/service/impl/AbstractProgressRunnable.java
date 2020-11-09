package org.shoulder.batch.service.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

public abstract class AbstractProgressRunnable implements Runnable, Serializable {

    /**
     * 任务ID
     */
    protected String taskId;
    /**
     * 任务需要处理的记录总数
     */
    protected int total;
    /**
     * 任务已经处理的记录数
     */
    protected int processed;

    public boolean finished;

    protected AbstractProgressRunnable() {
    }


    public boolean checkOver() {
        return finished;
    }

    /**
     * 任务执行初始时间
     */
    protected long timeBegin = System.currentTimeMillis();

    protected int waitTimeBegin = 10000;
    /**
     * 任务执行结束时间
     */
    protected long timeEnd;
    private AtomicLong waitTimeLeftInvoked = new AtomicLong(0);

    public AbstractProgressRunnable(String taskId) {
        this.taskId = taskId;
        this.timeBegin = System.currentTimeMillis();
    }

    @JsonIgnore
    public float getProgress() {
        if (finished || total == 0) {
            return 1;
        }
        if (!finished && this.processed == (float) total) {
            return 0.911111F;
        }
        return this.processed / (float) total;
    }

    public int getTotal() {
        return total;
    }

    protected void updateProcessed(int processed) {
        this.processed = processed;
    }

    public int getProcessed() {
        if (finished) {
            return total;
        }
        return processed;
    }

    protected void addProcessed(int processedThisTime) {
        processed += processedThisTime;
    }

    @JsonIgnore
    public long getTimeProcessed() {
        if (finished) {
            return timeEnd - timeBegin;
        }
        return System.currentTimeMillis() - ProgressTaskPool.getStartTime(taskId);
    }

    @JsonIgnore
    public long getTimeLeft() {
        if (finished) {
            return 0L;
        }
        long timeProcessed = getTimeProcessed() / 1000;
        int processed = getProcessed();
        if (processed == 0) {
            return getWaitingTimeLeft();
        }
        return (timeProcessed * (getTotal() - processed)) / processed;
    }

    long getWaitingTimeLeft() {
        waitTimeLeftInvoked.incrementAndGet();
        //sqrt开方，pow平方
        return (long) (waitTimeBegin + 20 * Math.sqrt(total) * Math.pow(waitTimeLeftInvoked.get(), 2));
    }

    public String getTaskId() {
        return taskId;
    }

    public void markDone() {
        finished = true;
        setTimeEnd(System.currentTimeMillis());
        processed = total;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public void setProcessed(int processed) {
        this.processed = processed;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public long getTimeBegin() {
        return timeBegin;
    }

    public void setTimeBegin(long timeBegin) {
        this.timeBegin = timeBegin;
    }

    public int getWaitTimeBegin() {
        return waitTimeBegin;
    }

    public void setWaitTimeBegin(int waitTimeBegin) {
        this.waitTimeBegin = waitTimeBegin;
    }

    public long getTimeEnd() {
        return timeEnd;
    }

    public void setTimeEnd(long timeEnd) {
        this.timeEnd = timeEnd;
    }

    public AtomicLong getWaitTimeLeftInvoked() {
        return waitTimeLeftInvoked;
    }

    public void setWaitTimeLeftInvoked(AtomicLong waitTimeLeftInvoked) {
        this.waitTimeLeftInvoked = waitTimeLeftInvoked;
    }
}
