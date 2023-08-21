/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2023 All Rights Reserved.
 */
package org.shoulder.core.concurrent;

import cn.hutool.core.thread.NamedThreadFactory;
import org.apache.ibatis.exceptions.TooManyResultsException;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.util.AssertUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 高性能优先级阻塞队列
 * 比 PriorityBlockingQueue 不通级别隔离程度更高、性能更好，可用于处理请求，比如生产流量 VIP 优先，生产流量普通用户次之，测试流量再次之..
 *
 * @author lym
 * @see java.util.concurrent.PriorityBlockingQueue 插入复杂度为 O(log n)，而Linked / Array BlockingQueue 插入复杂度为 O(1), 性能差距太大，
 * 故针优先级别数量较小的场景定制该类，时间复杂度也为 O(1)
 * https://developer.aliyun.com/article/84588
 */
public class FastPriorityBlockingQueue<E> implements BlockingQueue<E> {

    /**
     * 放的时候根据 priorityFetcher 计算数组下标（优先级）放入
     * 取的时候从第一个队列先取，取不到再并发阻塞 take
     */
    final BlockingQueue<E>[] queuesArray;

    /**
     * 入参 null 则 NullPointerException
     */
    final Function<E, Integer> priorityFetcher;

    /**
     * true 【高性能 | 默认】严格按照先处理高优先，再处理低优先的，高优先处理不完，低优先永远得不到处理
     * false 整体会先处理高优先，但实际低优先也不至于不会处理
     */
    final boolean alwaysAcquireHighPriorityFirst;

    final ThreadPoolExecutor threadPoolExecutor;

    public FastPriorityBlockingQueue(Supplier<BlockingQueue<E>> blockingQueueConstruction, Function<E, Integer> priorityFetcher, int priorityCount) {
        this(blockingQueueConstruction, priorityFetcher, priorityCount, true);
    }

    public FastPriorityBlockingQueue(Supplier<BlockingQueue<E>> blockingQueueConstruction, Function<E, Integer> priorityFetcher, int priorityCount, boolean alwaysAcquireHighPriorityFirst) {
        this(blockingQueueConstruction, priorityFetcher, priorityCount, alwaysAcquireHighPriorityFirst,
                // 由于该线程池只是用于等待，故理论上不会占用什么 CPU 资源，核数调大，避免阻塞
                new ThreadPoolExecutor(priorityCount * 32, priorityCount * 32,
                        60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(),
                        new NamedThreadFactory("FastPriorityBlockingQueue", true)
                ));
    }

    public FastPriorityBlockingQueue(Supplier<BlockingQueue<E>> blockingQueueConstruction, Function<E, Integer> priorityFetcher, int priorityCount, boolean alwaysAcquireHighPriorityFirst, ThreadPoolExecutor threadPoolExecutor) {
        AssertUtils.isTrue(priorityCount < 30, CommonErrorCodeEnum.ILLEGAL_PARAM, "Too many priorityCount, use PriorityBlockingQueue pls.");
        this.queuesArray = new BlockingQueue[priorityCount];
        for (int i = 0; i < priorityCount; i++) {
            queuesArray[i] = blockingQueueConstruction.get();
        }
        this.priorityFetcher = priorityFetcher;
        this.alwaysAcquireHighPriorityFirst = alwaysAcquireHighPriorityFirst;
        // 核数可以多点，主要是 wait
        this.threadPoolExecutor = threadPoolExecutor;
    }

    private BlockingQueue<E> getQueue(E e) {
        return queuesArray[priorityFetcher.apply(e)];
    }


    @Override
    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        return getQueue(e).offer(e, timeout, unit);
    }

    @Override
    public boolean add(E e) {
        return getQueue(e).add(e);
    }

    @Override
    public boolean offer(E e) {
        return getQueue(e).offer(e);
    }

    @Override
    public void put(E e) throws InterruptedException {
        getQueue(e).put(e);
    }

    @Override
    public E peek() {
        for (BlockingQueue<E> queue : queuesArray) {
            E e = queue.peek();
            if (e != null) {
                return e;
            }
        }
        return null;
    }

    @Override
    public Iterator<E> iterator() {
        // 非核心api，无锁; 特殊处理：聚合多个迭代器
        Iterator<E>[] iterators = new Iterator[queuesArray.length];
        for (int i = 0; i < iterators.length; i++) {
            iterators[i] = queuesArray[i].iterator();
        }
        return new MultiIterator<>(iterators);
    }

    @Override
    public E poll() {
        for (BlockingQueue<E> queue : queuesArray) {
            E e = queue.poll();
            if (e != null) {
                return e;
            }
        }
        return null;
    }

    @Override
    public E take() throws InterruptedException {
        if (alwaysAcquireHighPriorityFirst) {
            // 先尝试挨个 poll 避免阻塞（由于设计上，该类的优先级不会特别多，故不会有太多循环带来的额外性能损耗）
            E result = poll();
            if (result != null) {
                return result;
            }
        }
        // 都是空的，再阻塞
        return concurrentTake(queue -> {
            try {
                return queue.take();
            } catch (InterruptedException e) {
                throw new InterruptedExceptionWrapper(null, e);
            }
        });
    }

    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        if (alwaysAcquireHighPriorityFirst) {
            // 先尝试挨个 poll 避免阻塞（由于设计上，该类的优先级不会特别多，故不会有太多循环带来的额外性能损耗）
            E result = poll();
            if (result != null) {
                return result;
            }
        }
        return concurrentTake(queue -> {
            try {
                return queue.poll(timeout, unit);
            } catch (InterruptedException e) {
                throw new InterruptedExceptionWrapper(null, e);
            }
        });
    }

    protected E concurrentTake(Function<BlockingQueue<E>, E> takeFunction) throws InterruptedException {
        // 并行take,拿多了放回去
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<E> finalTake = new AtomicReference<>();
        for (BlockingQueue<E> queue : queuesArray) {
            Runnable r = () -> {
                E taked = null;
                boolean setted = false;
                try {
                    taked = takeFunction.apply(queue);
                    setted = finalTake.compareAndSet(null, taked);
                    if (setted) {
                        latch.countDown();
                    }
                } finally {
                    // 避免拿出来了，还没尝试 set，就cancel了
                    if (taked != null && !setted) {
                        // 加回去，改为返回 null，相当于没拿出来
                        queue.add(taked);
                    }
                }
            };
            threadPoolExecutor.execute(r);
        }
        latch.await();
        return finalTake.get();
    }

    @Override
    public E remove() {
        for (BlockingQueue<E> queue : queuesArray) {
            E e = queue.remove();
            if (e != null) {
                return e;
            }
        }
        throw new NoSuchElementException("No such element");
    }

    @Override
    public Object[] toArray() {
        // 非核心api，仅实现无锁
        if (size() == Integer.MAX_VALUE) {
            // 无法保证转的数据齐全：实际元素会更多，结果会丢
            throw new TooManyResultsException();
        }
        Object[] arr = new Object[queuesArray.length];
        int count = 0;
        for (int i = 0; i < queuesArray.length; i++) {
            Object[] arr2 = queuesArray[i].toArray();
            arr[i] = arr2;
            count += arr2.length;
        }
        Object[] result = new Object[count];

        int added = 0;
        for (int i = 0; i < arr.length; i++) {
            int toCopy = ((Object[]) arr[i]).length;
            System.arraycopy(((Object[]) arr[i]), 0, result, added, toCopy);
            added += toCopy;
        }
        return result;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        // 非核心api，无锁，多次拷贝
        int size = this.size();
        if (a.length < size) {
            throw new ArrayStoreException("need bigger capacity than " + size);
        }
        Object[] result = toArray();
        System.arraycopy(result, 0, a, 0, result.length);
        return a;
    }

    @Override
    public E element() {
        for (BlockingQueue<E> queue : queuesArray) {
            E e = queue.element();
            if (e != null) {
                return e;
            }
        }
        throw new NoSuchElementException("No such element");
    }

    @Override
    public boolean remove(Object e) {
        return getQueue((E) e).remove(e);
    }

    @Override
    public boolean contains(Object e) {
        return getQueue((E) e).contains(e);
    }

    @Override
    public boolean addAll(Collection c) {
        if (c == this) {
            throw new IllegalArgumentException();
        }

        boolean addedOne = false;
        for (Object o : c) {
            addedOne = addedOne || getQueue((E) o).add((E) o);
        }
        return addedOne;
    }

    @Override
    public void clear() {
        for (BlockingQueue<E> queue : queuesArray) {
            queue.clear();
        }
    }

    @Override
    public boolean retainAll(Collection c) {
        // 删除指定集合中不存在的那些元素
        boolean hasDelete = false;
        for (BlockingQueue<E> queue : queuesArray) {
            boolean d = queue.retainAll(c);
            hasDelete = hasDelete || d;
        }
        return hasDelete;
    }

    @Override
    public boolean removeAll(Collection c) {
        // 删除指定集合中元素
        boolean hasDelete = false;
        for (Object o : c) {
            boolean d = remove(o);
            hasDelete = hasDelete || d;
        }
        return hasDelete;
    }

    @Override
    public boolean containsAll(Collection c) {
        for (Object o : c) {
            if (!contains(o)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int remainingCapacity() {
        long count = 0;
        for (BlockingQueue<E> queue : queuesArray) {
            count += queue.remainingCapacity();
            if (count >= Integer.MAX_VALUE) {
                // 保证不越界
                return Integer.MAX_VALUE;
            }
        }
        return (int) count;
    }

    @Override
    public int size() {
        long count = 0;
        for (BlockingQueue<E> queue : queuesArray) {
            count += queue.size();
            if (count >= Integer.MAX_VALUE) {
                // 保证不越界
                return Integer.MAX_VALUE;
            }
        }
        return (int) count;
    }

    @Override
    public boolean isEmpty() {
        return Arrays.stream(queuesArray).allMatch(Collection::isEmpty);
    }

    @Override
    public int drainTo(Collection c) {
        // 非阻塞的批量获取元素，等于 pool 多个，性能更好点
        return drainTo(c, Integer.MAX_VALUE);
    }

    @Override
    public int drainTo(Collection c, int maxElements) {
        int pooled = 0;
        for (BlockingQueue<E> queue : queuesArray) {
            pooled += queue.drainTo(c, maxElements);
            maxElements -= pooled;
            if (maxElements == 0) {
                return pooled;
            }
        }
        return pooled;
    }

    public static class MultiIterator<X> implements Iterator<X> {

        private final AtomicInteger current = new AtomicInteger(0);

        private final Iterator<X>[] iterators;

        public MultiIterator(Iterator<X>[] iterators) {
            this.iterators = iterators;
        }

        @Override
        public boolean hasNext() {
            int c = current.get();
            return iterators[c].hasNext() || c < iterators.length - 1;
        }

        @Override
        public synchronized X next() {
            int c = current.get();
            if (iterators[c].hasNext()) {
                return iterators[c].next();
            }
            // cas
            current.compareAndSet(c, c + 1);
            return next();
        }
    }

    public static class InterruptedExceptionWrapper extends RuntimeException {
        public InterruptedExceptionWrapper(String message, InterruptedException cause) {
            super(message, cause);
        }

        public InterruptedException getCause() {
            return (InterruptedException) super.getCause();
        }
    }

}
