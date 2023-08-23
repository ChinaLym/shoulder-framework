/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2023 All Rights Reserved.
 */

import org.shoulder.core.concurrent.BaseDecorateableBlockingQueue;

import java.util.concurrent.BlockingQueue;

/**
 * monitorable
 * fixme 测试用，暂不上线
 */
public abstract class MonitorableBlockingQueue<E> extends BaseDecorateableBlockingQueue<E> {


    public MonitorableBlockingQueue(BlockingQueue<E> delegateBlockingQueue) {
        super(delegateBlockingQueue);
    }

    protected void enQueue(E e) {

    }

    protected E deQueue(E e) {
        return e;
    }

}
